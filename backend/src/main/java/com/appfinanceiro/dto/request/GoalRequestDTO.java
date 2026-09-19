package com.appfinanceiro.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GoalRequestDTO(
    @NotBlank(message = "O título da meta é obrigatório.")
    String title,

    String description,

    @NotNull(message = "O valor alvo é obrigatório.")
    @DecimalMin(value = "0.01", message = "O valor alvo deve ser positivo.")
    BigDecimal targetAmount,

    BigDecimal currentAmount,
    LocalDate targetDate,
    String categoryIcon
) {}
