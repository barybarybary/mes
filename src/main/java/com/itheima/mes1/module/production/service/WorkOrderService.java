package com.itheima.mes1.module.production.service;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.mes1.common.exception.BusinessException;
import com.itheima.mes1.common.mq.MessageSender;
import com.itheima.mes1.module.production.entity.WorkOrder;
import com.itheima.mes1.module.production.entity.WorkOrderProcess;
import com.itheima.mes1.module.production.mapper.WorkOrderMapper;
import com.itheima.mes1.module.production.mapper.WorkOrderProcessMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class WorkOrderService {

    @Autowired
    private WorkOrderMapper workOrderMapper;
    @Autowired
    private WorkOrderProcessMapper processMapper;
    @Autowired
    private MessageSender messageSender;

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
        wo.setStatus(3);
        wo.setActualEnd(LocalDateTime.now());
        workOrderMapper.updateById(wo);

        messageSender.sendEvent("notify.workorder.3", buildEventPayload(wo, 3));
    }

    @Transactional
    public void finishAndStockIn(Long id) {
        WorkOrder wo = workOrderMapper.selectWithProduct(id);
        if (wo.getStatus() != 3) throw new BusinessException("只有已完成的工单可以入库");
        wo.setStatus(4);
        workOrderMapper.updateById(wo);

        messageSender.sendEvent("notify.workorder.4", buildEventPayload(wo, 4));
        // 入库逻辑由调用方处理
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
