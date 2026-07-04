package com.itheima.mes1.module.bi.controller;

import com.itheima.mes1.common.Result;
import com.itheima.mes1.common.annotation.RequirePermission;
import com.itheima.mes1.module.bi.service.BiOverviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "BI经营分析")
@RestController
@RequestMapping("/api/bi")
public class BiOverviewController {

    @Autowired
    private BiOverviewService overviewService;

    @RequirePermission("bi:view")
    @Operation(summary = "经营概览卡片")
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.ok(overviewService.overview());
    }

    @RequirePermission("bi:view")
    @Operation(summary = "月度销售趋势含同环比")
    @GetMapping("/monthly-trend")
    public Result<List<Map<String, Object>>> monthlyTrend(@RequestParam(defaultValue = "12") int months) {
        return Result.ok(overviewService.monthlyTrend(months));
    }

    @RequirePermission("bi:view")
    @Operation(summary = "产品毛利排行")
    @GetMapping("/product-profit")
    public Result<List<Map<String, Object>>> productProfit(@RequestParam(defaultValue = "20") int limit) {
        return Result.ok(overviewService.productProfit(limit));
    }

    @RequirePermission("bi:view")
    @Operation(summary = "客户价值排行")
    @GetMapping("/customer-value")
    public Result<List<Map<String, Object>>> customerValue(@RequestParam(defaultValue = "20") int limit) {
        return Result.ok(overviewService.customerValue(limit));
    }
}