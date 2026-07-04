package com.itheima.mes1.module.bi.controller;

import com.itheima.mes1.common.Result;
import com.itheima.mes1.common.annotation.RequirePermission;
import com.itheima.mes1.module.bi.service.BiPivotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "BI多维交叉")
@RestController
@RequestMapping("/api/bi/pivot")
public class BiPivotController {

    @Autowired
    private BiPivotService pivotService;

    @RequirePermission("bi:view")
    @Operation(summary = "时间×产品→销售额")
    @GetMapping("/sales-by-product")
    public Result<List<Map<String, Object>>> salesByProduct(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return Result.ok(pivotService.salesByProduct(year, month));
    }

    @RequirePermission("bi:view")
    @Operation(summary = "时间×客户→销售额")
    @GetMapping("/sales-by-customer")
    public Result<List<Map<String, Object>>> salesByCustomer(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return Result.ok(pivotService.salesByCustomer(year, month));
    }

    @RequirePermission("bi:view")
    @Operation(summary = "月份×产品类别→销售额矩阵")
    @GetMapping("/sales-by-month-category")
    public Result<List<Map<String, Object>>> salesByMonthCategory(
            @RequestParam(required = false) Integer year) {
        return Result.ok(pivotService.salesByMonthCategory(year));
    }

    @RequirePermission("bi:view")
    @Operation(summary = "仓库×产品类别→库存量")
    @GetMapping("/inventory-by-warehouse")
    public Result<List<Map<String, Object>>> inventoryByWarehouse() {
        return Result.ok(pivotService.inventoryByWarehouse());
    }

    @RequirePermission("bi:view")
    @Operation(summary = "月份×产品→产量")
    @GetMapping("/production-by-month")
    public Result<List<Map<String, Object>>> productionByMonth(
            @RequestParam(required = false) Integer year) {
        return Result.ok(pivotService.productionByMonth(year));
    }

    @RequirePermission("bi:view")
    @Operation(summary = "客户×月份→发货量")
    @GetMapping("/delivery-by-customer")
    public Result<List<Map<String, Object>>> deliveryByCustomer(
            @RequestParam(required = false) Integer year) {
        return Result.ok(pivotService.deliveryByCustomer(year));
    }
}