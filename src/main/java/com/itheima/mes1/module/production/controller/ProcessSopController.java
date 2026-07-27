package com.itheima.mes1.module.production.controller;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.mes1.common.Result;
import com.itheima.mes1.common.annotation.RequirePermission;
import com.itheima.mes1.module.production.entity.ProcessSop;
import com.itheima.mes1.module.production.mapper.ProcessSopMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "工序SOP")
@RestController
@RequestMapping("/api/production/process-sop")
public class ProcessSopController {

    private final ServiceImpl<ProcessSopMapper, ProcessSop> service;
    private final ProcessSopMapper mapper;

    public ProcessSopController(ProcessSopMapper mapper) {
        this.mapper = mapper;
        this.service = new ServiceImpl<>() {{ baseMapper = mapper; }};
    }

    @RequirePermission("production:work-order:list")
    @Operation(summary = "工序SOP列表")
    @GetMapping
    public Result<List<ProcessSop>> list(@RequestParam(required = false) Long processId) {
        if (processId != null) {
            return Result.ok(mapper.selectByProcessId(processId));
        }
        return Result.ok(mapper.selectWithNames());
    }

    @RequirePermission("production:work-order:edit")
    @Operation(summary = "新增工序SOP关联")
    @PostMapping
    public Result<?> add(@RequestBody ProcessSop ps) {
        service.save(ps);
        return Result.ok();
    }

    @RequirePermission("production:work-order:edit")
    @Operation(summary = "更新工序SOP关联")
    @PutMapping
    public Result<?> update(@RequestBody ProcessSop ps) {
        service.updateById(ps);
        return Result.ok();
    }

    @RequirePermission("production:work-order:delete")
    @Operation(summary = "删除工序SOP关联")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }
}
