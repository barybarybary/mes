package com.itheima.mes1.module.dashboard.controller;

import com.itheima.mes1.common.Result;
import com.itheima.mes1.common.annotation.RequirePermission;
import com.itheima.mes1.module.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "报表大屏")
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

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

    // ==================== 数据大屏 ====================

    @RequirePermission("dashboard:view")
    @Operation(summary = "大屏实时数据")
    @GetMapping("/big-screen")
    public Result<Map<String, Object>> bigScreen() {
        return Result.ok(dashboardService.bigScreen());
    }
}