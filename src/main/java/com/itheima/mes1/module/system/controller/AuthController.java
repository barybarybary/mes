package com.itheima.mes1.module.system.controller;

import com.itheima.mes1.common.Result;
import com.itheima.mes1.module.system.entity.SysMenu;
import com.itheima.mes1.module.system.entity.SysRole;
import com.itheima.mes1.module.system.entity.SysUser;
import com.itheima.mes1.module.system.mapper.SysRoleMapper;
import com.itheima.mes1.module.system.service.SysMenuService;
import com.itheima.mes1.module.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public Result<Map<String, Object>> login(@RequestBody LoginRequest req) {
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
        // 验证通过即删除，防止复用
        redisTemplate.delete(req.getCaptchaKey());

        SysUser user = userService.login(req.getUsername(), req.getPassword());
        // 获取用户角色
        List<SysRole> roles = roleMapper.selectByUserId(user.getId());
        // 获取用户菜单
        List<SysMenu> menus = menuService.listByUserId(user.getId());
        // 递归提取所有节点的权限标识
        List<String> permissions = new ArrayList<>();
        collectPermissions(menus, permissions);

        // 管理员加万能标记，PermissionInterceptor 跳过检查
        if (roles.stream().anyMatch(r -> "admin".equals(r.getCode()))) {
            permissions.add("admin");
        }

        // 缓存权限到 Redis（24h），供 AuthInterceptor 使用
        redisTemplate.opsForValue().set("perm:" + user.getId(), new java.util.HashSet<>(permissions), 24, TimeUnit.HOURS);

        Map<String, Object> data = new HashMap<>();
        data.put("token", user.getToken());
        data.put("user", user);
        data.put("roles", roles);
        data.put("menus", menus);
        data.put("permissions", permissions);
        return Result.ok(data);
    }

    private void collectPermissions(List<SysMenu> menus, List<String> result) {
        for (SysMenu m : menus) {
            if (m.getPermission() != null) {
                result.add(m.getPermission());
            }
            if (m.getChildren() != null) {
                collectPermissions(m.getChildren(), result);
            }
        }
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
        if (user != null) user.setPassword(null);
        List<SysMenu> menus = menuService.listByUserId(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("menus", menus);
        return Result.ok(data);
    }
}

class LoginRequest {
    private String username;
    private String password;
    private String captchaKey;
    private String captchaAnswer;
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getCaptchaKey() { return captchaKey; }
    public void setCaptchaKey(String captchaKey) { this.captchaKey = captchaKey; }
    public String getCaptchaAnswer() { return captchaAnswer; }
    public void setCaptchaAnswer(String captchaAnswer) { this.captchaAnswer = captchaAnswer; }
}
