package com.itheima.mes1.module.production.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.mes1.common.PageResult;
import com.itheima.mes1.common.Result;
import com.itheima.mes1.common.annotation.RequirePermission;
import com.itheima.mes1.module.production.entity.WorkOrder;
import com.itheima.mes1.module.production.mapper.WorkOrderMapper;
import com.itheima.mes1.module.production.service.WorkOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "生产工单")
@RestController
@RequestMapping("/api/production/work-order")
public class WorkOrderController {

    @Autowired
    private WorkOrderService workOrderService;
    @Autowired
    private WorkOrderMapper workOrderMapper;

    @RequirePermission("production:work-order:list")
    @GetMapping
    public Result<PageResult<WorkOrder>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status) {
        Page<WorkOrder> result = workOrderService.page(page, pageSize, status);
        return Result.ok(new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @RequirePermission("production:work-order:list")
    @GetMapping("/{id}")
    public Result<WorkOrder> getById(@PathVariable Long id) {
        return Result.ok(workOrderService.getDetail(id));
    }

    @RequirePermission("production:work-order:add")
    @PostMapping
    public Result<WorkOrder> create(@RequestBody WorkOrder wo) {
        return Result.ok(workOrderService.create(wo));
    }

    @RequirePermission("production:work-order:start")
    @Operation(summary = "开工")
    @PutMapping("/{id}/start")
    public Result<?> start(@PathVariable Long id) { workOrderService.startWork(id); return Result.ok(); }

    @RequirePermission("production:work-order:complete")
    @Operation(summary = "完工")
    @PutMapping("/{id}/complete")
    public Result<?> complete(@PathVariable Long id) { workOrderService.complete(id); return Result.ok(); }

    @RequirePermission("production:work-order:stock-in")
    @Operation(summary = "完工入库")
    @PutMapping("/{id}/stock-in")
    public Result<?> stockIn(@PathVariable Long id) { workOrderService.finishAndStockIn(id); return Result.ok(); }

    @RequirePermission("production:work-order:delete")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) { workOrderMapper.deleteById(id); return Result.ok(); }
}
