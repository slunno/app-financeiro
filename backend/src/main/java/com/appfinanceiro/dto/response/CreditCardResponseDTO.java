package com.appfinanceiro.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record CreditCardResponseDTO(
    UUID id,
    String name,
    BigDecimal creditLimit,
    BigDecimal availableLimit,
    BigDecimal usedLimit,
    double limitUsagePercentage,
    Integer closingDay,
    Integer dueDay,
    String cardBrand,
    String color
) {}
