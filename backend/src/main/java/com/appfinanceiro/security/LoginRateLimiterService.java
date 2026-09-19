package com.appfinanceiro.security;

import com.appfinanceiro.exception.TooManyRequestsException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginRateLimiterService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_TIME_DURATION_MS = 15 * 60 * 1000; // 15 minutos

    private static class AttemptInfo {
        int attempts;
        long lastAttemptTimestamp;

        AttemptInfo(int attempts, long lastAttemptTimestamp) {
            this.attempts = attempts;
            this.lastAttemptTimestamp = lastAttemptTimestamp;
        }
    }

    private final Map<String, AttemptInfo> attemptsCache = new ConcurrentHashMap<>();

    public void checkRateLimit(String clientIp, String email) {
        String key = clientIp + ":" + (email != null ? email.toLowerCase().trim() : "");
        long now = Instant.now().toEpochMilli();

        AttemptInfo info = attemptsCache.get(key);
        if (info != null) {
            if (now - info.lastAttemptTimestamp > LOCK_TIME_DURATION_MS) {
                attemptsCache.remove(key);
            } else if (info.attempts >= MAX_ATTEMPTS) {
                throw new TooManyRequestsException("Muitas tentativas incorretas de login. Aguarde 15 minutos antes de tentar novamente.");
            }
        }
    }

    public void recordFailedAttempt(String clientIp, String email) {
        String key = clientIp + ":" + (email != null ? email.toLowerCase().trim() : "");
        long now = Instant.now().toEpochMilli();

        attemptsCache.compute(key, (k, v) -> {
            if (v == null || (now - v.lastAttemptTimestamp > LOCK_TIME_DURATION_MS)) {
                return new AttemptInfo(1, now);
            }
            v.attempts++;
            v.lastAttemptTimestamp = now;
            return v;
        });
    }

    public void resetAttempts(String clientIp, String email) {
        String key = clientIp + ":" + (email != null ? email.toLowerCase().trim() : "");
        attemptsCache.remove(key);
    }
}
