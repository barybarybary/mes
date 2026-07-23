package com.itheima.mes1.module.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itheima.mes1.common.exception.GlobalExceptionHandler;
import com.itheima.mes1.module.system.dto.LoginReq;
import com.itheima.mes1.module.system.entity.SysUser;
import com.itheima.mes1.module.system.mapper.SysRoleMapper;
import com.itheima.mes1.module.system.service.SysMenuService;
import com.itheima.mes1.module.system.service.SysUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private SysUserService userService;
    @MockBean private SysMenuService menuService;
    @MockBean private SysRoleMapper roleMapper;
    @SuppressWarnings("unchecked")
    @MockBean private RedisTemplate<String, Object> redisTemplate;

    @SuppressWarnings("unchecked")
    private final ValueOperations<String, Object> valueOps = mock(ValueOperations.class);

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    // ==================== login ====================

    @Test
    void login_shouldFail_whenNoCaptcha() throws Exception {
        LoginReq req = new LoginReq();
        req.setUsername("admin");
        req.setPassword("admin123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("请输入验证码"));
    }

    @Test
    void login_shouldFail_whenCaptchaExpired() throws Exception {
        LoginReq req = new LoginReq();
        req.setUsername("admin");
        req.setPassword("admin123");
        req.setCaptchaKey("expired-key");
        req.setCaptchaAnswer("42");

        when(valueOps.get("expired-key")).thenReturn(null);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("验证码已过期，请刷新"));
    }

    @Test
    void login_shouldFail_whenCaptchaWrong() throws Exception {
        LoginReq req = new LoginReq();
        req.setUsername("admin");
        req.setPassword("admin123");
        req.setCaptchaKey("valid-key");
        req.setCaptchaAnswer("wrong");

        when(valueOps.get("valid-key")).thenReturn("42");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("验证码错误"));
    }

    @Test
    void login_shouldSucceed_whenCredentialsValid() throws Exception {
        LoginReq req = new LoginReq();
        req.setUsername("admin");
        req.setPassword("admin123");
        req.setCaptchaKey("valid-key");
        req.setCaptchaAnswer("42");

        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setNickname("Administrator");
        user.setStatus(1);
        user.setToken("test-token-uuid");

        when(valueOps.get("valid-key")).thenReturn("42");
        when(userService.login("admin", "admin123")).thenReturn(user);
        when(roleMapper.selectByUserId(1L)).thenReturn(Collections.emptyList());
        when(menuService.listTreeByUserId(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("test-token-uuid"))
                .andExpect(jsonPath("$.data.user.username").value("admin"));
    }
}
