package com.itheima.mes1.module.system.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.mes1.common.MailService;
import com.itheima.mes1.common.Result;
import com.itheima.mes1.module.system.entity.SysUser;
import com.itheima.mes1.module.system.entity.SysUserRole;
import com.itheima.mes1.module.system.mapper.SysUserMapper;
import com.itheima.mes1.module.system.mapper.SysUserRoleMapper;
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
@Tag(name = "注册管理")
@RestController
@RequestMapping("/api/register")
public class RegisterController {

    @Autowired
    private SysUserService userService;
    @Autowired
    private MailService mailService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private SysUserRoleMapper userRoleMapper;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Operation(summary = "发送邮箱验证码")
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
        // 验证通过即删除，防止复用
        redisTemplate.delete(captchaKey);

        // 检查邮箱是否已被注册（邮箱即账号，查 username 和 email）
        long count = userService.count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, email).or().eq(SysUser::getEmail, email));
        if (count > 0) {
            return Result.fail("该邮箱已被注册");
        }

        // 检查是否 60 秒内已发送过
        String lastKey = "email:last:" + email;
        if (redisTemplate.hasKey(lastKey)) {
            return Result.fail("请 60 秒后再发送验证码");
        }

        // 生成 6 位数字验证码
        String code = RandomUtil.randomNumbers(6);

        // 存入 Redis，5 分钟有效
        String codeKey = "email:code:" + email;
        redisTemplate.opsForValue().set(codeKey, code, 5, TimeUnit.MINUTES);

        // 发送间隔限制 60 秒
        redisTemplate.opsForValue().set(lastKey, "1", 60, TimeUnit.SECONDS);

        // 发送邮件
        log.info("邮箱验证码: email={}, code={}", email, code);
        mailService.sendVerifyCode(email, code);

        return Result.ok("验证码已发送，请查收邮件");
    }

    @Operation(summary = "注册账号（用户名或邮箱均可登录）")
    @PostMapping
    public Result<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String nickname = body.get("nickname");
        String email = body.get("email");
        String code = body.get("code");
        String password = body.get("password");
        String roleIdStr = body.get("roleId");

        // 参数校验
        if (StrUtil.isBlank(username) || StrUtil.isBlank(email) || StrUtil.isBlank(code) || StrUtil.isBlank(password)) {
            return Result.fail("请填写完整信息");
        }
        if (username.length() < 2 || username.length() > 30) {
            return Result.fail("用户名需 2-30 个字符");
        }
        if (!username.matches("^[\\w\\u4e00-\\u9fa5.-]{2,30}$")) {
            return Result.fail("用户名仅支持中文、英文、数字、下划线、点号和短横线");
        }
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) {
            return Result.fail("邮箱格式不正确");
        }
        if (password.length() < 6) {
            return Result.fail("密码至少 6 位");
        }

        // 昵称：用户可自定义，不填则用用户名
        if (StrUtil.isBlank(nickname)) {
            nickname = username;
        }
        if (nickname.length() > 20) {
            return Result.fail("昵称最多 20 个字符");
        }

        // 校验验证码
        String codeKey = "email:code:" + email;
        String savedCode = (String) redisTemplate.opsForValue().get(codeKey);
        if (savedCode == null) {
            return Result.fail("验证码已过期，请重新获取");
        }
        if (!savedCode.equals(code)) {
            return Result.fail("验证码错误");
        }

        // 检查用户名是否已被占用
        long usernameCount = userService.count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (usernameCount > 0) {
            return Result.fail("该用户名已被使用");
        }

        // 检查邮箱是否已被占用
        long emailCount = userService.count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getEmail, email));
        if (emailCount > 0) {
            return Result.fail("该邮箱已被注册");
        }

        // 物理删除可能残留的软删除记录（避免唯一键冲突）
        SysUserMapper mapper = (SysUserMapper) userService.getBaseMapper();
        mapper.physicalDelete(username, email);

        // 角色校验
        Long roleId = null;
        if (StrUtil.isNotBlank(roleIdStr)) {
            try {
                roleId = Long.parseLong(roleIdStr);
            } catch (NumberFormatException e) {
                return Result.fail("角色参数无效");
            }
        }

        // 创建用户
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        user.setEmail(email);
        user.setStatus(1);
        userService.save(user);

        // 分配角色
        if (roleId != null) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(user.getId());
            ur.setRoleId(roleId);
            userRoleMapper.insert(ur);
        }

        // 删除验证码
        redisTemplate.delete(codeKey);

        log.info("新用户注册: username={}, email={}, roleId={}, id={}", username, email, roleId, user.getId());
        return Result.ok("注册成功");
    }
}
