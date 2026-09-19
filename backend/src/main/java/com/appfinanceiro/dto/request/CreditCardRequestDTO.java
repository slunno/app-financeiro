package com.appfinanceiro.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreditCardRequestDTO(
    @NotBlank(message = "O nome do cartão é obrigatório.")
    String name,

    @NotNull(message = "O limite total de crédito é obrigatório.")
    @DecimalMin(value = "0.01", message = "O limite deve ser maior que zero.")
    BigDecimal creditLimit,

    @NotNull(message = "O dia de fechamento é obrigatório.")
    @Min(1) @Max(31)
    Integer closingDay,

    @NotNull(message = "O dia de vencimento é obrigatório.")
    @Min(1) @Max(31)
    Integer dueDay,

    String cardBrand,
    String color
) {}
