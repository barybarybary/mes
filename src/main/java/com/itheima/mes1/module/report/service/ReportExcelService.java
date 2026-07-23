package com.itheima.mes1.module.report.service;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import cn.hutool.poi.excel.StyleSet;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.*;

/**
 * AI 报表 Excel 生成 — 使用 Hutool ExcelWriter 创建多 Sheet 工作簿
 */
@Service
public class ReportExcelService {

    public byte[] generateReport(String reportType, Map<String, Object> data, String timeLabel) {
        return switch (reportType) {
            case "production" -> generateProductionReport(data, timeLabel);
            case "sales" -> generateSalesReport(data, timeLabel);
            case "inventory" -> generateInventoryReport(data, timeLabel);
            case "summary" -> generateSummaryReport(data, timeLabel);
            default -> throw new IllegalArgumentException("未知报表类型: " + reportType);
        };
    }

    // ==================== 生产报表 ====================
    @SuppressWarnings("unchecked")
    private byte[] generateProductionReport(Map<String, Object> data, String timeLabel) {
        ExcelWriter writer = ExcelUtil.getWriter(true);
        styleHeader(writer);

        // Sheet 1: KPI
        writer.renameSheet("生产KPI");
        writer.writeRow(Arrays.asList("指标", "数值"), true);
        writer.writeRow(Arrays.asList("生产中工单", val(data, "inProgressOrders")));
        writer.writeRow(Arrays.asList("待生产工单", val(data, "pendingOrders")));
        writer.writeRow(Arrays.asList("今日产量", val(data, "todayOutput")));
        writer.writeRow(Arrays.asList("今日不良数", val(data, "todayDefect")));
        writer.writeRow(Arrays.asList("不良率", val(data, "defectRate") + "%"));
        autoWidth(writer);

        // Sheet 2: 生产进度
        List<Map<String, Object>> progress = (List<Map<String, Object>>) data.getOrDefault("orderProgress", List.of());
        if (!progress.isEmpty()) {
            writer.setSheet("生产进度");
            writer.writeRow(Arrays.asList("工单号", "产品", "计划数", "完成数", "进度", "状态"), true);
            for (Map<String, Object> o : progress) {
                writer.writeRow(Arrays.asList(
                        str(o, "orderNo"), str(o, "productName"),
                        val(o, "quantity"), val(o, "finishedQty"),
                        val(o, "progress") + "%",
                        statusName(o.get("status"))));
            }
            autoWidth(writer);
        }

        // Sheet 3: 不良原因
        List<Map<String, Object>> defects = (List<Map<String, Object>>) data.getOrDefault("defectCauseList", List.of());
        if (!defects.isEmpty()) {
            writer.setSheet("不良原因分布");
            writer.writeRow(Arrays.asList("原因", "次数"), true);
            for (Map<String, Object> d : defects) {
                writer.writeRow(Arrays.asList(str(d, "cause"), val(d, "count")));
            }
            autoWidth(writer);
        }

        // Sheet 4: 产量趋势
        List<Map<String, Object>> trend = (List<Map<String, Object>>) data.getOrDefault("productionTrend", List.of());
        if (!trend.isEmpty()) {
            writer.setSheet("产量趋势");
            writer.writeRow(Arrays.asList("日期", "产量"), true);
            for (Map<String, Object> t : trend) {
                writer.writeRow(Arrays.asList(str(t, "date"), val(t, "output")));
            }
            autoWidth(writer);
        }

        return flush(writer);
    }

    // ==================== 销售报表 ====================
    @SuppressWarnings("unchecked")
    private byte[] generateSalesReport(Map<String, Object> data, String timeLabel) {
        ExcelWriter writer = ExcelUtil.getWriter(true);
        styleHeader(writer);

        // Sheet 1: KPI
        writer.renameSheet("销售KPI");
        writer.writeRow(Arrays.asList("指标", "数值"), true);
        writer.writeRow(Arrays.asList("总订单数", val(data, "totalOrders")));
        writer.writeRow(Arrays.asList("总销售额", "¥" + val(data, "totalSalesAmount")));
        writer.writeRow(Arrays.asList("交付率", val(data, "rate") + "%"));
        writer.writeRow(Arrays.asList("交付完成数", val(data, "completed")));
        autoWidth(writer);

        // Sheet 2: 销售趋势
        List<Map<String, Object>> trend = (List<Map<String, Object>>) data.getOrDefault("salesTrend", List.of());
        if (!trend.isEmpty()) {
            writer.setSheet("销售趋势");
            writer.writeRow(Arrays.asList("日期", "销售额", "订单数", "入库量", "出库量"), true);
            for (Map<String, Object> t : trend) {
                writer.writeRow(Arrays.asList(
                        str(t, "date"), "¥" + val(t, "amount"),
                        val(t, "count"), val(t, "inQty"), val(t, "outQty")));
            }
            autoWidth(writer);
        }

        // Sheet 3: 产品排行
        Map<String, Object> ranking = (Map<String, Object>) data.get("ranking");
        if (ranking != null) {
            List<Map<String, Object>> products = (List<Map<String, Object>>) ranking.getOrDefault("products", List.of());
            if (!products.isEmpty()) {
                writer.setSheet("产品销售排行");
                writer.writeRow(Arrays.asList("产品", "销量", "销售额"), true);
                for (Map<String, Object> p : products) {
                    writer.writeRow(Arrays.asList(str(p, "productName"), val(p, "saleCount"), "¥" + val(p, "saleAmount")));
                }
                autoWidth(writer);
            }

            List<Map<String, Object>> customers = (List<Map<String, Object>>) ranking.getOrDefault("customers", List.of());
            if (!customers.isEmpty()) {
                writer.setSheet("客户排行");
                writer.writeRow(Arrays.asList("客户", "订单数", "销售额"), true);
                for (Map<String, Object> c : customers) {
                    writer.writeRow(Arrays.asList(str(c, "customerName"), val(c, "saleCount"), "¥" + val(c, "saleAmount")));
                }
                autoWidth(writer);
            }
        }

        return flush(writer);
    }

