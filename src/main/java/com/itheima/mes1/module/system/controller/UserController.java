package com.itheima.mes1.module.system.controller;

import cn.hutool.core.util.StrUtil;
import com.itheima.mes1.common.Result;
import com.itheima.mes1.module.system.entity.SysUser;
import com.itheima.mes1.module.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "个人中心")
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private SysUserService userService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Operation(summary = "获取个人资料")
    @GetMapping("/profile")
    public Result<SysUser> getProfile(@RequestHeader("Authorization") String token) {
        Object userIdObj = redisTemplate.opsForValue().get("token:" + token.replace("Bearer ", ""));
        if (userIdObj == null) {
            return Result.fail("未登录或登录已过期");
        }
        Long userId = ((Number) userIdObj).longValue();
        SysUser user = userService.getById(userId);
        if (user != null) user.setPassword(null);
        return Result.ok(user);
    }

    @Operation(summary = "更新个人资料")
    @PutMapping("/profile")
    public Result<?> updateProfile(@RequestHeader("Authorization") String token,
                                   @RequestBody Map<String, String> body) {
        Object userIdObj = redisTemplate.opsForValue().get("token:" + token.replace("Bearer ", ""));
        if (userIdObj == null) {
            return Result.fail("未登录或登录已过期");
        }
        Long userId = ((Number) userIdObj).longValue();
        SysUser user = userService.getById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }

        String nickname = body.get("nickname");
        String email = body.get("email");
        String phone = body.get("phone");

        if (StrUtil.isNotBlank(nickname)) {
            if (nickname.length() > 20) return Result.fail("昵称最多 20 个字符");
            user.setNickname(nickname);
        }
        if (StrUtil.isNotBlank(email)) {
            if (!email.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) return Result.fail("邮箱格式不正确");
            user.setEmail(email);
        }
        if (StrUtil.isNotBlank(phone)) {
            user.setPhone(phone);
        }
        userService.updateById(user);
        return Result.ok(user);
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<?> changePassword(@RequestHeader("Authorization") String token,
                                    @RequestBody Map<String, String> body) {
        Object userIdObj = redisTemplate.opsForValue().get("token:" + token.replace("Bearer ", ""));
        if (userIdObj == null) {
            return Result.fail("未登录或登录已过期");
        }
        Long userId = ((Number) userIdObj).longValue();
        SysUser user = userService.getById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }

        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        if (StrUtil.isBlank(oldPassword) || StrUtil.isBlank(newPassword)) {
            return Result.fail("请填写完整信息");
        }
        if (newPassword.length() < 6) {
            return Result.fail("新密码至少 6 位");
        }

        // 校验原密码 (兼容 MD5 和 BCrypt)
        if (!passwordEncoder.matches(oldPassword, user.getPassword())
                && !user.getPassword().equals(cn.hutool.crypto.SecureUtil.md5(oldPassword))) {
            return Result.fail("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userService.updateById(user);
        return Result.ok("密码修改成功");
    }
}
