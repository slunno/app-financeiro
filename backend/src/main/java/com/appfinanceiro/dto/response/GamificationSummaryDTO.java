package com.appfinanceiro.dto.response;

import java.util.List;

public record GamificationSummaryDTO(
    UserXPResponseDTO userXp,
    List<MissionResponseDTO> activeMissions,
    List<AchievementResponseDTO> achievements,
    Integer unlockedAchievementsCount,
    Integer totalAchievementsCount
) {}
