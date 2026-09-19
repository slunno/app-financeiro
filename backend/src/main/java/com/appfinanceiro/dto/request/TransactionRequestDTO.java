package com.appfinanceiro.dto.request;

import com.appfinanceiro.domain.enums.PaymentMethod;
import com.appfinanceiro.domain.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionRequestDTO(
    UUID accountId,
    @NotNull(message = "A categoria é obrigatória.")
    UUID categoryId,
    UUID creditCardId,

    @NotBlank(message = "A descrição é obrigatória.")
    String description,

    @NotNull(message = "O valor é obrigatório.")
    @DecimalMin(value = "0.01", message = "O valor da transação deve ser positivo.")
    BigDecimal amount,

    @NotNull(message = "A data é obrigatória.")
    LocalDate date,

    @NotNull(message = "O tipo da transação é obrigatório.")
    TransactionType type,

    @NotNull(message = "A forma de pagamento é obrigatória.")
    PaymentMethod paymentMethod,

    Boolean isRecurring,
    String recurrencePeriod,
    Integer installmentsCount, // opcional para parcelamento automático
    String notes
) {}
