package com.itheima.mes1.module.dashboard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.mes1.module.inventory.entity.Inventory;
import com.itheima.mes1.module.inventory.entity.InventoryTransaction;
import com.itheima.mes1.module.inventory.mapper.InventoryMapper;
import com.itheima.mes1.module.inventory.mapper.InventoryTransactionMapper;
import com.itheima.mes1.module.production.entity.WorkOrder;
import com.itheima.mes1.module.production.mapper.WorkOrderMapper;
import com.itheima.mes1.module.sale.entity.SaleOrder;
import com.itheima.mes1.module.sale.mapper.SaleOrderItemMapper;
import com.itheima.mes1.module.sale.mapper.SaleOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DashboardService {

    @Autowired private SaleOrderMapper saleOrderMapper;
    @Autowired private SaleOrderItemMapper saleOrderItemMapper;
    @Autowired private WorkOrderMapper workOrderMapper;
    @Autowired private InventoryMapper inventoryMapper;
    @Autowired private InventoryTransactionMapper transactionMapper;

    // ==================== 首页概览卡片 ====================

    public Map<String, Object> summary() {
        long pendingOrders = saleOrderMapper.selectCount(
                new LambdaQueryWrapper<SaleOrder>().eq(SaleOrder::getStatus, 1));
        long inProgressOrders = workOrderMapper.selectCount(
                new LambdaQueryWrapper<WorkOrder>().eq(WorkOrder::getStatus, 2));
        long todayInCount = transactionMapper.selectCount(
                new LambdaQueryWrapper<InventoryTransaction>()
                        .eq(InventoryTransaction::getType, "in")
                        .ge(InventoryTransaction::getCreateTime, LocalDate.now()));
        long skuCount = inventoryMapper.selectCount(
                new LambdaQueryWrapper<Inventory>().gt(Inventory::getQuantity, 0));

        // 本月销售额
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = monthStart.plusMonths(1);
        BigDecimal totalSalesAmount = BigDecimal.ZERO;
        List<SaleOrder> monthOrders = saleOrderMapper.selectList(
                new LambdaQueryWrapper<SaleOrder>()
                        .ge(SaleOrder::getCreateTime, monthStart)
                        .lt(SaleOrder::getCreateTime, monthEnd));
        for (SaleOrder o : monthOrders) {
            if (o.getTotalAmount() != null) totalSalesAmount = totalSalesAmount.add(o.getTotalAmount());
        }
        long totalOrders = monthOrders.size();

        // 上月销售额 (环比)
        LocalDateTime prevStart = monthStart.minusMonths(1);
        long prevOrderCount = saleOrderMapper.selectCount(
                new LambdaQueryWrapper<SaleOrder>()
                        .ge(SaleOrder::getCreateTime, prevStart)
                        .lt(SaleOrder::getCreateTime, monthStart));
        BigDecimal prevSales = BigDecimal.ZERO;
        for (SaleOrder o : saleOrderMapper.selectList(
                new LambdaQueryWrapper<SaleOrder>()
                        .ge(SaleOrder::getCreateTime, prevStart)
                        .lt(SaleOrder::getCreateTime, monthStart))) {
            if (o.getTotalAmount() != null) prevSales = prevSales.add(o.getTotalAmount());
        }

        int salesGrowth = prevSales.compareTo(BigDecimal.ZERO) > 0
                ? totalSalesAmount.subtract(prevSales).multiply(new BigDecimal("100")).divide(prevSales, 0, RoundingMode.HALF_UP).intValue()
                : 0;
        int orderGrowth = prevOrderCount > 0
                ? (int) Math.round((totalOrders - prevOrderCount) * 100.0 / prevOrderCount)
                : 0;

        // 仓库维度指标
        List<Map<String, Object>> warehouseMetrics = new ArrayList<>();
        List<Map<String, Object>> whStructure = inventoryMapper.selectWarehouseStructure();
        for (Map<String, Object> wh : whStructure) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", wh.get("warehouse_name"));
            m.put("warehouseName", wh.get("warehouse_name"));
            m.put("stockQty", wh.get("total_quantity"));
            m.put("skuCount", wh.get("sku_count"));
            m.put("orderCount", totalOrders);
            m.put("outputQty", 0);
            m.put("turnoverDays", 0);
            m.put("healthScore", 75);
            warehouseMetrics.add(m);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("pendingOrders", pendingOrders);
        data.put("inProgressOrders", inProgressOrders);
        data.put("todayInCount", todayInCount);
        data.put("skuCount", skuCount);
        data.put("totalSalesAmount", totalSalesAmount);
        data.put("totalOrders", totalOrders);
        data.put("salesGrowth", salesGrowth);
        data.put("orderGrowth", orderGrowth);
        data.put("warehouseMetrics", warehouseMetrics);
        return data;
    }

    // ==================== 销售趋势（优化版） ====================

    public List<Map<String, Object>> salesTrend(int days) {
        LocalDateTime start = LocalDate.now().minusDays(days).atStartOfDay();
        LocalDateTime end = LocalDate.now().plusDays(1).atStartOfDay();

        List<Map<String, Object>> dbStats = saleOrderMapper.selectDailyStats(start, end);

        Map<String, Map<String, Object>> dateMap = new LinkedHashMap<>();
        for (Map<String, Object> row : dbStats) {
            dateMap.put(row.get("date").toString(), row);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).toString();
            Map<String, Object> stat = dateMap.get(date);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", date);
            item.put("amount", stat != null ? stat.get("amount") : BigDecimal.ZERO);
            item.put("count", stat != null ? stat.get("count") : 0);

            // 当日入库/出库
            LocalDate d = LocalDate.now().minusDays(i);
            BigDecimal inQty = transactionMapper.sumInboundQuantity(d.atStartOfDay(), d.plusDays(1).atStartOfDay());
            BigDecimal outQty = transactionMapper.sumOutboundQuantity(d.atStartOfDay(), d.plusDays(1).atStartOfDay());
            item.put("inQty", inQty != null ? inQty : BigDecimal.ZERO);
            item.put("outQty", outQty != null ? outQty : BigDecimal.ZERO);
            result.add(item);
        }
        return result;
    }

    // ==================== 生产进度 ====================

    public List<Map<String, Object>> productionProgress() {
        List<WorkOrder> orders = workOrderMapper.selectList(
                new LambdaQueryWrapper<WorkOrder>().in(WorkOrder::getStatus, 1, 2, 3));
        List<Map<String, Object>> result = new ArrayList<>();
        for (WorkOrder wo : orders) {
            WorkOrder full = workOrderMapper.selectWithProduct(wo.getId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("orderNo", wo.getOrderNo());
            item.put("productName", full != null ? full.getProductName() : "");
            item.put("quantity", wo.getQuantity());
            item.put("finishedQty", wo.getFinishedQty());
            item.put("status", wo.getStatus());
            item.put("progress", computeProgress(wo.getQuantity(), wo.getFinishedQty()));
            result.add(item);
        }
        return result;
    }

    // ==================== 订单交付率 ====================

    public Map<String, Object> deliveryRate() {
        long total = saleOrderMapper.selectCount(null);
        long completed = saleOrderMapper.selectCount(
                new LambdaQueryWrapper<SaleOrder>().eq(SaleOrder::getStatus, 5));

        // 上月交付率用于变化量
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime prevStart = monthStart.minusMonths(1);
        long prevTotal = saleOrderMapper.selectCount(
                new LambdaQueryWrapper<SaleOrder>().lt(SaleOrder::getCreateTime, monthStart));
        long prevCompleted = saleOrderMapper.selectCount(
                new LambdaQueryWrapper<SaleOrder>().eq(SaleOrder::getStatus, 5)
                        .lt(SaleOrder::getCreateTime, monthStart));
        double rate = total > 0 ? Math.round(completed * 10000.0 / total) / 100.0 : 0;
        double prevRate = prevTotal > 0 ? Math.round(prevCompleted * 10000.0 / prevTotal) / 100.0 : 0;
        int rateGrowth = (int) Math.round(rate - prevRate);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", total);
        data.put("completed", completed);
        data.put("rate", rate);
        data.put("rateGrowth", rateGrowth);
        return data;
    }

    // ==================== 库存周转率 ====================

    public Map<String, Object> inventoryTurnover(int days) {
        LocalDateTime start = LocalDate.now().minusDays(days).atStartOfDay();
        LocalDateTime end = LocalDate.now().plusDays(1).atStartOfDay();

        // 期间出库总量
        BigDecimal outQty = transactionMapper.sumOutboundQuantity(start, end);
        // 期间入库总量
        BigDecimal inQty = transactionMapper.sumInboundQuantity(start, end);
        // 当前平均库存
        BigDecimal avgStock = inventoryMapper.selectAverageStock();

        // 周转率 = 出库量 / 平均库存（按天数比例年化）
        BigDecimal turnoverRate = BigDecimal.ZERO;
        BigDecimal turnoverDays = BigDecimal.ZERO;
        if (avgStock.compareTo(BigDecimal.ZERO) > 0) {
            // 年化周转率 = (出库量 / 平均库存) * (365 / 统计天数)
            BigDecimal periodRate = outQty.divide(avgStock, 4, RoundingMode.HALF_UP);
            turnoverRate = periodRate.multiply(new BigDecimal("365"))
                    .divide(new BigDecimal(days), 2, RoundingMode.HALF_UP);
            // 周转天数 = 360 / 周转率
            if (turnoverRate.compareTo(BigDecimal.ZERO) > 0) {
                turnoverDays = new BigDecimal("360").divide(turnoverRate, 1, RoundingMode.HALF_UP);
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("period", days);
        data.put("outboundQty", outQty);
        data.put("inboundQty", inQty);
        data.put("avgStock", avgStock);
        data.put("turnoverRate", turnoverRate);
        data.put("turnoverDays", turnoverDays);
        data.put("daysGrowth", 0); // 环比变化（简化处理）
        return data;
    }

    // ==================== 库存结构分析 ====================

    public Map<String, Object> inventoryStructure() {
        // 按仓库分布
        List<Map<String, Object>> byWarehouse = inventoryMapper.selectWarehouseStructure();

        // 总数汇总
        long totalSku = 0;
        BigDecimal totalQty = BigDecimal.ZERO;
        for (Map<String, Object> row : byWarehouse) {
            totalSku += ((Number) row.get("sku_count")).longValue();
            totalQty = totalQty.add((BigDecimal) row.get("total_quantity"));
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalSku", totalSku);
        data.put("totalQuantity", totalQty);
        data.put("byWarehouse", byWarehouse);
        return data;
    }

    // ==================== 销售排行 ====================

    public Map<String, Object> salesRanking(int days, int limit) {
        LocalDateTime start = LocalDate.now().minusDays(days).atStartOfDay();
        LocalDateTime end = LocalDate.now().plusDays(1).atStartOfDay();

        List<Map<String, Object>> byProduct = saleOrderItemMapper.selectProductRanking(start, end, limit);
        List<Map<String, Object>> byCustomer = saleOrderMapper.selectCustomerRanking(start, end, limit);

        // 转换字段名匹配前端: productName, saleCount / customerName, saleAmount
        List<Map<String, Object>> products = byProduct.stream().map(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("productName", row.get("product_name"));
            m.put("saleCount", row.get("total_quantity"));
            m.put("saleAmount", row.get("total_amount"));
            return m;
        }).collect(java.util.stream.Collectors.toList());

        List<Map<String, Object>> customers = byCustomer.stream().map(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("customerName", row.get("customer_name"));
            m.put("saleCount", row.get("order_count"));
            m.put("saleAmount", row.get("total_amount"));
            return m;
        }).collect(java.util.stream.Collectors.toList());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("products", products);
        data.put("customers", customers);
        return data;
    }

    // ==================== 数据大屏 ====================

    public Map<String, Object> bigScreen() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("summary", summary());

        // 最近工单
        List<WorkOrder> recentOrders = workOrderMapper.selectList(
                new LambdaQueryWrapper<WorkOrder>().orderByDesc(WorkOrder::getCreateTime).last("LIMIT 10"));
        List<Map<String, Object>> woList = new ArrayList<>();
        for (WorkOrder wo : recentOrders) {
            WorkOrder full = workOrderMapper.selectWithProduct(wo.getId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("orderNo", wo.getOrderNo());
            item.put("productName", full != null ? full.getProductName() : "");
            item.put("status", wo.getStatus());
            item.put("quantity", wo.getQuantity());
            item.put("finishedQty", wo.getFinishedQty());
            woList.add(item);
        }
        data.put("recentOrders", woList);
        data.put("inventory", inventoryMapper.selectAllWithDetail());

        long todayReports = workOrderMapper.selectCount(
                new LambdaQueryWrapper<WorkOrder>().ge(WorkOrder::getUpdateTime, LocalDate.now()));
        data.put("todayReports", todayReports);

        return data;
    }

    // ==================== 工具方法 ====================

    private BigDecimal computeProgress(BigDecimal quantity, BigDecimal finishedQty) {
        BigDecimal q = quantity != null ? quantity : BigDecimal.ZERO;
        BigDecimal f = finishedQty != null ? finishedQty : BigDecimal.ZERO;
        if (q.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        return f.multiply(new BigDecimal("100"))
                .divide(q, 1, RoundingMode.HALF_UP);
    }
}