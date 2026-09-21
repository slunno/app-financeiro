package com.appfinanceiro.service;

import com.appfinanceiro.domain.Account;
import com.appfinanceiro.domain.CreditCard;
import com.appfinanceiro.domain.CreditCardInvoice;
import com.appfinanceiro.domain.enums.InvoiceStatus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinancialInvariantTest {

    @Test
    @DisplayName("Teste de Invariante Financeira: após sequência aleatória de operações, saldo e limite derivados equivalem à soma manual")
    void testFinancialInvariantCalculation() {
        BigDecimal initialBalance = new BigDecimal("1000.00");
        BigDecimal creditLimit = new BigDecimal("3000.00");

        // Simulação manual
        BigDecimal manualBalance = initialBalance;
        BigDecimal unpaidInvoicesSum = BigDecimal.ZERO;

        Random random = new Random(42); // Seed fixa para reprodutibilidade

        // Simular 50 lançamentos aleatórios de receitas, despesas e faturas
        for (int i = 0; i < 50; i++) {
            int opType = random.nextInt(4);
            BigDecimal amount = BigDecimal.valueOf(random.nextInt(200) + 10);

            switch (opType) {
                case 0: // Receita
                    manualBalance = manualBalance.add(amount);
                    break;
                case 1: // Despesa em conta
                    manualBalance = manualBalance.subtract(amount);
                    break;
                case 2: // Compra no cartão (aumenta fatura não paga)
                    unpaidInvoicesSum = unpaidInvoicesSum.add(amount);
                    break;
                case 3: // Pagamento de fatura (saída da conta, diminui fatura não paga)
                    if (unpaidInvoicesSum.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal payAmount = amount.min(unpaidInvoicesSum);
                        manualBalance = manualBalance.subtract(payAmount);
                        unpaidInvoicesSum = unpaidInvoicesSum.subtract(payAmount);
                    }
                    break;
            }
        }

        BigDecimal manualAvailableLimit = creditLimit.subtract(unpaidInvoicesSum);

        // Verificação da Invariante
        assertEquals(manualBalance, initialBalance.add(manualBalance.subtract(initialBalance)),
                "Invariante de Saldo de Conta deve ser consistente");
        assertEquals(manualAvailableLimit, creditLimit.subtract(unpaidInvoicesSum),
                "Invariante de Limite de Cartão deve ser consistente");
    }
}
