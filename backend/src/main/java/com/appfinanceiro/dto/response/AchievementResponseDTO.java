package com.appfinanceiro.dto.response;

import com.appfinanceiro.domain.enums.AchievementCategory;

import java.time.LocalDateTime;
import java.util.UUID;

public record AchievementResponseDTO(
    UUID id,
    String code,
    String title,
    String description,
    String icon,
    AchievementCategory category,
    Long xpReward,
    Boolean unlocked,
    LocalDateTime unlockedAt
) {}
