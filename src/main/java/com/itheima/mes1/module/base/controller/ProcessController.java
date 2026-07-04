package com.itheima.mes1.module.base.controller;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.mes1.common.Result;
import com.itheima.mes1.common.annotation.RequirePermission;
import com.itheima.mes1.module.base.entity.Process;
import com.itheima.mes1.module.base.mapper.ProcessMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "工序管理")
@RestController
@RequestMapping("/api/base/process")
public class ProcessController {

    private final ServiceImpl<ProcessMapper, Process> service;

    public ProcessController(ProcessMapper mapper) {
        this.service = new ServiceImpl<>() {{ baseMapper = mapper; }};
    }

    @RequirePermission("base:process:list")
    @GetMapping
    public Result<List<Process>> list() { return Result.ok(service.list()); }

    @RequirePermission("base:process:add")
    @PostMapping
    public Result<?> add(@RequestBody Process p) { service.save(p); return Result.ok(); }

    @RequirePermission("base:process:edit")
    @PutMapping
    public Result<?> update(@RequestBody Process p) { service.updateById(p); return Result.ok(); }

    @RequirePermission("base:process:delete")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) { service.removeById(id); return Result.ok(); }
}
