package com.appfinanceiro.dto.response;

import com.appfinanceiro.domain.enums.AccountType;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponseDTO(
    UUID id,
    String name,
    AccountType type,
    BigDecimal initialBalance,
    BigDecimal currentBalance,
    String bankName,
    String color,
    String icon
) {}
