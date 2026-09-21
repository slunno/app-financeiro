package com.appfinanceiro.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record PayInvoiceRequestDTO(
    @NotNull(message = "A conta de pagamento é obrigatória.")
    UUID accountId,

    @DecimalMin(value = "0.01", message = "O valor mínimo de pagamento é R$ 0,01")
    BigDecimal amount
) {}
