package com.itheima.mes1.module.production.service;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.mes1.common.exception.BusinessException;
import com.itheima.mes1.common.mq.MessageSender;
import com.itheima.mes1.module.base.entity.Bom;
import com.itheima.mes1.module.base.mapper.BomMapper;
import com.itheima.mes1.module.production.entity.QcRecord;
import com.itheima.mes1.module.production.entity.WorkOrder;
import com.itheima.mes1.module.production.entity.WorkOrderProcess;
import com.itheima.mes1.module.production.mapper.QcRecordMapper;
import com.itheima.mes1.module.production.mapper.WorkOrderMapper;
import com.itheima.mes1.module.production.mapper.WorkOrderProcessMapper;
import com.itheima.mes1.module.sale.entity.SaleOrder;
import com.itheima.mes1.module.sale.entity.SaleOrderItem;
import com.itheima.mes1.module.sale.mapper.SaleOrderItemMapper;
import com.itheima.mes1.module.sale.mapper.SaleOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class WorkOrderService {

    @Autowired
    private WorkOrderMapper workOrderMapper;
    @Autowired
    private WorkOrderProcessMapper processMapper;
    @Autowired
    private MessageSender messageSender;
    @Autowired
    private SaleOrderMapper saleOrderMapper;
    @Autowired
    private SaleOrderItemMapper saleOrderItemMapper;
    @Autowired
    private BomMapper bomMapper;
    @Autowired
    private QcRecordMapper qcRecordMapper;

    public Page<WorkOrder> page(int page, int pageSize, Integer status) {
        LambdaQueryWrapper<WorkOrder> w = new LambdaQueryWrapper<WorkOrder>()
                .eq(status != null, WorkOrder::getStatus, status)
                .orderByDesc(WorkOrder::getCreateTime);
        Page<WorkOrder> result = workOrderMapper.selectPage(new Page<>(page, pageSize), w);
        result.setTotal(workOrderMapper.selectCount(w));
        result.getRecords().forEach(wo -> {
            WorkOrder full = workOrderMapper.selectWithProduct(wo.getId());
            if (full != null) {
                wo.setProductName(full.getProductName());
                wo.setProductCode(full.getProductCode());
            }
            wo.setProcesses(processMapper.selectByWorkOrderId(wo.getId()));
        });
        return result;
    }

    public WorkOrder getDetail(Long id) {
        WorkOrder wo = workOrderMapper.selectWithProduct(id);
        if (wo != null) wo.setProcesses(processMapper.selectByWorkOrderId(id));
        return wo;
    }

    @Transactional
    public WorkOrder create(WorkOrder wo) {
        wo.setOrderNo("WO" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                RandomUtil.randomNumbers(4));
        wo.setStatus(1);
        wo.setFinishedQty(BigDecimal.ZERO);
        wo.setQualifiedQty(BigDecimal.ZERO);
        wo.setScrapQty(BigDecimal.ZERO);
        workOrderMapper.insert(wo);

        if (wo.getProcesses() != null) {
            for (int i = 0; i < wo.getProcesses().size(); i++) {
                WorkOrderProcess wp = wo.getProcesses().get(i);
                wp.setWorkOrderId(wo.getId());
                wp.setSort(i + 1);
                wp.setStatus(1);
                wp.setFinishedQty(BigDecimal.ZERO);
                wp.setQualifiedQty(BigDecimal.ZERO);
                wp.setScrapQty(BigDecimal.ZERO);
                processMapper.insert(wp);
            }
        }
        return wo;
    }

    /**
     * 从销售订单创建生产工单（打通销→产链路）
     * 每个销售订单产品明细生成一个工单，同时加载 BOM 工序
     */
    @Transactional
    public List<WorkOrder> createFromSaleOrder(Long saleOrderId) {
        SaleOrder saleOrder = saleOrderMapper.selectById(saleOrderId);
        if (saleOrder == null) throw new BusinessException("销售订单不存在");
        if (saleOrder.getStatus() != 2) throw new BusinessException("只有已支付的订单可以转生产");

        List<SaleOrderItem> items = saleOrderItemMapper.selectByOrderId(saleOrderId);
        if (items == null || items.isEmpty()) throw new BusinessException("订单无产品明细");

        List<WorkOrder> workOrders = new ArrayList<>();
        for (SaleOrderItem item : items) {
            WorkOrder wo = new WorkOrder();
            wo.setOrderNo("WO" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                    + RandomUtil.randomNumbers(4));
            wo.setProductId(item.getProductId());
            wo.setQuantity(item.getQuantity());
            wo.setSourceType("SALE_ORDER");
            wo.setSourceNo(saleOrder.getOrderNo());
            wo.setSourceId(saleOrder.getId());
            wo.setSourceItemId(item.getId());
            wo.setStatus(1);
            wo.setFinishedQty(BigDecimal.ZERO);
            wo.setQualifiedQty(BigDecimal.ZERO);
            wo.setScrapQty(BigDecimal.ZERO);
            workOrderMapper.insert(wo);

            // 加载该产品的 BOM → 生成工单工序
            List<Bom> boms = bomMapper.selectByProductId(item.getProductId());
            if (boms != null && !boms.isEmpty()) {
                for (int i = 0; i < boms.size(); i++) {
                    Bom bom = boms.get(i);
                    if (bom.getProcessId() != null) {
                        WorkOrderProcess wp = new WorkOrderProcess();
                        wp.setWorkOrderId(wo.getId());
                        wp.setProcessId(bom.getProcessId());
                        wp.setSort(i + 1);
                        wp.setStatus(1);
                        wp.setFinishedQty(BigDecimal.ZERO);
                        wp.setQualifiedQty(BigDecimal.ZERO);
                        wp.setScrapQty(BigDecimal.ZERO);
                        processMapper.insert(wp);
                    }
                }
            }
            workOrders.add(wo);
        }

        // 更新销售订单状态 → 生产中
        saleOrder.setStatus(3);
        saleOrderMapper.updateById(saleOrder);

        return workOrders;
    }

    @Transactional
    public void startWork(Long id) {
        WorkOrder wo = workOrderMapper.selectWithProduct(id);
        if (wo.getStatus() != 1) throw new BusinessException("只有待生产的工单可以开工");
        wo.setStatus(2);
        wo.setActualStart(LocalDateTime.now());
        workOrderMapper.updateById(wo);

        messageSender.sendEvent("notify.workorder.2", buildEventPayload(wo, 2));
    }

    @Transactional
    public void complete(Long id) {
        WorkOrder wo = workOrderMapper.selectWithProduct(id);
        if (wo.getStatus() != 2) throw new BusinessException("只有生产中的工单可以完成");

        // 质检提醒：有未质检或不合格时发出警告，但不阻断完工
        List<QcRecord> qcList = qcRecordMapper.selectList(
                new LambdaQueryWrapper<QcRecord>().eq(QcRecord::getWorkOrderId, id));
        if (qcList.isEmpty()) {
            log.warn("工单 {} 完工时无质检记录，建议补录", wo.getOrderNo());
        }
        boolean hasFailed = qcList.stream().anyMatch(q -> q.getResult() != null && q.getResult() == 0);
        if (hasFailed) {
            log.warn("工单 {} 存在不合格质检记录，但已允许完工", wo.getOrderNo());
        }

        wo.setStatus(3);
        wo.setActualEnd(LocalDateTime.now());
        workOrderMapper.updateById(wo);

        messageSender.sendEvent("notify.workorder.3", buildEventPayload(wo, 3));
    }

    @Autowired
    private com.itheima.mes1.module.inventory.service.InventoryService inventoryService;

    @Transactional
    public void finishAndStockIn(Long id) {
        WorkOrder wo = workOrderMapper.selectWithProduct(id);
        if (wo.getStatus() != 3) throw new BusinessException("只有已完成的工单可以入库");
        wo.setStatus(4);
        workOrderMapper.updateById(wo);

        // 成品入库：将生产完工的产品入库到默认仓库
        try {
            inventoryService.stockIn(wo.getProductId(), null, null, null,
                    wo.getQualifiedQty() != null && wo.getQualifiedQty().compareTo(BigDecimal.ZERO) > 0
                            ? wo.getQualifiedQty() : wo.getFinishedQty(),
                    "production", wo.getOrderNo(), "生产入库");
        } catch (Exception e) {
            log.warn("工单 {} 入库失败: {}", wo.getOrderNo(), e.getMessage());
        }

        // 回写销售订单状态：如果工单来自销售订单，更新订单状态为可发货
        if (wo.getSourceId() != null && "SALE_ORDER".equals(wo.getSourceType())) {
            SaleOrder saleOrder = saleOrderMapper.selectById(wo.getSourceId());
            if (saleOrder != null && saleOrder.getStatus() == 3) {
                saleOrder.setStatus(4); // 4=已生产/待发货
                saleOrderMapper.updateById(saleOrder);
                log.info("销售订单 {} 已生产完成，进入待发货状态", saleOrder.getOrderNo());
            }
        }

        messageSender.sendEvent("notify.workorder.4", buildEventPayload(wo, 4));
    }

    private Map<String, Object> buildEventPayload(WorkOrder wo, int status) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", wo.getId());
        payload.put("orderNo", wo.getOrderNo());
        payload.put("productName", wo.getProductName() != null ? wo.getProductName() : "");
        payload.put("status", status);
        return payload;
    }
}
