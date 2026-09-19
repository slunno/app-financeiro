package com.appfinanceiro.dto.response;

import java.time.LocalDate;
import java.util.UUID;

public record UserXPResponseDTO(
    UUID id,
    Integer currentLevel,
    Long currentXp,
    Long totalXp,
    Integer streakDays,
    LocalDate lastActivityDate,
    Integer financialHealthScore
) {}
