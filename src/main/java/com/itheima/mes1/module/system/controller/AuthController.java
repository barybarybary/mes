package com.itheima.mes1.module.system.controller;

import com.itheima.mes1.common.Result;
import com.itheima.mes1.module.system.SysConverter;
import com.itheima.mes1.module.system.dto.LoginReq;
import com.itheima.mes1.module.system.entity.SysRole;
import com.itheima.mes1.module.system.entity.SysUser;
import com.itheima.mes1.module.system.mapper.SysRoleMapper;
import com.itheima.mes1.module.system.service.SysMenuService;
import com.itheima.mes1.module.system.service.SysUserService;
import com.itheima.mes1.module.system.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private SysUserService userService;
    @Autowired
    private SysMenuService menuService;
    @Autowired
    private SysRoleMapper roleMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginReq req) {
        // 数字验证码校验
        if (req.getCaptchaKey() == null || req.getCaptchaAnswer() == null) {
            return Result.fail("请输入验证码");
        }
        String savedAnswer = (String) redisTemplate.opsForValue().get(req.getCaptchaKey());
        if (savedAnswer == null) {
            return Result.fail("验证码已过期，请刷新");
        }
        if (!savedAnswer.equals(req.getCaptchaAnswer().trim())) {
            return Result.fail("验证码错误");
        }
        redisTemplate.delete(req.getCaptchaKey());

        SysUser user = userService.login(req.getUsername(), req.getPassword());
        List<SysRole> roles = roleMapper.selectByUserId(user.getId());
        List<SysMenuVO> menus = menuService.listTreeByUserId(user.getId());
        List<String> permissions = SysConverter.collectPermissions(menus);

        if (roles.stream().anyMatch(r -> "admin".equals(r.getCode()))) {
            permissions.add("admin");
        }

        redisTemplate.opsForValue().set("perm:" + user.getId(),
                new java.util.HashSet<>(permissions), 24, TimeUnit.HOURS);

        LoginVO vo = new LoginVO();
        vo.setToken(user.getToken());
        vo.setUser(SysConverter.toVO(user));
        vo.setRoles(SysConverter.toRoleVOList(roles));
        vo.setMenus(menus);
        vo.setPermissions(permissions);
        return Result.ok(vo);
    }

    @Operation(summary = "登出")
    @PostMapping("/logout")
    public Result<?> logout(@RequestHeader("Authorization") String token) {
        redisTemplate.delete("token:" + token.replace("Bearer ", ""));
        return Result.ok();
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/user-info")
    public Result<Map<String, Object>> userInfo(@RequestHeader("Authorization") String token) {
        Object userIdObj = redisTemplate.opsForValue().get("token:" + token.replace("Bearer ", ""));
        if (userIdObj == null) {
            return Result.fail(401, "Token 已过期，请重新登录");
        }
        Long userId = ((Number) userIdObj).longValue();
        SysUser user = userService.getById(userId);
        List<SysMenuVO> menus = menuService.listTreeByUserId(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("user", SysConverter.toVO(user));
        data.put("menus", menus);
        return Result.ok(data);
    }
}
