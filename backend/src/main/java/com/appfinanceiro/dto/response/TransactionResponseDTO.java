package com.appfinanceiro.dto.response;

import com.appfinanceiro.domain.enums.PaymentMethod;
import com.appfinanceiro.domain.enums.TransactionStatus;
import com.appfinanceiro.domain.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionResponseDTO(
    UUID id,
    UUID accountId,
    String accountName,
    UUID categoryId,
    String categoryName,
    String categoryIcon,
    String categoryColor,
    UUID creditCardId,
    String creditCardName,
    String description,
    BigDecimal amount,
    LocalDate date,
    TransactionType type,
    TransactionStatus status,
    PaymentMethod paymentMethod,
    Boolean isRecurring,
    String notes
) {}
