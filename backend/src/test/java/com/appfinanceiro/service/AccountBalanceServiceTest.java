package com.appfinanceiro.service;

import com.appfinanceiro.domain.Account;
import com.appfinanceiro.domain.enums.AccountType;
import com.appfinanceiro.domain.enums.TransactionStatus;
import com.appfinanceiro.repository.AccountRepository;
import com.appfinanceiro.repository.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountBalanceServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountBalanceService accountBalanceService;

    @Test
    @DisplayName("Deve calcular o saldo derivado da conta = initialBalance + movimentação líquida")
    void testGetBalanceDerived() {
        UUID accountId = UUID.randomUUID();
        Account account = Account.builder()
                .id(accountId)
                .initialBalance(new BigDecimal("1000.00"))
                .type(AccountType.CHECKING)
                .name("Conta Corrente")
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        // Net movement: +500 (receitas) - 200 (despesas) = +300
        when(transactionRepository.calculateNetAmountByAccountIdAndStatus(accountId, TransactionStatus.COMPLETED))
                .thenReturn(new BigDecimal("300.00"));

        BigDecimal balance = accountBalanceService.getBalance(accountId);
        assertEquals(new BigDecimal("1300.00"), balance);
    }

    @Test
    @DisplayName("Deve calcular saldo total de todas as contas ativas do usuário")
    void testGetTotalBalanceActiveAccounts() {
        UUID userId = UUID.randomUUID();
        UUID acc1Id = UUID.randomUUID();
        UUID acc2Id = UUID.randomUUID();

        Account acc1 = Account.builder().id(acc1Id).initialBalance(new BigDecimal("500.00")).build();
        Account acc2 = Account.builder().id(acc2Id).initialBalance(new BigDecimal("200.00")).build();

        when(accountRepository.findByUserIdAndArchivedAtIsNull(userId)).thenReturn(List.of(acc1, acc2));
        when(transactionRepository.calculateNetAmountByAccountIdAndStatus(acc1Id, TransactionStatus.COMPLETED))
                .thenReturn(new BigDecimal("100.00"));
        when(transactionRepository.calculateNetAmountByAccountIdAndStatus(acc2Id, TransactionStatus.COMPLETED))
                .thenReturn(new BigDecimal("-50.00"));

        // acc1: 500 + 100 = 600, acc2: 200 - 50 = 150. Total = 750
        BigDecimal total = accountBalanceService.getTotalBalance(userId);
        assertEquals(new BigDecimal("750.00"), total);
    }
}