    // ==================== 库存报表 ====================
    @SuppressWarnings("unchecked")
    private byte[] generateInventoryReport(Map<String, Object> data, String timeLabel) {
        ExcelWriter writer = ExcelUtil.getWriter(true);
        styleHeader(writer);

        // Sheet 1: KPI
        writer.renameSheet("库存KPI");
        writer.writeRow(Arrays.asList("指标", "数值"), true);
        writer.writeRow(Arrays.asList("SKU总数", val(data, "totalSku")));
        writer.writeRow(Arrays.asList("总库存量", val(data, "totalQuantity")));
        writer.writeRow(Arrays.asList("出库总量", val(data, "totalOutbound")));
        writer.writeRow(Arrays.asList("周转次数", val(data, "turnoverRate")));
        writer.writeRow(Arrays.asList("周转天数", val(data, "turnoverDays")));
        autoWidth(writer);

        // Sheet 2: 仓库分布
        List<Map<String, Object>> warehouses = (List<Map<String, Object>>) data.getOrDefault("warehouses", List.of());
        if (!warehouses.isEmpty()) {
            writer.setSheet("仓库分布");
            writer.writeRow(Arrays.asList("仓库", "SKU数", "库存量"), true);
            for (Map<String, Object> w : warehouses) {
                writer.writeRow(Arrays.asList(str(w, "warehouseName"), val(w, "skuCount"), val(w, "quantity")));
            }
            autoWidth(writer);
        }

        return flush(writer);
    }

    // ==================== 综合报表 ====================
    @SuppressWarnings("unchecked")
    private byte[] generateSummaryReport(Map<String, Object> data, String timeLabel) {
        ExcelWriter writer = ExcelUtil.getWriter(true);
        styleHeader(writer);

        // Sheet 1: 综合KPI
        writer.renameSheet("综合KPI");
        writer.writeRow(Arrays.asList("类别", "指标", "数值"), true);
        writer.writeRow(Arrays.asList("订单", "总订单数", val(data, "totalOrders")));
        writer.writeRow(Arrays.asList("订单", "总销售额", "¥" + val(data, "totalSalesAmount")));
        writer.writeRow(Arrays.asList("订单", "交付率", val(data, "rate") + "%"));
        writer.writeRow(Arrays.asList("生产", "生产中工单", val(data, "inProgressOrders")));
        writer.writeRow(Arrays.asList("生产", "今日产量", val(data, "todayOutput")));
        writer.writeRow(Arrays.asList("生产", "不良率", val(data, "defectRate") + "%"));
        writer.writeRow(Arrays.asList("库存", "SKU总数", val(data, "totalSku")));
        writer.writeRow(Arrays.asList("库存", "周转天数", val(data, "turnoverDays")));
        autoWidth(writer);

        // Sheet 2: 生产进度
        List<Map<String, Object>> progress = (List<Map<String, Object>>) data.getOrDefault("orderProgress", List.of());
        if (!progress.isEmpty()) {
            writer.setSheet("生产进度");
            writer.writeRow(Arrays.asList("工单号", "产品", "计划数", "完成数", "进度"), true);
            for (Map<String, Object> o : progress) {
                writer.writeRow(Arrays.asList(str(o, "orderNo"), str(o, "productName"),
                        val(o, "quantity"), val(o, "finishedQty"), val(o, "progress") + "%"));
            }
            autoWidth(writer);
        }

        // Sheet 3: 仓库分布
        List<Map<String, Object>> warehouses = (List<Map<String, Object>>) data.getOrDefault("warehouses", List.of());
        if (!warehouses.isEmpty()) {
            writer.setSheet("仓库分布");
            writer.writeRow(Arrays.asList("仓库", "SKU数", "库存量"), true);
            for (Map<String, Object> w : warehouses) {
                writer.writeRow(Arrays.asList(str(w, "warehouseName"), val(w, "skuCount"), val(w, "quantity")));
            }
            autoWidth(writer);
        }

        return flush(writer);
    }

    // ==================== 工具方法 ====================

    private void styleHeader(ExcelWriter writer) {
        StyleSet style = writer.getStyleSet();
        style.getHeadCellStyle().setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        Font font = writer.getWorkbook().createFont();
        font.setBold(true);
        style.getHeadCellStyle().setFont(font);
    }

    private void autoWidth(ExcelWriter writer) {
        for (int i = 0; i < writer.getColumnCount(); i++) {
            writer.autoSizeColumn(i);
        }
    }

    private String val(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v == null) return "0";
        if (v instanceof BigDecimal bd) return bd.stripTrailingZeros().toPlainString();
        return String.valueOf(v);
    }

    private String str(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? v.toString() : "";
    }

    private String statusName(Object status) {
        if (status == null) return "未知";
        return switch (status.toString()) {
            case "1" -> "待生产";
            case "2" -> "生产中";
            case "3" -> "已完成";
            case "4" -> "已入库";
            default -> "状态" + status;
        };
    }

    private byte[] flush(ExcelWriter writer) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        writer.flush(bos);
        writer.close();
        return bos.toByteArray();
    }
}
