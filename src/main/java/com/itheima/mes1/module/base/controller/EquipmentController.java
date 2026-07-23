package com.itheima.mes1.module.base.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.mes1.common.PageResult;
import com.itheima.mes1.common.Result;
import com.itheima.mes1.common.annotation.RequirePermission;
import com.itheima.mes1.module.base.entity.Equipment;
import com.itheima.mes1.module.base.mapper.EquipmentMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "设备管理")
@RestController
@RequestMapping("/api/base/equipment")
public class EquipmentController {

    private final ServiceImpl<EquipmentMapper, Equipment> service;

    public EquipmentController(EquipmentMapper mapper) {
        this.service = new ServiceImpl<>() {{ baseMapper = mapper; }};
    }

    @RequirePermission("base:equipment:list")
    @GetMapping
    public Result<PageResult<Equipment>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Equipment> w = new LambdaQueryWrapper<Equipment>()
                .like(StrUtil.isNotBlank(keyword), Equipment::getName, keyword)
                .or().like(StrUtil.isNotBlank(keyword), Equipment::getCode, keyword)
                .orderByDesc(Equipment::getCreateTime);
        Page<Equipment> result = service.page(new Page<>(page, pageSize), w);
        result.setTotal(service.count(w));
        return Result.ok(new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @RequirePermission("base:equipment:list")
    @GetMapping("/all")
    public Result<List<Equipment>> all() {
        return Result.ok(service.list(new LambdaQueryWrapper<Equipment>()
                .eq(Equipment::getStatus, "ACTIVE").orderByAsc(Equipment::getCode)));
    }

    @RequirePermission("base:equipment:add")
    @PostMapping
    public Result<?> add(@RequestBody Equipment e) { service.save(e); return Result.ok(); }

    @RequirePermission("base:equipment:edit")
    @PutMapping
    public Result<?> update(@RequestBody Equipment e) { service.updateById(e); return Result.ok(); }

    @RequirePermission("base:equipment:delete")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) { service.removeById(id); return Result.ok(); }
}
