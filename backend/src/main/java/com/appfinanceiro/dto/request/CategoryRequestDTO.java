package com.appfinanceiro.dto.request;

import com.appfinanceiro.domain.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoryRequestDTO(
    @NotBlank(message = "O nome da categoria é obrigatório.")
    String name,

    @NotBlank(message = "O ícone é obrigatório.")
    String icon,

    @NotBlank(message = "A cor é obrigatória.")
    String color,

    @NotNull(message = "O tipo (INCOME/EXPENSE) é obrigatório.")
    TransactionType type
) {}
