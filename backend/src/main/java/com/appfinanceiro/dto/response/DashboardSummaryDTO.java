package com.appfinanceiro.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record DashboardSummaryDTO(
    BigDecimal totalBalance,
    BigDecimal monthlyIncome,
    BigDecimal monthlyExpenses,
    BigDecimal monthlyResult,
    UserXPResponseDTO userXp,
    List<TransactionResponseDTO> recentTransactions,
    List<BudgetResponseDTO> budgets,
    List<GoalResponseDTO> activeGoals,
    List<MissionResponseDTO> activeMissions,
    List<ExpensesByCategoryDTO> expensesByCategory,
    long unreadNotificationsCount
) {}
