package com.appfinanceiro.service;

import com.appfinanceiro.domain.Budget;
import com.appfinanceiro.domain.Category;
import com.appfinanceiro.domain.User;
import com.appfinanceiro.domain.enums.TransactionType;
import com.appfinanceiro.dto.request.BudgetRequestDTO;
import com.appfinanceiro.dto.response.BudgetResponseDTO;
import com.appfinanceiro.exception.ResourceNotFoundException;
import com.appfinanceiro.repository.BudgetRepository;
import com.appfinanceiro.repository.CategoryRepository;
import com.appfinanceiro.repository.TransactionRepository;
import com.appfinanceiro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public List<BudgetResponseDTO> findByMonthAndYear(UUID userId, Integer month, Integer year) {
        List<Budget> budgets = budgetRepository.findByUserIdAndMonthAndYear(userId, month, year);

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        for (Budget budget : budgets) {
            BigDecimal spent = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                    userId, TransactionType.EXPENSE, start, end
            );
            budget.setSpentAmount(spent != null ? spent : BigDecimal.ZERO);
            budgetRepository.save(budget);
        }

        return budgets.stream().map(this::mapToResponse).toList();
    }

    @Transactional
    public BudgetResponseDTO createOrUpdate(UUID userId, BudgetRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", userId));

        Category category = categoryRepository.findByIdAndUserId(request.categoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", "id", request.categoryId()));

        Budget budget = budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(
                userId, request.categoryId(), request.month(), request.year()
        ).orElseGet(() -> Budget.builder()
                .user(user)
                .category(category)
                .month(request.month())
                .year(request.year())
                .build());

        budget.setMaxAmount(request.maxAmount());

        YearMonth yearMonth = YearMonth.of(request.year(), request.month());
        BigDecimal spent = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                userId, TransactionType.EXPENSE, yearMonth.atDay(1), yearMonth.atEndOfMonth()
        );
        budget.setSpentAmount(spent != null ? spent : BigDecimal.ZERO);

        budget = budgetRepository.save(budget);
        return mapToResponse(budget);
    }

    @Transactional
    public void delete(UUID id, UUID userId) {
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Orçamento", "id", id));
        budgetRepository.delete(budget);
    }

    public BudgetResponseDTO mapToResponse(Budget budget) {
        double percentage = 0.0;
        if (budget.getMaxAmount().compareTo(BigDecimal.ZERO) > 0) {
            percentage = budget.getSpentAmount().divide(budget.getMaxAmount(), 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
        }

        String status = "NORMAL";
        if (percentage >= 100.0) {
            status = "EXCEEDED";
        } else if (percentage >= 80.0) {
            status = "WARNING";
        }

        return new BudgetResponseDTO(
                budget.getId(),
                budget.getCategory().getId(),
                budget.getCategory().getName(),
                budget.getCategory().getIcon(),
                budget.getCategory().getColor(),
                budget.getMaxAmount(),
                budget.getSpentAmount(),
                percentage,
                status,
                budget.getMonth(),
                budget.getYear()
        );
    }
}
