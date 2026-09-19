package com.appfinanceiro.security;

import com.appfinanceiro.exception.TooManyRequestsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginRateLimiterServiceTest {

    private LoginRateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        rateLimiterService = new LoginRateLimiterService();
    }

    @Test
    @DisplayName("Deve permitir até 5 tentativas e lançar TooManyRequestsException na 6ª")
    void testRateLimitThreshold() {
        String ip = "192.168.1.100";
        String email = "test@exemplo.com";

        for (int i = 0; i < 5; i++) {
            assertDoesNotThrow(() -> rateLimiterService.checkRateLimit(ip, email));
            rateLimiterService.recordFailedAttempt(ip, email);
        }

        assertThrows(TooManyRequestsException.class, () -> rateLimiterService.checkRateLimit(ip, email));
    }

    @Test
    @DisplayName("Deve resetar contador de tentativas após sucesso")
    void testResetAttempts() {
        String ip = "192.168.1.100";
        String email = "test@exemplo.com";

        for (int i = 0; i < 4; i++) {
            rateLimiterService.recordFailedAttempt(ip, email);
        }

        rateLimiterService.resetAttempts(ip, email);

        assertDoesNotThrow(() -> rateLimiterService.checkRateLimit(ip, email));
    }
}
