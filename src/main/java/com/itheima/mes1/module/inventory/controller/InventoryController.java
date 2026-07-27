package com.itheima.mes1.module.inventory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.mes1.common.PageResult;
import com.itheima.mes1.common.Result;
import com.itheima.mes1.common.annotation.RequirePermission;
import com.itheima.mes1.module.inventory.entity.Inventory;
import com.itheima.mes1.module.inventory.entity.InventoryTransaction;
import com.itheima.mes1.module.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Tag(name = "库存管理")
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @RequirePermission("inventory:list")
    @Operation(summary = "库存查询")
    @GetMapping
    public Result<PageResult<Inventory>> list(
            @RequestParam(required = false) Long productId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<Inventory> result = inventoryService.pageStocks(page, pageSize, productId);
        return Result.ok(new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @RequirePermission("inventory:transaction:list")
    @Operation(summary = "库存流水")
    @GetMapping("/transactions")
    public Result<PageResult<InventoryTransaction>> transactions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long productId) {
        Page<InventoryTransaction> result = inventoryService.pageTransactions(page, pageSize, productId);
        return Result.ok(new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @RequirePermission("inventory:adjust")
    @Operation(summary = "入库")
    @PostMapping("/in")
    public Result<?> stockIn(@RequestBody Map<String, Object> body) {
        inventoryService.stockIn(
                Long.valueOf(body.get("productId").toString()),
                Long.valueOf(body.get("warehouseId").toString()),
                body.get("locationId") != null ? Long.valueOf(body.get("locationId").toString()) : null,
                (String) body.get("batchNo"),
                new BigDecimal(body.get("quantity").toString()),
                (String) body.getOrDefault("type", "in"),
                (String) body.get("orderNo"),
                (String) body.get("remark")
        );
        return Result.ok();
    }

    @RequirePermission("inventory:adjust")
    @Operation(summary = "调拨（仓库间转移）")
    @PostMapping("/transfer")
    public Result<?> transfer(@RequestBody Map<String, Object> body) {
        inventoryService.transfer(
                Long.valueOf(body.get("productId").toString()),
                Long.valueOf(body.get("fromWarehouseId").toString()),
                Long.valueOf(body.get("toWarehouseId").toString()),
                (String) body.get("batchNo"),
                new BigDecimal(body.get("quantity").toString()),
                (String) body.get("remark")
        );
        return Result.ok();
    }

    @RequirePermission("inventory:adjust")
    @Operation(summary = "出库")
    @PostMapping("/out")
    public Result<?> stockOut(@RequestBody Map<String, Object> body) {
        inventoryService.stockOut(
                Long.valueOf(body.get("productId").toString()),
                Long.valueOf(body.get("warehouseId").toString()),
                (String) body.get("batchNo"),
                new BigDecimal(body.get("quantity").toString()),
                (String) body.getOrDefault("type", "out"),
                (String) body.get("orderNo"),
                (String) body.get("remark")
        );
        return Result.ok();
    }
}
