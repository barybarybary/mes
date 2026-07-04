package com.itheima.mes1.module.system.controller;

import com.itheima.mes1.common.Result;
import com.itheima.mes1.common.annotation.RequirePermission;
import com.itheima.mes1.module.system.entity.SysMenu;
import com.itheima.mes1.module.system.service.SysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "菜单管理")
@RestController
@RequestMapping("/api/system/menu")
public class SysMenuController {

    @Autowired
    private SysMenuService menuService;

    @RequirePermission("system:menu:list")
    @Operation(summary = "菜单树")
    @GetMapping("/tree")
    public Result<List<SysMenu>> tree() {
        return Result.ok(menuService.listTree());
    }

    @RequirePermission("system:menu:list")
    @Operation(summary = "菜单列表")
    @GetMapping
    public Result<List<SysMenu>> list() {
        return Result.ok(menuService.list());
    }

    @RequirePermission("system:menu:add")
    @Operation(summary = "新增菜单")
    @PostMapping
    public Result<?> add(@RequestBody SysMenu menu) {
        menuService.save(menu);
        return Result.ok();
    }

    @RequirePermission("system:menu:edit")
    @Operation(summary = "修改菜单")
    @PutMapping
    public Result<?> update(@RequestBody SysMenu menu) {
        menuService.updateById(menu);
        return Result.ok();
    }

    @RequirePermission("system:menu:delete")
    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        menuService.removeById(id);
        return Result.ok();
    }
}
