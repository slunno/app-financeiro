package com.appfinanceiro.service;

import com.appfinanceiro.domain.UserXP;
import com.appfinanceiro.domain.enums.GoalStatus;
import com.appfinanceiro.domain.enums.TransactionType;
import com.appfinanceiro.dto.response.*;
import com.appfinanceiro.exception.ResourceNotFoundException;
import com.appfinanceiro.repository.*;
import com.appfinanceiro.service.gamification.GamificationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserXPRepository userXPRepository;
    private final BudgetService budgetService;
    private final GoalService goalService;
    private final TransactionService transactionService;
    private final NotificationRepository notificationRepository;
    private final GamificationQueryService gamificationQueryService;

    @Transactional(readOnly = true)
    public DashboardSummaryDTO getDashboardSummary(UUID userId) {
        BigDecimal totalBalance = accountRepository.calculateTotalBalanceByUserId(userId);
        if (totalBalance == null) totalBalance = BigDecimal.ZERO;

        YearMonth currentMonth = YearMonth.now();
        LocalDate start = currentMonth.atDay(1);
        LocalDate end = currentMonth.atEndOfMonth();

        BigDecimal monthlyIncome = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                userId, TransactionType.INCOME, start, end
        );
        if (monthlyIncome == null) monthlyIncome = BigDecimal.ZERO;

        BigDecimal monthlyExpenses = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                userId, TransactionType.EXPENSE, start, end
        );
        if (monthlyExpenses == null) monthlyExpenses = BigDecimal.ZERO;

        BigDecimal monthlyResult = monthlyIncome.subtract(monthlyExpenses);

        UserXP userXP = userXPRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserXP não encontrado"));
        UserXPResponseDTO userXpDTO = new UserXPResponseDTO(
                userXP.getId(),
                userXP.getCurrentLevel(),
                userXP.getCurrentXp(),
                userXP.getTotalXp(),
                userXP.getStreakDays(),
                userXP.getLastActivityDate(),
                userXP.getFinancialHealthScore()
        );

        List<TransactionResponseDTO> recentTransactions = transactionRepository.findByUserIdOrderByDateDesc(userId)
                .stream().limit(5).map(transactionService::mapToResponse).toList();

        List<BudgetResponseDTO> budgets = budgetService.findByMonthAndYear(userId, currentMonth.getMonthValue(), currentMonth.getYear());

        List<GoalResponseDTO> activeGoals = goalService.findAllByUser(userId).stream()
                .filter(g -> g.status() == GoalStatus.IN_PROGRESS).toList();

        GamificationSummaryDTO gamificationSummary = gamificationQueryService.getSummary(userId);

        List<Object[]> categoryData = transactionRepository.sumExpensesByCategoryAndPeriod(userId, start, end);
        List<ExpensesByCategoryDTO> expensesByCategory = new ArrayList<>();

        if (monthlyExpenses.compareTo(BigDecimal.ZERO) > 0) {
            for (Object[] row : categoryData) {
                String catName = (String) row[0];
                BigDecimal amount = (BigDecimal) row[1];
                double percentage = amount.divide(monthlyExpenses, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
                expensesByCategory.add(new ExpensesByCategoryDTO(catName, "#10B981", "Tag", amount, percentage));
            }
        }

        long unreadNotifications = notificationRepository.countByUserIdAndIsReadFalse(userId);

        return new DashboardSummaryDTO(
                totalBalance,
                monthlyIncome,
                monthlyExpenses,
                monthlyResult,
                userXpDTO,
                recentTransactions,
                budgets,
                activeGoals,
                gamificationSummary.activeMissions(),
                expensesByCategory,
                unreadNotifications
        );
    }
}
