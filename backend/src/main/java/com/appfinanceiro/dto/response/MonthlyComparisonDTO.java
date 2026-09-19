package com.appfinanceiro.dto.response;

import java.math.BigDecimal;

public record MonthlyComparisonDTO(
    String monthLabel,
    BigDecimal income,
    BigDecimal expense,
    BigDecimal result
) {}
