package com.itheima.mes1.module.bi.service;

import com.itheima.mes1.module.bi.mapper.BiMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class BiOverviewService {

    @Autowired
    private BiMapper biMapper;

    /** 经营概览卡片（全部累计数据） */
    public Map<String, Object> overview() {
        // 全部时间范围
        LocalDateTime allStart = LocalDate.of(2020, 1, 1).atStartOfDay();
        LocalDateTime allEnd = LocalDate.now().plusDays(1).atStartOfDay();
        // 上月（用于环比）
        LocalDateTime prevMonthStart = LocalDate.now().minusMonths(1).withDayOfMonth(1).atStartOfDay();
        LocalDateTime prevMonthEnd = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        BigDecimal monthSales = biMapper.selectMonthSales(allStart, allEnd);
        long monthOrders = biMapper.selectMonthOrderCount(allStart, allEnd);
        BigDecimal monthProfit = biMapper.selectMonthGrossProfit(allStart, allEnd);
        // 上月销售额（环比）
        BigDecimal prevMonthSales = biMapper.selectMonthSales(prevMonthStart, prevMonthEnd);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("monthSales", monthSales);
        data.put("monthOrders", monthOrders);
        data.put("monthProfit", monthProfit);
        // 毛利率
        BigDecimal profitRate = monthSales.compareTo(BigDecimal.ZERO) > 0
                ? monthProfit.multiply(new BigDecimal("100")).divide(monthSales, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        data.put("profitRate", profitRate);
        // 客单价
        BigDecimal avgOrder = monthOrders > 0
                ? monthSales.divide(new BigDecimal(monthOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        data.put("avgOrderAmount", avgOrder);
        // 环比
        data.put("momSales", computeChange(monthSales, prevMonthSales));
        // 同比（去年全年）
        LocalDateTime lastYearStart = LocalDate.now().minusYears(1).withDayOfYear(1).atStartOfDay();
        LocalDateTime lastYearEnd = LocalDate.now().minusYears(1).plusDays(1).atStartOfDay();
        BigDecimal lastYearSales = biMapper.selectMonthSales(lastYearStart, lastYearEnd);
        data.put("yoySales", computeChange(monthSales, lastYearSales));

        return data;
    }

    /** 月度趋势含同环比 */
    public List<Map<String, Object>> monthlyTrend(int months) {
        LocalDateTime start = LocalDate.now().minusMonths(months).withDayOfMonth(1).atStartOfDay();
        List<Map<String, Object>> dbList = biMapper.selectMonthlySales(start);

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < dbList.size(); i++) {
            Map<String, Object> row = dbList.get(i);
            String month = (String) row.get("month");
            BigDecimal amount = (BigDecimal) row.get("amount");

            // 上月数据
            BigDecimal prevAmount = BigDecimal.ZERO;
            for (int j = 0; j < dbList.size(); j++) {
                Map<String, Object> prev = dbList.get(j);
                if (isPrevMonth(month, (String) prev.get("month"))) {
                    prevAmount = (BigDecimal) prev.get("amount");
                    break;
                }
            }
            // 去年同月
            BigDecimal lastYearAmount = BigDecimal.ZERO;
            String lastYearMonth = lastYearMonth(month);
            for (int j = 0; j < dbList.size(); j++) {
                Map<String, Object> ly = dbList.get(j);
                if (lastYearMonth.equals(ly.get("month"))) {
                    lastYearAmount = (BigDecimal) ly.get("amount");
                    break;
                }
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("month", month);
            item.put("amount", amount);
            item.put("count", row.get("count"));
            item.put("mom", computeChange(amount, prevAmount));
            item.put("yoy", computeChange(amount, lastYearAmount));
            result.add(item);
        }
        return result;
    }

    /** 产品毛利排行 */
    public List<Map<String, Object>> productProfit(int limit) {
        LocalDateTime start = LocalDate.now().minusYears(1).withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = LocalDate.now().plusDays(1).atStartOfDay();
        return biMapper.selectProductProfit(start, end, limit);
    }

    /** 客户价值排行 */
    public List<Map<String, Object>> customerValue(int limit) {
        LocalDateTime start = LocalDate.now().minusYears(1).withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = LocalDate.now().plusDays(1).atStartOfDay();
        List<Map<String, Object>> list = biMapper.selectCustomerValue(start, end, limit);
        // 计算总销售额用于占比
        BigDecimal total = list.stream()
                .map(m -> (BigDecimal) m.get("total_amount"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        for (Map<String, Object> m : list) {
            BigDecimal amt = (BigDecimal) m.get("total_amount");
            BigDecimal pct = total.compareTo(BigDecimal.ZERO) > 0
                    ? amt.multiply(new BigDecimal("100")).divide(total, 1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            m.put("percentage", pct);
        }
        return list;
    }

    // ---- helpers ----

    private BigDecimal computeChange(BigDecimal current, BigDecimal prev) {
        if (prev == null || prev.compareTo(BigDecimal.ZERO) == 0) return null;
        return current.subtract(prev)
                .multiply(new BigDecimal("100"))
                .divide(prev, 1, RoundingMode.HALF_UP);
    }

    private boolean isPrevMonth(String month, String candidate) {
        if (candidate == null) return false;
        String[] parts = month.split("-");
        int y = Integer.parseInt(parts[0]), m = Integer.parseInt(parts[1]);
        if (m == 1) return candidate.equals((y - 1) + "-12");
        return candidate.equals(y + "-" + (m < 11 ? "0" : "") + (m - 1));
    }

    private String lastYearMonth(String month) {
        String[] parts = month.split("-");
        int y = Integer.parseInt(parts[0]) - 1;
        return y + "-" + parts[1];
    }
}