package com.itheima.mes1.module.dashboard.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.mes1.common.Result;
import com.itheima.mes1.common.annotation.RequirePermission;
import com.itheima.mes1.module.dashboard.entity.OrderNotification;
import com.itheima.mes1.module.dashboard.mapper.OrderNotificationMapper;
import com.itheima.mes1.module.dashboard.service.DashboardService;
import com.itheima.mes1.module.inventory.entity.StockAlert;
import com.itheima.mes1.module.inventory.mapper.StockAlertMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Tag(name = "报表大屏")
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;
    @Autowired
    private StockAlertMapper stockAlertMapper;
    @Autowired
    private OrderNotificationMapper orderNotificationMapper;

    // ==================== 报表驾驶舱 ====================

    @RequirePermission("dashboard:view")
    @Operation(summary = "首页概览卡片")
    @GetMapping("/summary")
    public Result<Map<String, Object>> summary() {
        return Result.ok(dashboardService.summary());
    }

    @RequirePermission("dashboard:view")
    @Operation(summary = "销售趋势（折线图）")
    @GetMapping("/sales-trend")
    public Result<List<Map<String, Object>>> salesTrend(@RequestParam(defaultValue = "30") int days) {
        return Result.ok(dashboardService.salesTrend(days));
    }

    @RequirePermission("dashboard:view")
    @Operation(summary = "生产进度概览")
    @GetMapping("/production-progress")
    public Result<List<Map<String, Object>>> productionProgress() {
        return Result.ok(dashboardService.productionProgress());
    }

    @RequirePermission("dashboard:view")
    @Operation(summary = "订单交付率")
    @GetMapping("/delivery-rate")
    public Result<Map<String, Object>> deliveryRate() {
        return Result.ok(dashboardService.deliveryRate());
    }

    @RequirePermission("dashboard:view")
    @Operation(summary = "库存周转率")
    @GetMapping("/inventory-turnover")
    public Result<Map<String, Object>> inventoryTurnover(@RequestParam(defaultValue = "30") int days) {
        return Result.ok(dashboardService.inventoryTurnover(days));
    }

    @RequirePermission("dashboard:view")
    @Operation(summary = "库存结构分析")
    @GetMapping("/inventory-structure")
    public Result<Map<String, Object>> inventoryStructure() {
        return Result.ok(dashboardService.inventoryStructure());
    }

    @RequirePermission("dashboard:view")
    @Operation(summary = "销售排行（产品+客户）")
    @GetMapping("/sales-ranking")
    public Result<Map<String, Object>> salesRanking(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "10") int limit) {
        return Result.ok(dashboardService.salesRanking(days, limit));
    }

    // ==================== MES 车间驾驶舱 ====================

    @RequirePermission("dashboard:view")
    @Operation(summary = "MES车间驾驶舱摘要")
    @GetMapping("/mes-summary")
    public Result<Map<String, Object>> mesSummary() {
        return Result.ok(dashboardService.mesSummary());
    }

    // ==================== 数据大屏 ====================

    @RequirePermission("dashboard:view")
    @Operation(summary = "大屏实时数据")
    @GetMapping("/big-screen")
    public Result<Map<String, Object>> bigScreen() {
        return Result.ok(dashboardService.bigScreen());
    }

    // ==================== 库存预警 ====================

    @RequirePermission("dashboard:view")
    @Operation(summary = "未处理预警列表")
    @GetMapping("/alerts")
    public Result<List<StockAlert>> alerts() {
        return Result.ok(stockAlertMapper.selectList(
                new LambdaQueryWrapper<StockAlert>()
                        .eq(StockAlert::getStatus, 0)
                        .orderByDesc(StockAlert::getCreateTime)));
    }

    @RequirePermission("dashboard:view")
    @Operation(summary = "标记预警已处理")
    @PutMapping("/alerts/{id}/resolve")
    public Result<?> resolveAlert(@PathVariable Long id) {
        StockAlert alert = stockAlertMapper.selectById(id);
        if (alert != null) {
            alert.setStatus(1);
            alert.setResolveTime(LocalDateTime.now());
            stockAlertMapper.updateById(alert);
        }
        return Result.ok();
    }

    // ==================== 订单支付通知 ====================

    @RequirePermission("dashboard:view")
    @Operation(summary = "未读订单通知列表")
    @GetMapping("/order-notifications")
    public Result<List<OrderNotification>> orderNotifications() {
        return Result.ok(orderNotificationMapper.selectList(
                new LambdaQueryWrapper<OrderNotification>()
                        .eq(OrderNotification::getIsRead, 0)
                        .orderByDesc(OrderNotification::getCreateTime)));
    }

    @RequirePermission("dashboard:view")
    @Operation(summary = "标记通知已读")
    @PutMapping("/order-notifications/{id}/read")
    public Result<?> markRead(@PathVariable Long id) {
        OrderNotification notification = orderNotificationMapper.selectById(id);
        if (notification != null) {
            notification.setIsRead(1);
            orderNotificationMapper.updateById(notification);
        }
        return Result.ok();
    }

    @RequirePermission("dashboard:view")
    @Operation(summary = "全部标记已读")
    @PutMapping("/order-notifications/read-all")
    public Result<?> markAllRead() {
        List<OrderNotification> unread = orderNotificationMapper.selectList(
                new LambdaQueryWrapper<OrderNotification>().eq(OrderNotification::getIsRead, 0));
        for (OrderNotification n : unread) {
            n.setIsRead(1);
            orderNotificationMapper.updateById(n);
        }
        return Result.ok();
    }
}