package com.itheima.mes1.module.sale.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.mes1.common.PageResult;
import com.itheima.mes1.common.Result;
import com.itheima.mes1.common.annotation.RequirePermission;
import com.itheima.mes1.module.sale.dto.SaleOrderCreateReq;
import com.itheima.mes1.module.sale.dto.SaleOrderUpdateReq;
import com.itheima.mes1.module.sale.service.SaleOrderService;
import com.itheima.mes1.module.sale.vo.SaleOrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "销售订单")
@RestController
@RequestMapping("/api/sale/order")
public class SaleOrderController {

    @Autowired
    private SaleOrderService orderService;

    @RequirePermission("sale:order:list")
    @GetMapping
    public Result<PageResult<SaleOrderVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        Page<SaleOrderVO> result = orderService.page(page, pageSize, status, keyword);
        return Result.ok(new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @RequirePermission("sale:order:list")
    @GetMapping("/{id}")
    public Result<SaleOrderVO> getById(@PathVariable Long id) {
        return Result.ok(orderService.getDetail(id));
    }

    @RequirePermission("sale:order:add")
    @PostMapping
    public Result<SaleOrderVO> create(@RequestBody SaleOrderCreateReq req) {
        return Result.ok(orderService.create(req));
    }

    @RequirePermission("sale:order:edit")
    @PutMapping
    public Result<?> update(@RequestBody SaleOrderUpdateReq req) {
        orderService.update(req);
        return Result.ok();
    }

    @RequirePermission("sale:order:audit")
    @Operation(summary = "审核/状态变更")
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        orderService.updateStatus(id, status);
        return Result.ok();
    }

    @RequirePermission("sale:order:delete")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        orderService.delete(id);
        return Result.ok();
    }
}
