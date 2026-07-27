package com.itheima.mes1.module.base.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.mes1.common.Result;
import com.itheima.mes1.common.annotation.RequirePermission;
import com.itheima.mes1.module.base.entity.Warehouse;
import com.itheima.mes1.module.base.entity.WarehouseLocation;
import com.itheima.mes1.module.base.mapper.WarehouseLocationMapper;
import com.itheima.mes1.module.base.mapper.WarehouseMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "仓库管理")
@RestController
@RequestMapping("/api/base/warehouse")
public class WarehouseController {

    private final ServiceImpl<WarehouseMapper, Warehouse> service;
    private final WarehouseLocationMapper locationMapper;

    public WarehouseController(WarehouseMapper mapper, WarehouseLocationMapper locationMapper) {
        this.service = new ServiceImpl<>() {{ baseMapper = mapper; }};
        this.locationMapper = locationMapper;
    }

    @RequirePermission("base:warehouse:list")
    @GetMapping
    public Result<List<Warehouse>> list() { return Result.ok(service.list()); }

    @RequirePermission("base:warehouse:add")
    @PostMapping
    public Result<?> add(@RequestBody Warehouse w) { service.save(w); return Result.ok(); }

    @RequirePermission("base:warehouse:edit")
    @PutMapping
    public Result<?> update(@RequestBody Warehouse w) { service.updateById(w); return Result.ok(); }

    @RequirePermission("base:warehouse:delete")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) { service.removeById(id); return Result.ok(); }

    @RequirePermission("base:warehouse:list")
    @GetMapping("/{id}/locations")
    public Result<List<WarehouseLocation>> locations(@PathVariable Long id) {
        return Result.ok(locationMapper.selectList(
                new LambdaQueryWrapper<WarehouseLocation>().eq(WarehouseLocation::getWarehouseId, id)));
    }

    @RequirePermission("base:warehouse:edit")
    @PostMapping("/{id}/locations")
    public Result<?> addLocation(@PathVariable Long id, @RequestBody WarehouseLocation loc) {
        loc.setWarehouseId(id);
        locationMapper.insert(loc);
        return Result.ok();
    }

    @RequirePermission("base:warehouse:edit")
    @PutMapping("/locations/{id}")
    public Result<?> updateLocation(@PathVariable Long id, @RequestBody WarehouseLocation loc) {
        loc.setId(id);
        locationMapper.updateById(loc);
        return Result.ok();
    }

    @RequirePermission("base:warehouse:delete")
    @DeleteMapping("/locations/{id}")
    public Result<?> deleteLocation(@PathVariable Long id) {
        locationMapper.deleteById(id);
        return Result.ok();
    }
}
