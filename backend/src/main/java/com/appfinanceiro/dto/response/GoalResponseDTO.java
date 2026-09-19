package com.appfinanceiro.dto.response;

import com.appfinanceiro.domain.enums.GoalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record GoalResponseDTO(
    UUID id,
    String title,
    String description,
    BigDecimal targetAmount,
    BigDecimal currentAmount,
    double progressPercentage,
    LocalDate targetDate,
    String categoryIcon,
    GoalStatus status
) {}
