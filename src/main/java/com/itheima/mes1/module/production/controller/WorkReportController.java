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
        // 填充工单号和工序名称
        WorkReportMapper mapper = (WorkReportMapper) service.getBaseMapper();
        for (WorkReport report : result.getRecords()) {
            WorkReport full = mapper.selectWithRelations(report.getId());
            if (full != null) {
                report.setWorkOrderNo(full.getWorkOrderNo());
                report.setProcessName(full.getProcessName());
            }
        }
        return Result.ok(new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @RequirePermission("production:report:add")
    @Operation(summary = "工序报工")
    @PostMapping
    public Result<?> report(@RequestBody WorkReport report) {
        // 默认值
        if (report.getReportType() == null) report.setReportType("NORMAL");
        if (report.getQualifiedQty() == null) report.setQualifiedQty(BigDecimal.ZERO);
        if (report.getScrapQty() == null) report.setScrapQty(BigDecimal.ZERO);
        if (report.getQuantity() == null) {
            report.setQuantity(report.getQualifiedQty().add(report.getScrapQty()));
        }
        service.save(report);

        // 更新工序进度
        if (report.getWorkOrderProcessId() != null) {
            WorkOrderProcess wp = processMapper.selectById(report.getWorkOrderProcessId());
            if (wp != null) {
                // 设置状态为加工中
                if (wp.getStatus() == null || wp.getStatus() == 1) {
                    wp.setStatus(2);
                    if (wp.getStartTime() == null) {
                        wp.setStartTime(java.time.LocalDateTime.now());
                    }
                }
                wp.setFinishedQty(safe(wp.getFinishedQty()).add(report.getQuantity()));
                wp.setQualifiedQty(safe(wp.getQualifiedQty()).add(report.getQualifiedQty()));
                wp.setScrapQty(safe(wp.getScrapQty()).add(report.getScrapQty()));
                // 工序完成判定：完成数 >= 计划数
                if (wp.getPlanQty() != null && wp.getFinishedQty().compareTo(wp.getPlanQty()) >= 0) {
                    wp.setStatus(3);
                    wp.setEndTime(java.time.LocalDateTime.now());
                }
                processMapper.updateById(wp);
            }
        }
        return Result.ok();
    }

    private BigDecimal safe(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }
}
