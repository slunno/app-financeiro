package com.appfinanceiro.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GoalContributionRequestDTO(
    @NotNull(message = "O valor do aporte é obrigatório.")
    @DecimalMin(value = "0.01", message = "O aporte deve ser maior que zero.")
    BigDecimal amount,

    LocalDate contributionDate,
    String notes
) {}
