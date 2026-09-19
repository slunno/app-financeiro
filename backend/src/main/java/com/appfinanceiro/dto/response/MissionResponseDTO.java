package com.appfinanceiro.dto.response;

import com.appfinanceiro.domain.enums.MissionDifficulty;
import com.appfinanceiro.domain.enums.MissionFrequency;
import com.appfinanceiro.domain.enums.MissionStatus;

import java.time.LocalDate;
import java.util.UUID;

public record MissionResponseDTO(
    UUID id,
    String title,
    String description,
    MissionFrequency frequency,
    MissionDifficulty difficulty,
    Long xpReward,
    Integer targetCount,
    Integer currentCount,
    double progressPercentage,
    MissionStatus status,
    LocalDate endDate
) {}
