package com.itheima.mes1.module.system.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.mes1.common.MailService;
import com.itheima.mes1.common.Result;
import com.itheima.mes1.module.system.entity.SysUser;
import com.itheima.mes1.module.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Tag(name = "密码重置")
@RestController
@RequestMapping("/api/password")
public class PasswordController {

    @Autowired
    private SysUserService userService;
    @Autowired
    private MailService mailService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private static final String CODE_PREFIX = "email:code:pwdreset:";

    @Operation(summary = "发送密码重置验证码")
    @PostMapping("/send-code")
    public Result<?> sendCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String captchaKey = body.get("captchaKey");
        String captchaCode = body.get("captchaCode");

        if (StrUtil.isBlank(email)) {
            return Result.fail("邮箱不能为空");
        }
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) {
            return Result.fail("邮箱格式不正确");
        }

        // 图形验证码校验
        if (StrUtil.isBlank(captchaKey) || StrUtil.isBlank(captchaCode)) {
            return Result.fail("请输入图形验证码");
        }
        String savedCaptcha = (String) redisTemplate.opsForValue().get(captchaKey);
        if (savedCaptcha == null) {
            return Result.fail("图形验证码已过期，请刷新");
        }
        if (!savedCaptcha.equalsIgnoreCase(captchaCode.trim())) {
            return Result.fail("图形验证码错误");
        }
        redisTemplate.delete(captchaKey);

        // 检查邮箱是否存在
        long count = userService.count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getEmail, email));
        if (count == 0) {
            return Result.fail("该邮箱未注册");
        }

        // 检查是否 60 秒内已发送过
        String cooldownKey = "pwdreset:cooldown:" + email;
        if (redisTemplate.hasKey(cooldownKey)) {
            return Result.fail("请 60 秒后再发送验证码");
        }

        String code = RandomUtil.randomNumbers(6);
        String codeKey = CODE_PREFIX + email;
        redisTemplate.opsForValue().set(codeKey, code, 5, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(cooldownKey, "1", 60, TimeUnit.SECONDS);

        log.info("密码重置验证码: email={}, code={}", email, code);
        mailService.sendPasswordResetCode(email, code);

        return Result.ok("验证码已发送，请查收邮件");
    }

    @Operation(summary = "重置密码")
    @PostMapping("/reset")
    public Result<?> reset(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        String code = body.get("code");

        if (StrUtil.isBlank(email) || StrUtil.isBlank(password) || StrUtil.isBlank(code)) {
            return Result.fail("请填写完整信息");
        }
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) {
            return Result.fail("邮箱格式不正确");
        }
        if (password.length() < 6) {
            return Result.fail("密码至少 6 位");
        }

        // 校验验证码
        String codeKey = CODE_PREFIX + email;
        String savedCode = (String) redisTemplate.opsForValue().get(codeKey);
        if (savedCode == null) {
            return Result.fail("验证码已过期，请重新获取");
        }
        if (!savedCode.equals(code)) {
            return Result.fail("验证码错误");
        }

        // 查找用户
        SysUser user = userService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getEmail, email));
        if (user == null) {
            return Result.fail("用户不存在");
        }

        user.setPassword(passwordEncoder.encode(password));
        userService.updateById(user);

        // 删除验证码
        redisTemplate.delete(codeKey);

        log.info("密码重置成功: email={}, userId={}", email, user.getId());
        return Result.ok("密码重置成功");
    }
}
