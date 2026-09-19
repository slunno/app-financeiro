package com.appfinanceiro.dto.response;

import com.appfinanceiro.domain.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreditCardInvoiceResponseDTO(
    UUID id,
    UUID creditCardId,
    String creditCardName,
    Integer referenceMonth,
    Integer referenceYear,
    BigDecimal totalAmount,
    BigDecimal paidAmount,
    LocalDate dueDate,
    LocalDate closingDate,
    InvoiceStatus status
) {}
