package com.itheima.mes1.module.report.service;

import com.itheima.mes1.module.dashboard.service.DashboardService;
import com.itheima.mes1.module.report.entity.ReportRecord;
import com.itheima.mes1.module.report.mapper.ReportRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 报表生成编排 — 解析时间范围 → 查询数据 → Excel 生成 → 存库
 */
@Service
public class ReportGenerateService {

    @Autowired private DashboardService dashboardService;
    @Autowired private ReportExcelService excelService;
    @Autowired private ReportRecordMapper recordMapper;

    /**
     * 生成报表
     * @param reportType sales/production/inventory/summary
     * @param timeRange 中文时间范围描述 or "自动"
     * @param userId    生成用户
     * @return 保存后的报表记录
     */
    public ReportRecord generate(String reportType, String timeRange, Long userId) {
        // 解析时间
        LocalDate[] range = resolveRange(timeRange);
        int days = (int) (range[1].toEpochDay() - range[0].toEpochDay()) + 1;
        String timeLabel = buildTimeLabel(range, timeRange);

        // 收集数据
        Map<String, Object> data = gatherData(reportType, days);

        // 生成 Excel
        byte[] bytes = excelService.generateReport(reportType, data, timeLabel);

        // 文件名
        String typeName = switch (reportType) {
            case "production" -> "生产报表";
            case "sales" -> "销售报表";
            case "inventory" -> "库存报表";
            case "summary" -> "综合报表";
            default -> "报表";
        };
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fileName = reportType + "_report_" + datePart + ".xlsx";
        String title = timeLabel + " " + typeName;

        // 保存记录
        ReportRecord record = new ReportRecord();
        record.setUserId(userId);
        record.setTitle(title);
        record.setReportType(reportType);
        record.setTimeRange(timeLabel);
        record.setFileBytes(bytes);
        record.setFileName(fileName);
        record.setFileSize((long) bytes.length);
        recordMapper.insert(record);

        return record;
    }

    /**
     * 收集数据 — 针对不同报表类型调用 Dashboard API
     */
    private Map<String, Object> gatherData(String reportType, int days) {
        Map<String, Object> data = new LinkedHashMap<>();

        switch (reportType) {
            case "production" -> {
                Map<String, Object> mes = dashboardService.mesSummary();
                data.putAll(mes);
            }
            case "sales" -> {
                Map<String, Object> summary = dashboardService.summary();
                data.putAll(summary);
                data.put("salesTrend", dashboardService.salesTrend(days));
                data.put("rate", dashboardService.deliveryRate().get("rate"));
                data.put("completed", dashboardService.deliveryRate().get("completed"));
                data.put("ranking", dashboardService.salesRanking(days, 10));
            }
            case "inventory" -> {
                Map<String, Object> structure = dashboardService.inventoryStructure();
                data.putAll(structure);
                Map<String, Object> turnover = dashboardService.inventoryTurnover(days);
                data.putAll(turnover);
            }
            case "summary" -> {
                Map<String, Object> summary = dashboardService.summary();
                data.putAll(summary);
                Map<String, Object> mes = dashboardService.mesSummary();
                data.put("inProgressOrders", mes.get("inProgressOrders"));
                data.put("pendingOrders", mes.get("pendingOrders"));
                data.put("todayOutput", mes.get("todayOutput"));
                data.put("todayDefect", mes.get("todayDefect"));
                data.put("defectRate", mes.get("defectRate"));
                data.put("orderProgress", mes.get("orderProgress"));
                data.put("rate", dashboardService.deliveryRate().get("rate"));
                data.put("completed", dashboardService.deliveryRate().get("completed"));
                Map<String, Object> structure = dashboardService.inventoryStructure();
                data.put("totalSku", structure.get("totalSku"));
                data.put("totalQuantity", structure.get("totalQuantity"));
                data.put("warehouses", structure.get("warehouses"));
                data.put("turnoverDays", dashboardService.inventoryTurnover(days).get("turnoverDays"));
            }
        }
        return data;
    }

    /**
     * 解析时间范围
     */
    private LocalDate[] resolveRange(String timeRange) {
        LocalDate today = LocalDate.now();
        if (timeRange == null) timeRange = "本月";

        return switch (timeRange) {
            case "本周" -> new LocalDate[]{today.with(DayOfWeek.MONDAY), today};
            case "本月" -> new LocalDate[]{today.withDayOfMonth(1), today};
            case "上月" -> {
                LocalDate lastMonth = today.minusMonths(1);
                yield new LocalDate[]{lastMonth.withDayOfMonth(1), lastMonth.with(TemporalAdjusters.lastDayOfMonth())};
            }
            case "近7天" -> new LocalDate[]{today.minusDays(6), today};
            case "近30天" -> new LocalDate[]{today.minusDays(29), today};
            default -> {
                // "自动" 或其他 → 默认本月
                if (timeRange.contains("月")) {
                    yield new LocalDate[]{today.withDayOfMonth(1), today};
                }
                yield new LocalDate[]{today.minusDays(29), today};
            }
        };
    }

    private String buildTimeLabel(LocalDate[] range, String raw) {
        if ("自动".equals(raw)) {
            return range[0].format(DateTimeFormatter.ofPattern("yyyy年M月d日")) + " ~ " +
                   range[1].format(DateTimeFormatter.ofPattern("yyyy年M月d日"));
        }
        return raw != null ? raw : range[0].format(DateTimeFormatter.ofPattern("yyyy年M月"));
    }

    /** 列出用户最近的报表记录 */
    public List<ReportRecord> listByUser(Long userId, int limit) {
        return recordMapper.selectByUserId(userId, limit);
    }
}
