package com.itheima.mes1.module.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.mes1.common.PageResult;
import com.itheima.mes1.common.Result;
import com.itheima.mes1.common.annotation.RequirePermission;
import com.itheima.mes1.module.system.entity.SysRole;
import com.itheima.mes1.module.system.entity.SysUser;
import com.itheima.mes1.module.system.mapper.SysRoleMapper;
import com.itheima.mes1.module.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/system/user")
public class SysUserController {

    @Autowired
    private SysUserService userService;
    @Autowired
    private SysRoleMapper roleMapper;

    @RequirePermission("system:user:list")
    @Operation(summary = "分页查询用户")
    @GetMapping
    public Result<PageResult<SysUser>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        Page<SysUser> result = userService.pageUsers(page, pageSize, keyword);
        result.getRecords().forEach(u -> {
            u.setPassword(null);
            u.setRoles(roleMapper.selectByUserId(u.getId()));
        });
        return Result.ok(new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @Operation(summary = "根据ID查询用户")
    @RequirePermission("system:user:list")
    @GetMapping("/{id}")
    public Result<SysUser> getById(@PathVariable Long id) {
        SysUser user = userService.getById(id);
        user.setPassword(null);
        return Result.ok(user);
    }

    @Operation(summary = "新增用户")
    @RequirePermission("system:user:add")
    @PostMapping
    public Result<?> add(@RequestBody SysUser user) {
        userService.save(user);
        return Result.ok();
    }

    @RequirePermission("system:user:edit")
    @Operation(summary = "修改用户")
    @PutMapping
    public Result<?> update(@RequestBody SysUser user) {
        userService.updateById(user);
        return Result.ok();
    }

    @RequirePermission("system:user:delete")
    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        userService.removeById(id);
        return Result.ok();
    }

    @RequirePermission("system:user:list")
    @Operation(summary = "分配角色")
    @PostMapping("/{id}/roles")
    public Result<?> assignRoles(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        List<Long> roleIds = body.get("roleIds");
        userService.assignRoles(id, roleIds != null ? roleIds.toArray(new Long[0]) : null);
        return Result.ok();
    }
}
