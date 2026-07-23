package com.itheima.mes1.module.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.mes1.common.Result;
import com.itheima.mes1.common.annotation.RequirePermission;
import com.itheima.mes1.module.system.SysConverter;
import com.itheima.mes1.module.system.dto.SysUserCreateReq;
import com.itheima.mes1.module.system.dto.SysUserUpdateReq;
import com.itheima.mes1.module.system.entity.SysRole;
import com.itheima.mes1.module.system.entity.SysUser;
import com.itheima.mes1.module.system.mapper.SysRoleMapper;
import com.itheima.mes1.module.system.mapper.SysUserRoleMapper;
import com.itheima.mes1.module.system.service.SysUserService;
import com.itheima.mes1.module.system.vo.SysUserVO;
import com.itheima.mes1.module.system.vo.UserPageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/system/user")
public class SysUserController {

    @Autowired
    private SysUserService userService;
    @Autowired
    private SysRoleMapper roleMapper;
    @Autowired
    private SysUserRoleMapper userRoleMapper;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @RequirePermission("system:user:list")
    @Operation(summary = "分页查询用户")
    @GetMapping
    public Result<UserPageResult<SysUserVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        Page<SysUser> result = userService.pageUsers(page, pageSize, keyword);
        List<SysUserVO> voList = result.getRecords().stream().map(u -> {
            SysUserVO vo = SysConverter.toVO(u);
            List<SysRole> roles = roleMapper.selectByUserId(u.getId());
            vo.setRoles(SysConverter.toRoleVOList(roles));
            if (roles != null && !roles.isEmpty()) {
                vo.setRole(roles.get(0).getCode());
                vo.setRoleName(roles.stream()
                        .map(SysRole::getCode)
                        .reduce((a, b) -> a + "," + b).orElse(""));
            }
            return vo;
        }).collect(Collectors.toList());

        UserPageResult<SysUserVO> pageResult = new UserPageResult<>(voList, result.getTotal(), page, pageSize);
        pageResult.setAdminCount(userRoleMapper.countAdminUsers(keyword));
        pageResult.setEnabledCount(userService.count(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getStatus, 1)
                        .like(cn.hutool.core.util.StrUtil.isNotBlank(keyword), SysUser::getUsername, keyword)));
        pageResult.setDisabledCount(userService.count(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getStatus, 0)
                        .like(cn.hutool.core.util.StrUtil.isNotBlank(keyword), SysUser::getUsername, keyword)));
        return Result.ok(pageResult);
    }

    @Operation(summary = "根据ID查询用户")
    @RequirePermission("system:user:list")
    @GetMapping("/{id}")
    public Result<SysUserVO> getById(@PathVariable Long id) {
        SysUser user = userService.getById(id);
        return Result.ok(SysConverter.toVO(user));
    }

    @Operation(summary = "新增用户")
    @RequirePermission("system:user:add")
    @PostMapping
    public Result<?> add(@RequestBody SysUserCreateReq req) {
        SysUser user = SysConverter.toEntity(req);
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userService.save(user);
        return Result.ok();
    }

    @RequirePermission("system:user:edit")
    @Operation(summary = "修改用户")
    @PutMapping
    public Result<?> update(@RequestBody SysUserUpdateReq req) {
        SysUser user = SysConverter.toEntity(req);
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
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
