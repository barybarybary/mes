package com.itheima.mes1.module.production.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.mes1.common.PageResult;
import com.itheima.mes1.common.Result;
import com.itheima.mes1.common.annotation.RequirePermission;
import com.itheima.mes1.module.production.entity.WorkOrderProcess;
import com.itheima.mes1.module.production.entity.WorkReport;
import com.itheima.mes1.module.production.mapper.WorkOrderProcessMapper;
import com.itheima.mes1.module.production.mapper.WorkReportMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(name = "报工管理")
@RestController
@RequestMapping("/api/production/report")
public class WorkReportController {

    private final ServiceImpl<WorkReportMapper, WorkReport> service;
    private final WorkOrderProcessMapper processMapper;

    public WorkReportController(WorkReportMapper mapper, WorkOrderProcessMapper processMapper) {
        this.service = new ServiceImpl<>() {{ baseMapper = mapper; }};
        this.processMapper = processMapper;
    }

    @RequirePermission("production:report:list")
    @GetMapping
    public Result<PageResult<WorkReport>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long workOrderId) {
        LambdaQueryWrapper<WorkReport> w = new LambdaQueryWrapper<WorkReport>()
                .eq(workOrderId != null, WorkReport::getWorkOrderId, workOrderId)
                .orderByDesc(WorkReport::getCreateTime);
        Page<WorkReport> result = service.page(new Page<>(page, pageSize), w);
        result.setTotal(service.count(w));
        return Result.ok(new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @RequirePermission("production:report:add")
    @Operation(summary = "工序报工")
    @PostMapping
    public Result<?> report(@RequestBody WorkReport report) {
        service.save(report);

        // 更新工序进度
        if (report.getWorkOrderProcessId() != null) {
            WorkOrderProcess wp = processMapper.selectById(report.getWorkOrderProcessId());
            if (wp != null) {
                wp.setFinishedQty((wp.getFinishedQty() != null ? wp.getFinishedQty() : BigDecimal.ZERO)
                        .add(report.getQuantity()));
                wp.setQualifiedQty((wp.getQualifiedQty() != null ? wp.getQualifiedQty() : BigDecimal.ZERO)
                        .add(report.getQualifiedQty() != null ? report.getQualifiedQty() : BigDecimal.ZERO));
                wp.setScrapQty((wp.getScrapQty() != null ? wp.getScrapQty() : BigDecimal.ZERO)
                        .add(report.getScrapQty() != null ? report.getScrapQty() : BigDecimal.ZERO));
                wp.setStatus(2); // 加工中
                processMapper.updateById(wp);
            }
        }
        return Result.ok();
    }
}
