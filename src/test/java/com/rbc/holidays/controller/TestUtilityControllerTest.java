package com.rbc.holidays.controller;

import com.rbc.holidays.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestUtilityControllerTest {

    @Mock
    private JwtUtil jwtUtil;

    @Test
    void generateTokenShouldReturnPayloadForRequestedUser() {
        TestUtilityController controller = new TestUtilityController(jwtUtil);
        when(jwtUtil.generateToken("qa-user")).thenReturn("jwt-token");

        var response = controller.generateToken("qa-user");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("token", "jwt-token");
        assertThat(response.getBody()).containsEntry("userId", "qa-user");
        assertThat(response.getBody()).containsEntry("expiresInMs", 3600000);
        assertThat(response.getBody()).containsKey("correlationId");
        verify(jwtUtil).generateToken("qa-user");
    }
}