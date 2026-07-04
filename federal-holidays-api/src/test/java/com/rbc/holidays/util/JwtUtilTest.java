package com.rbc.holidays.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey", "rbc-federal-holidays-secret-key-must-be-at-least-256-bits-long-for-hs256");
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 3600000L);
    }

    @Test
    void generateTokenShouldCreateValidTokenAndExposeUserId() {
        String token = jwtUtil.generateToken("qa-user");

        assertThat(token).isNotBlank();
        assertThat(jwtUtil.validateToken(token)).isTrue();
        assertThat(jwtUtil.getUserIdFromToken(token)).isEqualTo("qa-user");
    }

    @Test
    void validateTokenShouldReturnFalseForInvalidToken() {
        assertThat(jwtUtil.validateToken("bad-token")).isFalse();
    }

    @Test
    void getUserIdFromTokenShouldReturnNullForInvalidToken() {
        assertThat(jwtUtil.getUserIdFromToken("bad-token")).isNull();
    }

    @Test
    void extractTokenFromHeaderShouldReturnRawTokenWhenBearerPrefixExists() {
        assertThat(jwtUtil.extractTokenFromHeader("Bearer abc.def.ghi")).isEqualTo("abc.def.ghi");
    }

    @Test
    void extractTokenFromHeaderShouldReturnNullForInvalidFormat() {
        assertThat(jwtUtil.extractTokenFromHeader("Token abc.def.ghi")).isNull();
        assertThat(jwtUtil.extractTokenFromHeader(null)).isNull();
    }
}