package com.appfinanceiro.service;

import com.appfinanceiro.domain.enums.TransactionType;
import com.appfinanceiro.dto.response.MonthlyComparisonDTO;
import com.appfinanceiro.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public List<MonthlyComparisonDTO> getMonthlyComparison(UUID userId, int months) {
        List<MonthlyComparisonDTO> list = new ArrayList<>();
        YearMonth current = YearMonth.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM/yy", Locale.forLanguageTag("pt-BR"));

        for (int i = months - 1; i >= 0; i--) {
            YearMonth target = current.minusMonths(i);
            LocalDate start = target.atDay(1);
            LocalDate end = target.atEndOfMonth();

            BigDecimal income = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                    userId, TransactionType.INCOME, start, end
            );
            if (income == null) income = BigDecimal.ZERO;

            BigDecimal expense = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                    userId, TransactionType.EXPENSE, start, end
            );
            if (expense == null) expense = BigDecimal.ZERO;

            BigDecimal result = income.subtract(expense);
            String label = target.format(formatter);

            list.add(new MonthlyComparisonDTO(label, income, expense, result));
        }

        return list;
    }
}
