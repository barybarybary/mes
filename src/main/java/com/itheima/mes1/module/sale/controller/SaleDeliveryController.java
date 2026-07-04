package com.itheima.mes1.module.sale.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.mes1.common.PageResult;
import com.itheima.mes1.common.Result;
import com.itheima.mes1.common.annotation.RequirePermission;
import com.itheima.mes1.module.sale.entity.Delivery;
import com.itheima.mes1.module.sale.service.SaleDeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "发货管理")
@RestController
@RequestMapping("/api/sale/delivery")
public class SaleDeliveryController {

    @Autowired
    private SaleDeliveryService deliveryService;

    @RequirePermission("sale:order:list")
    @Operation(summary = "发货单列表")
    @GetMapping
    public Result<PageResult<Delivery>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<Delivery> result = deliveryService.page(page, pageSize);
        return Result.ok(new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @RequirePermission("sale:order:list")
    @Operation(summary = "发货单详情")
    @GetMapping("/{id}")
    public Result<Delivery> getById(@PathVariable Long id) {
        return Result.ok(deliveryService.getDetail(id));
    }

    @RequirePermission("sale:order:add")
    @Operation(summary = "创建发货单")
    @PostMapping
    public Result<Delivery> create(@RequestBody Delivery delivery) {
        return Result.ok(deliveryService.create(delivery));
    }

    @RequirePermission("sale:order:edit")
    @Operation(summary = "编辑发货单")
    @PutMapping
    public Result<?> update(@RequestBody Delivery delivery) {
        deliveryService.update(delivery);
        return Result.ok();
    }

    @RequirePermission("sale:order:audit")
    @Operation(summary = "更新发货状态")
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        deliveryService.updateStatus(id, status);
        return Result.ok();
    }

    @RequirePermission("sale:order:delete")
    @Operation(summary = "删除发货单")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        deliveryService.delete(id);
        return Result.ok();
    }
}