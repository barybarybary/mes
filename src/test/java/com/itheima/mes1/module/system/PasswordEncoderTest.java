package com.itheima.mes1.module.system;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BCrypt 密码加密单元测试
 */
class PasswordEncoderTest {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void testEncodeAndMatch() {
        String raw = "admin123";
        String encoded = encoder.encode(raw);

        assertNotEquals(raw, encoded);
        assertTrue(encoded.startsWith("$2"));
        assertTrue(encoder.matches(raw, encoded));
    }

    @Test
    void testDifferentSalts() {
        String raw = "password";
        String enc1 = encoder.encode(raw);
        String enc2 = encoder.encode(raw);

        // 相同的明文每次生成的密文不同（随机盐）
        assertNotEquals(enc1, enc2);
        // 但都能匹配
        assertTrue(encoder.matches(raw, enc1));
        assertTrue(encoder.matches(raw, enc2));
    }

    @Test
    void testWrongPassword() {
        String encoded = encoder.encode("correct");
        assertFalse(encoder.matches("wrong", encoded));
    }

    @Test
    void testEmptyPassword() {
        String encoded = encoder.encode("");
        assertTrue(encoder.matches("", encoded));
        assertFalse(encoder.matches(" ", encoded));
    }

}
