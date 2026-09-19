package com.appfinanceiro.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequestDTO(
    @NotNull(message = "A conta de origem é obrigatória.")
    UUID sourceAccountId,

    @NotNull(message = "A conta de destino é obrigatória.")
    UUID destinationAccountId,

    @NotNull(message = "O valor é obrigatório.")
    @DecimalMin(value = "0.01", message = "O valor mínimo de transferência é R$ 0,01")
    BigDecimal amount,

    String description
) {}
