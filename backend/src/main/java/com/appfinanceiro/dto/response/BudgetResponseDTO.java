package com.appfinanceiro.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetResponseDTO(
    UUID id,
    UUID categoryId,
    String categoryName,
    String categoryIcon,
    String categoryColor,
    BigDecimal maxAmount,
    BigDecimal spentAmount,
    double percentageUsed,
    String status, // NORMAL, WARNING (>80%), EXCEEDED (>100%)
    Integer month,
    Integer year
) {}
