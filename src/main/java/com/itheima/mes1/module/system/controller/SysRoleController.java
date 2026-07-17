package com.itheima.mes1.module.system.controller;

import com.itheima.mes1.common.PageResult;
import com.itheima.mes1.common.Result;
import com.itheima.mes1.common.annotation.RequirePermission;
import com.itheima.mes1.module.system.entity.SysRole;
import com.itheima.mes1.module.system.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.itheima.mes1.module.system.mapper.SysRoleMenuMapper;
import com.itheima.mes1.module.system.mapper.SysUserRoleMapper;

import java.util.List;
import java.util.Map;

@Tag(name = "角色管理")
@RestController
@RequestMapping("/api/system/role")
public class SysRoleController {

    @Autowired
    private SysRoleService roleService;
    @Autowired
    private SysRoleMenuMapper roleMenuMapper;
    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @RequirePermission("system:role:list")
    @Operation(summary = "角色列表")
    @GetMapping
    public Result<List<SysRole>> list() {
        return Result.ok(roleService.list());
    }

    @RequirePermission("system:role:add")
    @Operation(summary = "新增角色")
    @PostMapping
    public Result<?> add(@RequestBody SysRole role) {
        roleService.save(role);
        return Result.ok();
    }

    @RequirePermission("system:role:edit")
    @Operation(summary = "修改角色")
    @PutMapping
    public Result<?> update(@RequestBody SysRole role) {
        roleService.updateById(role);
        return Result.ok();
    }

    @RequirePermission("system:role:delete")
    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        // 清理关联数据
        roleMenuMapper.deleteByRoleId(id);
        userRoleMapper.deleteByRoleId(id);
        roleService.removeById(id);
        return Result.ok();
    }

    @RequirePermission("system:role:list")
    @Operation(summary = "分配菜单")
    @PostMapping("/{id}/menus")
    public Result<?> assignMenus(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        List<Long> menuIds = body.get("menuIds");
        roleService.assignMenus(id, menuIds != null ? menuIds.toArray(new Long[0]) : null);
        return Result.ok();
    }
}
