package com.itheima.mes1.module.base.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.mes1.common.PageResult;
import com.itheima.mes1.common.Result;
import com.itheima.mes1.common.annotation.RequirePermission;
import com.itheima.mes1.module.base.entity.Supplier;
import com.itheima.mes1.module.base.mapper.SupplierMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "供应商管理")
@RestController
@RequestMapping("/api/base/supplier")
public class SupplierController {

    private final ServiceImpl<SupplierMapper, Supplier> service;

    public SupplierController(SupplierMapper mapper) {
        this.service = new ServiceImpl<>() {{ baseMapper = mapper; }};
    }

    @RequirePermission("base:supplier:list")
    @GetMapping
    public Result<PageResult<Supplier>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Supplier> w = new LambdaQueryWrapper<Supplier>()
                .like(StrUtil.isNotBlank(keyword), Supplier::getName, keyword)
                .or().like(StrUtil.isNotBlank(keyword), Supplier::getCode, keyword)
                .orderByDesc(Supplier::getCreateTime);
        Page<Supplier> result = service.page(new Page<>(page, pageSize), w);
        result.setTotal(service.count(w));
        return Result.ok(new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @RequirePermission("base:supplier:add")
    @PostMapping
    public Result<?> add(@RequestBody Supplier s) { service.save(s); return Result.ok(); }

    @RequirePermission("base:supplier:edit")
    @PutMapping
    public Result<?> update(@RequestBody Supplier s) { service.updateById(s); return Result.ok(); }

    @RequirePermission("base:supplier:delete")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) { service.removeById(id); return Result.ok(); }
}
