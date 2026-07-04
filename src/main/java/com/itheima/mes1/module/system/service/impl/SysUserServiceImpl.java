package com.itheima.mes1.module.system.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.mes1.common.exception.BusinessException;
import com.itheima.mes1.module.system.entity.SysUser;
import com.itheima.mes1.module.system.entity.SysUserRole;
import com.itheima.mes1.module.system.mapper.SysUserMapper;
import com.itheima.mes1.module.system.mapper.SysUserRoleMapper;
import com.itheima.mes1.module.system.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    private SysUserRoleMapper userRoleMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public SysUser login(String account, String password) {
        // 支持用户名或邮箱登录
        SysUser user = getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, account).or()
                .eq(SysUser::getEmail, account));
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        // BCrypt 密码校验 + MD5 兼容迁移
        String storedPwd = user.getPassword();
        if (passwordEncoder.matches(password, storedPwd)) {
            // BCrypt 匹配，登录成功
        } else if (storedPwd.equals(SecureUtil.md5(password))) {
            // MD5 匹配成功，自动升级为 BCrypt
            user.setPassword(passwordEncoder.encode(password));
            updateById(user);
        } else {
            throw new BusinessException("用户名或密码错误");
        }
        // 更新最后登录时间
        user.setLastLoginTime(java.time.LocalDateTime.now());
        updateById(user);

        // 删除旧 token（避免同一用户多次登录积累 token）
        Object oldToken = redisTemplate.opsForValue().get("user_token:" + user.getId());
        if (oldToken != null) {
            redisTemplate.delete("token:" + oldToken.toString());
        }

        // 生成新 token
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set("token:" + token, user.getId(), 24, TimeUnit.HOURS);
        redisTemplate.opsForValue().set("user_token:" + user.getId(), token, 24, TimeUnit.HOURS);
        user.setToken(token);
        user.setPassword(null);
        return user;
    }

    @Override
    public Page<SysUser> pageUsers(int page, int pageSize, String keyword) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .like(StrUtil.isNotBlank(keyword), SysUser::getUsername, keyword)
                .orderByDesc(SysUser::getCreateTime);
        Page<SysUser> result = page(new Page<>(page, pageSize), wrapper);
        result.setTotal(count(wrapper));
        return result;
    }

    @Override
    @Transactional
    public void assignRoles(Long userId, Long[] roleIds) {
        userRoleMapper.deleteByUserId(userId);
        if (roleIds != null) {
            for (Long roleId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            }
        }
    }
}
