package com.itheima.mes1.module.production.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.mes1.common.PageResult;
import com.itheima.mes1.common.Result;
import com.itheima.mes1.common.annotation.RequirePermission;
import com.itheima.mes1.module.production.entity.QcRecord;
import com.itheima.mes1.module.production.entity.WorkOrder;
import com.itheima.mes1.module.production.mapper.QcRecordMapper;
import com.itheima.mes1.module.production.mapper.WorkOrderMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "质检管理")
@RestController
@RequestMapping("/api/production/qc")
public class QcRecordController {

    private final ServiceImpl<QcRecordMapper, QcRecord> service;
    private final WorkOrderMapper workOrderMapper;
    private final QcRecordMapper qcRecordMapper;

    public QcRecordController(QcRecordMapper mapper, WorkOrderMapper workOrderMapper) {
        this.qcRecordMapper = mapper;
        this.service = new ServiceImpl<>() {{ baseMapper = mapper; }};
        this.workOrderMapper = workOrderMapper;
    }

    @RequirePermission("production:qc:list")
    @GetMapping
    public Result<PageResult<QcRecord>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long workOrderId,
            @RequestParam(required = false) String type) {
        LambdaQueryWrapper<QcRecord> w = new LambdaQueryWrapper<QcRecord>()
                .eq(workOrderId != null, QcRecord::getWorkOrderId, workOrderId)
                .eq(type != null, QcRecord::getType, type)
                .orderByDesc(QcRecord::getCreateTime);
        Page<QcRecord> result = service.page(new Page<>(page, pageSize), w);
        result.setTotal(service.count(w));

        // 批量填充工序名称
        List<Long> processIds = result.getRecords().stream()
                .map(QcRecord::getWorkOrderProcessId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (!processIds.isEmpty()) {
            Map<Long, String> nameMap = qcRecordMapper.selectProcessNames(processIds).stream()
                    .collect(Collectors.toMap(
                            m -> (Long) m.get("id"),
                            m -> (String) m.get("name"),
                            (a, b) -> a));
            result.getRecords().forEach(r -> {
                if (r.getWorkOrderProcessId() != null) {
                    r.setProcessName(nameMap.get(r.getWorkOrderProcessId()));
                }
            });
        }

        return Result.ok(new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @RequirePermission("production:qc:add")
    @PostMapping
    public Result<?> add(@RequestBody QcRecord qc) {
        service.save(qc);
        // 质检不合格 → 联动标记工单
        if (qc.getResult() != null && qc.getResult() == 0 && qc.getWorkOrderId() != null) {
            WorkOrder wo = workOrderMapper.selectById(qc.getWorkOrderId());
            if (wo != null && wo.getStatus() == 2) {
                String prefix = wo.getRemark() != null ? wo.getRemark() + "; " : "";
                wo.setRemark(prefix + "工序" + qc.getProcessName() + "质检NG:" + qc.getNgDescription());
                workOrderMapper.updateById(wo);
            }
        }
        return Result.ok();
    }

    @RequirePermission("production:qc:edit")
    @PutMapping
    public Result<?> update(@RequestBody QcRecord qc) { service.updateById(qc); return Result.ok(); }
}
