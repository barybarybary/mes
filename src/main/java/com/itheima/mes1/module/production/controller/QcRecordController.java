package com.itheima.mes1.module.production.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.mes1.common.PageResult;
import com.itheima.mes1.common.Result;
import com.itheima.mes1.common.annotation.RequirePermission;
import com.itheima.mes1.module.production.entity.QcRecord;
import com.itheima.mes1.module.production.mapper.QcRecordMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "质检管理")
@RestController
@RequestMapping("/api/production/qc")
public class QcRecordController {

    private final ServiceImpl<QcRecordMapper, QcRecord> service;

    public QcRecordController(QcRecordMapper mapper) {
        this.service = new ServiceImpl<>() {{ baseMapper = mapper; }};
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
        return Result.ok(new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @RequirePermission("production:qc:add")
    @PostMapping
    public Result<?> add(@RequestBody QcRecord qc) { service.save(qc); return Result.ok(); }

    @RequirePermission("production:qc:edit")
    @PutMapping
    public Result<?> update(@RequestBody QcRecord qc) { service.updateById(qc); return Result.ok(); }
}
