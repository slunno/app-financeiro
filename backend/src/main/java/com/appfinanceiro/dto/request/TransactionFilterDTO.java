package com.appfinanceiro.dto.request;

import com.appfinanceiro.domain.enums.TransactionStatus;
import com.appfinanceiro.domain.enums.TransactionType;

import java.time.LocalDate;
import java.util.UUID;

public record TransactionFilterDTO(
    LocalDate startDate,
    LocalDate endDate,
    UUID accountId,
    UUID creditCardId,
    UUID categoryId,
    TransactionType type,
    TransactionStatus status,
    String search
) {}
