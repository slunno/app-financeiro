package com.appfinanceiro.dto.response;

import java.math.BigDecimal;

public record ExpensesByCategoryDTO(
    String categoryName,
    String categoryColor,
    String categoryIcon,
    BigDecimal totalAmount,
    double percentage
) {}
