package com.appfinanceiro.service;

import com.appfinanceiro.domain.*;
import com.appfinanceiro.domain.enums.PaymentMethod;
import com.appfinanceiro.domain.enums.TransactionType;
import com.appfinanceiro.dto.request.TransactionRequestDTO;
import com.appfinanceiro.repository.*;
import com.appfinanceiro.service.gamification.GamificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private CreditCardRepository creditCardRepository;
    @Mock private CreditCardInvoiceRepository invoiceRepository;
    @Mock private InstallmentRepository installmentRepository;
    @Mock private GamificationService gamificationService;

    @InjectMocks private TransactionService transactionService;

    private UUID userId;
    private User mockUser;
    private Category mockCategory;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        mockUser = User.builder().id(userId).email("user@test.com").build();
        mockCategory = Category.builder().id(UUID.randomUUID()).name("Alimentação").build();
    }

    @Test
    @DisplayName("Deve rejeitar transação com cartão E conta selecionados ao mesmo tempo (exclusividade mútua)")
    void testMutualExclusivityValidation() {
        UUID accountId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(categoryRepository.findByIdAndUserId(mockCategory.getId(), userId)).thenReturn(Optional.of(mockCategory));
        when(accountRepository.findByIdAndUserIdAndArchivedAtIsNull(accountId, userId)).thenReturn(Optional.of(Account.builder().id(accountId).build()));
        when(creditCardRepository.findByIdAndUserId(cardId, userId)).thenReturn(Optional.of(CreditCard.builder().id(cardId).build()));

        TransactionRequestDTO request = new TransactionRequestDTO(
                accountId, mockCategory.getId(), cardId, "Supermercado",
                new BigDecimal("100.00"), LocalDate.now(), TransactionType.EXPENSE,
                PaymentMethod.CREDIT_CARD, false, null, null, null
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                transactionService.create(userId, request)
        );

        assertTrue(ex.getMessage().contains("conta vinculada"));
    }

    @Test
    @DisplayName("Deve ajustar arredondamento de parcelamento na última parcela (100 em 3x: 33.33, 33.33, 33.34)")
    void testInstallmentRoundingLastParcelAbsorbsDifference() {
        UUID cardId = UUID.randomUUID();
        CreditCard card = CreditCard.builder().id(cardId).closingDay(25).dueDay(10).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(categoryRepository.findByIdAndUserId(mockCategory.getId(), userId)).thenReturn(Optional.of(mockCategory));
        when(creditCardRepository.findByIdAndUserId(cardId, userId)).thenReturn(Optional.of(card));
        when(invoiceRepository.findByCreditCardIdAndReferenceMonthAndReferenceYear(any(), anyInt(), anyInt()))
                .thenReturn(Optional.empty());
        when(invoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });

        TransactionRequestDTO request = new TransactionRequestDTO(
                null, mockCategory.getId(), cardId, "Geladeira",
                new BigDecimal("100.00"), LocalDate.of(2026, 1, 10), TransactionType.EXPENSE,
                PaymentMethod.CREDIT_CARD, false, null, 3, null
        );

        transactionService.create(userId, request);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(3)).save(captor.capture());

        var transactions = captor.getAllValues();
        assertEquals(3, transactions.size());
        assertEquals(new BigDecimal("33.33"), transactions.get(0).getAmount());
        assertEquals(new BigDecimal("33.33"), transactions.get(1).getAmount());
        assertEquals(new BigDecimal("33.34"), transactions.get(2).getAmount()); // última absorve a diferença (33.33*2 + 33.34 = 100.00)
    }

    @Test
    @DisplayName("Deve calcular datas de fatura corretamente: compra no dia de fechamento entra no mês seguinte")
    void testInvoiceDateCalculationClosingDay() {
        CreditCard card = CreditCard.builder()
                .id(UUID.randomUUID())
                .closingDay(20)
                .dueDay(5) // dueDay < closingDay => vencimento mês seguinte ao fechamento
                .build();

        when(invoiceRepository.findByCreditCardIdAndReferenceMonthAndReferenceYear(any(), anyInt(), anyInt()))
                .thenReturn(Optional.empty());
        when(invoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Compra no dia 20/01 (dia do fechamento) -> fatura de Fevereiro/2026
        CreditCardInvoice invoice = transactionService.getOrCreateInvoiceForCardAndDate(card, LocalDate.of(2026, 1, 20));

        assertEquals(2, invoice.getReferenceMonth());
        assertEquals(2026, invoice.getReferenceYear());
        assertEquals(LocalDate.of(2026, 2, 20), invoice.getClosingDate());
        assertEquals(LocalDate.of(2026, 3, 5), invoice.getDueDate()); // dueDay < closingDay => Março
    }
}
