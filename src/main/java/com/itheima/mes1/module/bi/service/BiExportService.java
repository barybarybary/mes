package com.itheima.mes1.module.bi.service;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.mes1.module.bi.entity.BiReportConfig;
import com.itheima.mes1.module.bi.mapper.BiMapper;
import com.itheima.mes1.module.inventory.entity.Inventory;
import com.itheima.mes1.module.inventory.mapper.InventoryMapper;
import com.itheima.mes1.module.production.entity.WorkOrder;
import com.itheima.mes1.module.production.mapper.WorkOrderMapper;
import com.itheima.mes1.module.sale.entity.SaleOrder;
import com.itheima.mes1.module.sale.mapper.SaleOrderItemMapper;
import com.itheima.mes1.module.sale.mapper.SaleOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class BiExportService {

    @Autowired private BiMapper biMapper;
    @Autowired private SaleOrderMapper saleOrderMapper;
    @Autowired private SaleOrderItemMapper saleOrderItemMapper;
    @Autowired private InventoryMapper inventoryMapper;
    @Autowired private WorkOrderMapper workOrderMapper;

    /** 销售报表 Excel */
    public byte[] exportSalesExcel(Integer year, Integer month) {
        int y = year != null ? year : LocalDate.now().getYear();
        int m = month != null ? month : LocalDate.now().getMonthValue();
        LocalDateTime start = LocalDate.of(y, m, 1).atStartOfDay();
        LocalDateTime end = start.plusMonths(1);

        List<SaleOrder> orders = saleOrderMapper.selectList(
                new LambdaQueryWrapper<SaleOrder>()
                        .ge(SaleOrder::getCreateTime, start)
                        .lt(SaleOrder::getCreateTime, end)
                        .orderByDesc(SaleOrder::getCreateTime));

        try (ExcelWriter writer = ExcelUtil.getWriter(true)) {
            writer.addHeaderAlias("orderNo", "订单号");
            writer.addHeaderAlias("customerName", "客户");
            writer.addHeaderAlias("orderDate", "订单日期");
            writer.addHeaderAlias("deliveryDate", "交期");
            writer.addHeaderAlias("totalAmount", "金额");
            writer.addHeaderAlias("status", "状态");

            List<Map<String, Object>> rows = new ArrayList<>();
            for (SaleOrder o : orders) {
                SaleOrder full = saleOrderMapper.selectWithCustomer(o.getId());
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("orderNo", o.getOrderNo());
                row.put("customerName", full != null ? full.getCustomerName() : "");
                row.put("orderDate", o.getOrderDate());
                row.put("deliveryDate", o.getDeliveryDate());
                row.put("totalAmount", o.getTotalAmount());
                row.put("status", statusName(o.getStatus()));
                rows.add(row);
            }
            writer.write(rows, true);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            writer.flush(out);
            return out.toByteArray();
        }
    }

    /** 库存报表 Excel */
    public byte[] exportInventoryExcel() {
        List<Inventory> list = inventoryMapper.selectAllWithDetail();
        try (ExcelWriter writer = ExcelUtil.getWriter(true)) {
            writer.addHeaderAlias("productCode", "产品编码");
            writer.addHeaderAlias("productName", "产品名称");
            writer.addHeaderAlias("warehouseName", "仓库");
            writer.addHeaderAlias("batchNo", "批次");
            writer.addHeaderAlias("quantity", "数量");
            writer.addHeaderAlias("unit", "单位");

            List<Map<String, Object>> rows = new ArrayList<>();
            for (Inventory inv : list) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("productCode", inv.getProductCode());
                row.put("productName", inv.getProductName());
                row.put("warehouseName", inv.getWarehouseName());
                row.put("batchNo", inv.getBatchNo());
                row.put("quantity", inv.getQuantity());
                row.put("unit", inv.getUnit());
                rows.add(row);
            }
            writer.write(rows, true);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            writer.flush(out);
            return out.toByteArray();
        }
    }

    /** 生产报表 Excel */
    public byte[] exportProductionExcel(Integer year, Integer month) {
        int y = year != null ? year : LocalDate.now().getYear();
        int m = month != null ? month : LocalDate.now().getMonthValue();
        LocalDateTime start = LocalDate.of(y, m, 1).atStartOfDay();
        LocalDateTime end = start.plusMonths(1);

        List<WorkOrder> orders = workOrderMapper.selectList(
                new LambdaQueryWrapper<WorkOrder>()
                        .ge(WorkOrder::getCreateTime, start)
                        .lt(WorkOrder::getCreateTime, end)
                        .orderByDesc(WorkOrder::getCreateTime));

        try (ExcelWriter writer = ExcelUtil.getWriter(true)) {
            writer.addHeaderAlias("orderNo", "工单号");
            writer.addHeaderAlias("productName", "产品");
            writer.addHeaderAlias("quantity", "计划数");
            writer.addHeaderAlias("finishedQty", "完成数");
            writer.addHeaderAlias("qualifiedQty", "合格数");
            writer.addHeaderAlias("scrapQty", "报废数");
            writer.addHeaderAlias("status", "状态");

            List<Map<String, Object>> rows = new ArrayList<>();
            for (WorkOrder wo : orders) {
                WorkOrder full = workOrderMapper.selectWithProduct(wo.getId());
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("orderNo", wo.getOrderNo());
                row.put("productName", full != null ? full.getProductName() : "");
                row.put("quantity", wo.getQuantity());
                row.put("finishedQty", wo.getFinishedQty());
                row.put("qualifiedQty", wo.getQualifiedQty());
                row.put("scrapQty", wo.getScrapQty());
                row.put("status", woStatusName(wo.getStatus()));
                rows.add(row);
            }
            writer.write(rows, true);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            writer.flush(out);
            return out.toByteArray();
        }
    }

    private String statusName(Integer s) {
        return switch (s) {
            case 1 -> "待审核"; case 2 -> "已审核"; case 3 -> "生产中";
            case 4 -> "部分发货"; case 5 -> "已完成"; case 6 -> "已取消";
            default -> "未知";
        };
    }

    private String woStatusName(Integer s) {
        return switch (s) {
            case 1 -> "待生产"; case 2 -> "生产中"; case 3 -> "已完成"; case 4 -> "已入库";
            default -> "未知";
        };
    }
}