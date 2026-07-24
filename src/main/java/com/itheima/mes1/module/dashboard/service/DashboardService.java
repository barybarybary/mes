package com.itheima.mes1.module.dashboard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.mes1.module.inventory.entity.Inventory;
import com.itheima.mes1.module.inventory.entity.InventoryTransaction;
import com.itheima.mes1.module.inventory.mapper.InventoryMapper;
import com.itheima.mes1.module.inventory.mapper.InventoryTransactionMapper;
import com.itheima.mes1.module.production.entity.WorkOrder;
import com.itheima.mes1.module.production.mapper.WorkOrderMapper;
import com.itheima.mes1.module.dashboard.entity.OrderNotification;
import com.itheima.mes1.module.dashboard.mapper.OrderNotificationMapper;
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
    @Autowired private com.itheima.mes1.module.production.mapper.WorkReportMapper workReportMapper;
    @Autowired private com.itheima.mes1.module.production.mapper.QcRecordMapper qcRecordMapper;
    @Autowired private com.itheima.mes1.module.base.mapper.ProductMapper productMapper;
    @Autowired private com.itheima.mes1.module.base.mapper.ProcessMapper processMapper;
    @Autowired private com.itheima.mes1.module.inventory.mapper.StockAlertMapper stockAlertMapper;
    @Autowired private OrderNotificationMapper orderNotificationMapper;

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

        // 全部销售额（累计）
        List<SaleOrder> allOrders = saleOrderMapper.selectList(null);
        BigDecimal totalSalesAmount = BigDecimal.ZERO;
        for (SaleOrder o : allOrders) {
            if (o.getTotalAmount() != null) totalSalesAmount = totalSalesAmount.add(o.getTotalAmount());
        }
        long totalOrders = allOrders.size();

        // 本月新增订单（环比用）
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        long monthOrders = saleOrderMapper.selectCount(
                new LambdaQueryWrapper<SaleOrder>()
                        .ge(SaleOrder::getCreateTime, monthStart));
        long prevMonthOrders = saleOrderMapper.selectCount(
                new LambdaQueryWrapper<SaleOrder>()
                        .ge(SaleOrder::getCreateTime, monthStart.minusMonths(1))
                        .lt(SaleOrder::getCreateTime, monthStart));
        int orderGrowth = prevMonthOrders > 0
                ? (int) Math.round((monthOrders - prevMonthOrders) * 100.0 / prevMonthOrders)
                : (monthOrders > 0 ? 100 : 0);

        BigDecimal monthSales = BigDecimal.ZERO;
        for (SaleOrder o : saleOrderMapper.selectList(
                new LambdaQueryWrapper<SaleOrder>().ge(SaleOrder::getCreateTime, monthStart))) {
            if (o.getTotalAmount() != null) monthSales = monthSales.add(o.getTotalAmount());
        }
        BigDecimal prevMonthSales = BigDecimal.ZERO;
        for (SaleOrder o : saleOrderMapper.selectList(
                new LambdaQueryWrapper<SaleOrder>()
                        .ge(SaleOrder::getCreateTime, monthStart.minusMonths(1))
                        .lt(SaleOrder::getCreateTime, monthStart))) {
            if (o.getTotalAmount() != null) prevMonthSales = prevMonthSales.add(o.getTotalAmount());
        }
        int salesGrowth = prevMonthSales.compareTo(BigDecimal.ZERO) > 0
                ? monthSales.subtract(prevMonthSales).multiply(new BigDecimal("100")).divide(prevMonthSales, 0, RoundingMode.HALF_UP).intValue()
                : (monthSales.compareTo(BigDecimal.ZERO) > 0 ? 100 : 0);

        // 仓库维度指标（含真实周转数据）
        List<Map<String, Object>> warehouseMetrics = new ArrayList<>();
        List<Map<String, Object>> whStructure = inventoryMapper.selectWarehouseStructure();
        // 近30天交易数据用于计算周转
        LocalDateTime days30Ago = LocalDate.now().minusDays(30).atStartOfDay();
        for (Map<String, Object> wh : whStructure) {
            Long warehouseId = ((Number) wh.get("warehouse_id")).longValue();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", wh.get("warehouse_name"));
            m.put("warehouseName", wh.get("warehouse_name"));
            BigDecimal stockQty = (BigDecimal) wh.get("total_quantity");
            m.put("stockQty", stockQty);
            m.put("skuCount", wh.get("sku_count"));
            // 当日入库量
            m.put("orderCount", totalOrders);

            // 近30天出库量（按仓库）
            BigDecimal whOutQty = transactionMapper.sumOutboundByWarehouse(warehouseId, days30Ago);
            m.put("outputQty", whOutQty != null ? whOutQty : BigDecimal.ZERO);

            // 周转天数 = 平均库存 / (出库量/30)
            BigDecimal avgOutPerDay = (whOutQty != null && whOutQty.compareTo(BigDecimal.ZERO) > 0)
                    ? whOutQty.divide(new BigDecimal("30"), 4, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            BigDecimal turnoverDays = avgOutPerDay.compareTo(BigDecimal.ZERO) > 0
                    ? stockQty.divide(avgOutPerDay, 1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            m.put("turnoverDays", turnoverDays);

            // 健康度评分: 周转天数越短越好
            int healthScore = 75;
            if (turnoverDays.compareTo(BigDecimal.ZERO) > 0) {
                if (turnoverDays.compareTo(new BigDecimal("15")) <= 0) healthScore = 90;
                else if (turnoverDays.compareTo(new BigDecimal("30")) <= 0) healthScore = 75;
                else if (turnoverDays.compareTo(new BigDecimal("60")) <= 0) healthScore = 60;
                else healthScore = 40;
            }
            m.put("healthScore", healthScore);
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

        // 低库存预警：直接查库存表，≤阈值的产品
        List<Map<String, Object>> lowStockProducts = new ArrayList<>();
        List<Inventory> allInv = inventoryMapper.selectAllWithDetail();
        BigDecimal threshold = new BigDecimal("10");
        for (Inventory inv : allInv) {
            if (inv.getQuantity() != null && inv.getQuantity().compareTo(threshold) <= 0) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("productId", inv.getProductId());
                item.put("productName", inv.getProductName() != null ? inv.getProductName() : "产品#" + inv.getProductId());
                item.put("productCode", inv.getProductCode());
                item.put("quantity", inv.getQuantity());
                item.put("warehouseName", inv.getWarehouseName());
                lowStockProducts.add(item);
            }
        }
        data.put("lowStockProducts", lowStockProducts);
        data.put("lowStockCount", lowStockProducts.size());

        // 未处理预警数
        long unresolvedAlerts = stockAlertMapper.selectCount(
                new LambdaQueryWrapper<com.itheima.mes1.module.inventory.entity.StockAlert>()
                        .eq(com.itheima.mes1.module.inventory.entity.StockAlert::getStatus, 0));
        data.put("unresolvedAlerts", unresolvedAlerts);
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
        data.put("outboundQty", outQty);       // 兼容 BiOverview
        data.put("totalOutbound", outQty);     // 兼容 Dashboard
        data.put("inboundQty", inQty);
        data.put("avgStock", avgStock);        // 兼容 BiOverview
        data.put("avgInventory", avgStock);    // 兼容 Dashboard
        data.put("turnoverRate", turnoverRate);
        data.put("turnoverDays", turnoverDays);
        data.put("daysGrowth", 0); // 环比变化（简化处理）
        return data;
    }

    // ==================== 库存结构分析 ====================

    public Map<String, Object> inventoryStructure() {
        // 按仓库分布（原始 SQL 返回下划线 key）
        List<Map<String, Object>> rawRows = inventoryMapper.selectWarehouseStructure();

        // 总数汇总 + 转换为前端期望的驼峰 key
        long totalSku = 0;
        BigDecimal totalQty = BigDecimal.ZERO;
        List<Map<String, Object>> warehouses = new ArrayList<>();
        for (Map<String, Object> row : rawRows) {
            totalSku += ((Number) row.get("sku_count")).longValue();
            totalQty = totalQty.add((BigDecimal) row.get("total_quantity"));
            Map<String, Object> w = new LinkedHashMap<>();
            w.put("name", row.get("warehouse_name"));
            w.put("warehouseName", row.get("warehouse_name"));
            w.put("quantity", row.get("total_quantity"));
            w.put("skuCount", row.get("sku_count"));
            w.put("warehouseId", row.get("warehouse_id"));
            warehouses.add(w);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalSku", totalSku);
        data.put("totalQuantity", totalQty);
        data.put("byWarehouse", rawRows);    // 兼容旧调用
        data.put("warehouses", warehouses);  // Dashboard / BiOverview 通用
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

    // ==================== MES 车间驾驶舱 ====================

    public Map<String, Object> mesSummary() {
        Map<String, Object> data = new LinkedHashMap<>();

        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();

        // KPI 卡片
        long inProgressOrders = workOrderMapper.selectCount(
                new LambdaQueryWrapper<WorkOrder>().eq(WorkOrder::getStatus, 2));
        long pendingOrders = workOrderMapper.selectCount(
                new LambdaQueryWrapper<WorkOrder>().eq(WorkOrder::getStatus, 1));

        // 今日报工汇总
        List<com.itheima.mes1.module.production.entity.WorkReport> todayReports =
                workReportMapper.selectList(
                        new LambdaQueryWrapper<com.itheima.mes1.module.production.entity.WorkReport>()
                                .ge(com.itheima.mes1.module.production.entity.WorkReport::getCreateTime, todayStart));
        BigDecimal todayOutput = BigDecimal.ZERO;
        BigDecimal todayDefect = BigDecimal.ZERO;
        for (var r : todayReports) {
            if (r.getQuantity() != null) todayOutput = todayOutput.add(r.getQuantity());
            if (r.getScrapQty() != null) todayDefect = todayDefect.add(r.getScrapQty());
        }
        BigDecimal defectRate = todayOutput.compareTo(BigDecimal.ZERO) > 0
                ? todayDefect.divide(todayOutput, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        data.put("inProgressOrders", inProgressOrders);
        data.put("pendingOrders", pendingOrders);
        data.put("todayOutput", todayOutput);
        data.put("todayDefect", todayDefect);
        data.put("defectRate", defectRate.setScale(1, RoundingMode.HALF_UP));

        // 生产进度（进行中工单）
        List<WorkOrder> activeOrders = workOrderMapper.selectList(
                new LambdaQueryWrapper<WorkOrder>().in(WorkOrder::getStatus, 1, 2));
        List<Map<String, Object>> orderProgress = new ArrayList<>();
        for (WorkOrder wo : activeOrders) {
            WorkOrder full = workOrderMapper.selectWithProduct(wo.getId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("orderNo", wo.getOrderNo());
            item.put("productName", full != null ? full.getProductName() : "");
            item.put("quantity", wo.getQuantity());
            item.put("finishedQty", wo.getFinishedQty());
            item.put("status", wo.getStatus());
            item.put("progress", computeProgress(wo.getQuantity(), wo.getFinishedQty()));
            item.put("planEnd", wo.getPlanEnd());
            orderProgress.add(item);
        }
        data.put("orderProgress", orderProgress);

        // 不良原因分布（最近30天）
        LocalDate days30Ago = today.minusDays(30);
        List<com.itheima.mes1.module.production.entity.QcRecord> qcRecords =
                qcRecordMapper.selectList(
                        new LambdaQueryWrapper<com.itheima.mes1.module.production.entity.QcRecord>()
                                .ge(com.itheima.mes1.module.production.entity.QcRecord::getCheckDate, days30Ago)
                                .le(com.itheima.mes1.module.production.entity.QcRecord::getCheckDate, today));
        Map<String, Long> causeCount = new LinkedHashMap<>();
        for (var qc : qcRecords) {
            if (qc.getNgDescription() != null && !qc.getNgDescription().isBlank()) {
                causeCount.merge(qc.getNgDescription(), 1L, Long::sum);
            }
        }
        List<Map<String, Object>> defectCauseList = new ArrayList<>();
        causeCount.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(10)
                .forEach(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("cause", e.getKey());
                    m.put("count", e.getValue());
                    defectCauseList.add(m);
                });
        data.put("defectCauseList", defectCauseList);

        // 产量趋势（最近7天）
        List<Map<String, Object>> productionTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            LocalDateTime dayStart = d.atStartOfDay();
            LocalDateTime dayEnd = d.plusDays(1).atStartOfDay();
            List<com.itheima.mes1.module.production.entity.WorkReport> dayReports =
                    workReportMapper.selectList(
                            new LambdaQueryWrapper<com.itheima.mes1.module.production.entity.WorkReport>()
                                    .ge(com.itheima.mes1.module.production.entity.WorkReport::getCreateTime, dayStart)
                                    .lt(com.itheima.mes1.module.production.entity.WorkReport::getCreateTime, dayEnd));
            BigDecimal dayTotal = BigDecimal.ZERO;
            for (var r : dayReports) {
                if (r.getQuantity() != null) dayTotal = dayTotal.add(r.getQuantity());
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("date", d.toString());
            m.put("output", dayTotal);
            productionTrend.add(m);
        }
        data.put("productionTrend", productionTrend);

        // 最近报工记录（最近10条）
        List<com.itheima.mes1.module.production.entity.WorkReport> recentReports =
                workReportMapper.selectList(
                        new LambdaQueryWrapper<com.itheima.mes1.module.production.entity.WorkReport>()
                                .orderByDesc(com.itheima.mes1.module.production.entity.WorkReport::getCreateTime)
                                .last("LIMIT 10"));
        List<Map<String, Object>> recentReportList = new ArrayList<>();
        for (var r : recentReports) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", r.getId());
            item.put("workOrderId", r.getWorkOrderId());
            item.put("worker", r.getWorker());
            item.put("quantity", r.getQuantity());
            item.put("qualifiedQty", r.getQualifiedQty());
            item.put("scrapQty", r.getScrapQty());
            item.put("defectReason", r.getDefectReason());
            item.put("reportDate", r.getReportDate());
            if (r.getProductId() != null) {
                var p = productMapper.selectById(r.getProductId());
                item.put("productName", p != null ? p.getName() : "");
            }
            if (r.getProcessId() != null) {
                var proc = processMapper.selectById(r.getProcessId());
                item.put("processName", proc != null ? proc.getName() : "");
            }
            if (r.getWorkOrderId() != null) {
                var wo = workOrderMapper.selectById(r.getWorkOrderId());
                item.put("orderNo", wo != null ? wo.getOrderNo() : "");
            }
            recentReportList.add(item);
        }
        data.put("recentReportList", recentReportList);

        // 待处理工单
        List<WorkOrder> pendingOrdersList = workOrderMapper.selectList(
                new LambdaQueryWrapper<WorkOrder>().eq(WorkOrder::getStatus, 1)
                        .orderByDesc(WorkOrder::getCreateTime).last("LIMIT 10"));
        List<Map<String, Object>> pendingOrderList = new ArrayList<>();
        for (WorkOrder wo : pendingOrdersList) {
            WorkOrder full = workOrderMapper.selectWithProduct(wo.getId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", wo.getId());
            item.put("orderNo", wo.getOrderNo());
            item.put("productName", full != null ? full.getProductName() : "");
            item.put("quantity", wo.getQuantity());
            item.put("planStart", wo.getPlanStart());
            item.put("planEnd", wo.getPlanEnd());
            pendingOrderList.add(item);
        }
        data.put("pendingOrderList", pendingOrderList);

        // 低库存预警
        List<Map<String, Object>> lowStockProducts = new ArrayList<>();
        List<Inventory> allInv = inventoryMapper.selectAllWithDetail();
        BigDecimal threshold = new BigDecimal("10");
        for (Inventory inv : allInv) {
            if (inv.getQuantity() != null && inv.getQuantity().compareTo(threshold) <= 0) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("productId", inv.getProductId());
                item.put("productName", inv.getProductName() != null ? inv.getProductName() : "产品#" + inv.getProductId());
                item.put("productCode", inv.getProductCode());
                item.put("quantity", inv.getQuantity());
                item.put("warehouseName", inv.getWarehouseName());
                lowStockProducts.add(item);
            }
        }
        data.put("lowStockProducts", lowStockProducts);
        data.put("lowStockCount", lowStockProducts.size());
        data.put("unresolvedAlerts", stockAlertMapper.selectCount(
                new LambdaQueryWrapper<com.itheima.mes1.module.inventory.entity.StockAlert>()
                        .eq(com.itheima.mes1.module.inventory.entity.StockAlert::getStatus, 0)));

        // 未读订单支付通知
        long unreadNotifications = orderNotificationMapper.selectCount(
                new LambdaQueryWrapper<OrderNotification>().eq(OrderNotification::getIsRead, 0));
        data.put("unreadOrderNotifications", unreadNotifications);

        List<OrderNotification> recentNotifications = orderNotificationMapper.selectList(
                new LambdaQueryWrapper<OrderNotification>()
                        .eq(OrderNotification::getIsRead, 0)
                        .orderByDesc(OrderNotification::getCreateTime)
                        .last("LIMIT 5"));
        data.put("recentOrderNotifications", recentNotifications);

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