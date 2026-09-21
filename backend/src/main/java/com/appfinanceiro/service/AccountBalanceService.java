package com.appfinanceiro.service;

import com.appfinanceiro.domain.Account;
import com.appfinanceiro.domain.enums.TransactionStatus;
import com.appfinanceiro.exception.ResourceNotFoundException;
import com.appfinanceiro.repository.AccountRepository;
import com.appfinanceiro.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountBalanceService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    /**
     * Calcula o saldo derivado de uma conta:
     * saldo = initial_balance + Σ(INCOME) - Σ(EXPENSE) + Σ(TRANSFER IN) - Σ(TRANSFER OUT)
     * para todas as transações COMPLETED dessa conta.
     */
    @Transactional(readOnly = true)
    public BigDecimal getBalance(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta", "id", accountId));

        BigDecimal netMovement = transactionRepository.calculateNetAmountByAccountIdAndStatus(
                accountId, TransactionStatus.COMPLETED);

        return account.getInitialBalance().add(netMovement);
    }

    /**
     * Calcula o saldo total de todas as contas ativas (não arquivadas) de um usuário.
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalBalance(UUID userId) {
        List<Account> activeAccounts = accountRepository.findByUserIdAndArchivedAtIsNull(userId);
        BigDecimal total = BigDecimal.ZERO;

        for (Account account : activeAccounts) {
            BigDecimal netMovement = transactionRepository.calculateNetAmountByAccountIdAndStatus(
                    account.getId(), TransactionStatus.COMPLETED);
            total = total.add(account.getInitialBalance()).add(netMovement);
        }

        return total;
    }
}
