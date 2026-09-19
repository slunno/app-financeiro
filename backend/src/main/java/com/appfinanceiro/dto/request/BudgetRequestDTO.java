package com.appfinanceiro.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetRequestDTO(
    @NotNull(message = "A categoria é obrigatória.")
    UUID categoryId,

    @NotNull(message = "O valor teto do orçamento é obrigatório.")
    @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero.")
    BigDecimal maxAmount,

    @NotNull(message = "O mês é obrigatório.")
    @Min(1) @Max(12)
    Integer month,

    @NotNull(message = "O ano é obrigatório.")
    Integer year
) {}
