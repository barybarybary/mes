package com.itheima.mes1.module.bi.service;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import cn.hutool.poi.excel.StyleSet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.mes1.module.inventory.entity.Inventory;
import com.itheima.mes1.module.inventory.mapper.InventoryMapper;
import com.itheima.mes1.module.production.entity.WorkOrder;
import com.itheima.mes1.module.production.mapper.WorkOrderMapper;
import com.itheima.mes1.module.sale.entity.SaleOrder;
import com.itheima.mes1.module.sale.mapper.SaleOrderItemMapper;
import com.itheima.mes1.module.sale.mapper.SaleOrderMapper;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class BiExportService {

    @Autowired private SaleOrderMapper saleOrderMapper;
    @Autowired private SaleOrderItemMapper saleOrderItemMapper;
    @Autowired private InventoryMapper inventoryMapper;
    @Autowired private WorkOrderMapper workOrderMapper;

    /** 销售报表 Excel */
    public byte[] exportSalesExcel(Integer year, Integer month) {
        LambdaQueryWrapper<SaleOrder> qw = new LambdaQueryWrapper<SaleOrder>()
                .orderByDesc(SaleOrder::getCreateTime);

        if (year != null && month != null) {
            LocalDateTime start = LocalDate.of(year, month, 1).atStartOfDay();
            LocalDateTime end = start.plusMonths(1);
            qw.ge(SaleOrder::getCreateTime, start).lt(SaleOrder::getCreateTime, end);
        }

        List<SaleOrder> orders = saleOrderMapper.selectList(qw);

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
                row.put("orderDate", o.getOrderDate() != null ? o.getOrderDate().toString() : "");
                row.put("deliveryDate", o.getDeliveryDate() != null ? o.getDeliveryDate().toString() : "");
                row.put("totalAmount", o.getTotalAmount());
                row.put("status", statusName(o.getStatus()));
                rows.add(row);
            }
            writer.write(rows, true);

            // 设置列宽
            Sheet sheet = writer.getSheet();
            sheet.setColumnWidth(0, 5000);   // 订单号
            sheet.setColumnWidth(1, 6000);   // 客户
            sheet.setColumnWidth(2, 4000);   // 订单日期
            sheet.setColumnWidth(3, 4000);   // 交期
            sheet.setColumnWidth(4, 4000);   // 金额
            sheet.setColumnWidth(5, 3000);   // 状态

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

            Sheet sheet = writer.getSheet();
            sheet.setColumnWidth(0, 4500);
            sheet.setColumnWidth(1, 7000);
            sheet.setColumnWidth(2, 4500);
            sheet.setColumnWidth(3, 4500);
            sheet.setColumnWidth(4, 3500);
            sheet.setColumnWidth(5, 3000);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            writer.flush(out);
            return out.toByteArray();
        }
    }

    /** 生产报表 Excel */
    public byte[] exportProductionExcel(Integer year, Integer month) {
        LambdaQueryWrapper<WorkOrder> qw = new LambdaQueryWrapper<WorkOrder>()
                .orderByDesc(WorkOrder::getCreateTime);

        if (year != null && month != null) {
            LocalDateTime start = LocalDate.of(year, month, 1).atStartOfDay();
            LocalDateTime end = start.plusMonths(1);
            qw.ge(WorkOrder::getCreateTime, start).lt(WorkOrder::getCreateTime, end);
        }

        List<WorkOrder> orders = workOrderMapper.selectList(qw);

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

            Sheet sheet = writer.getSheet();
            sheet.setColumnWidth(0, 5000);
            sheet.setColumnWidth(1, 6000);
            sheet.setColumnWidth(2, 3500);
            sheet.setColumnWidth(3, 3500);
            sheet.setColumnWidth(4, 3500);
            sheet.setColumnWidth(5, 3500);
            sheet.setColumnWidth(6, 3000);

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
