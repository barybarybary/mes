package com.itheima.mes1.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.mes1.common.exception.BusinessException;
import com.itheima.mes1.module.system.entity.SysUser;
import com.itheima.mes1.module.system.entity.SysUserRole;
import com.itheima.mes1.module.system.mapper.SysUserMapper;
import com.itheima.mes1.module.system.mapper.SysUserRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SysUserServiceImplTest {

    @Mock private SysUserMapper userMapper;
    @Mock private SysUserRoleMapper userRoleMapper;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOps;
    @Mock private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private SysUserServiceImpl userService;

    @BeforeEach
    void setUp() {
        // 手动注入 baseMapper（MyBatis-Plus 依赖此字段）
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    // ==================== login ====================

    @Test
    void login_shouldReturnUserWithToken_whenCredentialsValid() {
        SysUser user = buildUser(1L, "admin", "$2a$encoded", 1);
        when(userMapper.selectOne(any(LambdaQueryWrapper.class), anyBoolean())).thenReturn(user);
        when(passwordEncoder.matches("admin123", "$2a$encoded")).thenReturn(true);

        SysUser result = userService.login("admin", "admin123");

        assertNotNull(result);
        assertNotNull(result.getToken());
        assertEquals(32, result.getToken().length());
        assertNull(result.getPassword());
        verify(valueOps).set(eq("token:" + result.getToken()), eq(1L), eq(24L), eq(TimeUnit.HOURS));
    }

    @Test
    void login_shouldThrowException_whenUserNotFound() {
        lenient().when(userMapper.selectOne(any(LambdaQueryWrapper.class), anyBoolean())).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.login("nobody", "password"));
        assertEquals("用户名或密码错误", ex.getMessage());
    }

    @Test
    void login_shouldThrowException_whenUserDisabled() {
        SysUser user = buildUser(2L, "disabled", "$2a$encoded", 0);
        lenient().when(userMapper.selectOne(any(LambdaQueryWrapper.class), anyBoolean())).thenReturn(user);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.login("disabled", "password"));
        assertEquals("账号已被禁用", ex.getMessage());
    }

    @Test
    void login_shouldThrowException_whenPasswordWrong() {
        SysUser user = buildUser(3L, "user", "$2a$encoded", 1);
        lenient().when(userMapper.selectOne(any(LambdaQueryWrapper.class), anyBoolean())).thenReturn(user);
        lenient().when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.login("user", "wrong"));
        assertEquals("用户名或密码错误", ex.getMessage());
    }

    // ==================== pageUsers ====================

    @Test
    void pageUsers_shouldCallMapperWithCorrectWrapper() {
        when(userMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<>(1, 10));
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        Page<SysUser> result = userService.pageUsers(1, 10, "test");

        assertNotNull(result);
        assertEquals(0, result.getTotal());
    }

    // ==================== assignRoles ====================

    @Test
    void assignRoles_shouldDeleteOldAndInsertNew() {
        Long userId = 1L;
        Long[] roleIds = {10L, 20L};

        userService.assignRoles(userId, roleIds);

        verify(userRoleMapper).deleteByUserId(userId);
        ArgumentCaptor<SysUserRole> captor = ArgumentCaptor.forClass(SysUserRole.class);
        verify(userRoleMapper, times(2)).insert(captor.capture());
        assertEquals(10L, captor.getAllValues().get(0).getRoleId());
        assertEquals(20L, captor.getAllValues().get(1).getRoleId());
        assertEquals(userId, captor.getAllValues().get(0).getUserId());
    }

    @Test
    void assignRoles_shouldOnlyDelete_whenRoleIdsNull() {
        userService.assignRoles(1L, null);
        verify(userRoleMapper).deleteByUserId(1L);
        verify(userRoleMapper, never()).insert(any(SysUserRole.class));
    }

    @Test
    void assignRoles_shouldOnlyDelete_whenRoleIdsEmpty() {
        userService.assignRoles(1L, new Long[0]);
        verify(userRoleMapper).deleteByUserId(1L);
        verify(userRoleMapper, never()).insert(any(SysUserRole.class));
    }

    // ==================== helper ====================

    private SysUser buildUser(Long id, String username, String password, int status) {
        SysUser u = new SysUser();
        u.setId(id);
        u.setUsername(username);
        u.setPassword(password);
        u.setStatus(status);
        u.setCreateTime(LocalDateTime.now());
        return u;
    }
}
