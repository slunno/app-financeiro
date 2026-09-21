package com.appfinanceiro.service;

import com.appfinanceiro.domain.CreditCard;
import com.appfinanceiro.repository.CreditCardInvoiceRepository;
import com.appfinanceiro.repository.CreditCardRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardLimitServiceTest {

    @Mock
    private CreditCardRepository creditCardRepository;

    @Mock
    private CreditCardInvoiceRepository invoiceRepository;

    @InjectMocks
    private CardLimitService cardLimitService;

    @Test
    @DisplayName("Deve calcular limite disponível derivado = creditLimit - unpaidInvoices")
    void testGetAvailableLimit() {
        UUID cardId = UUID.randomUUID();
        CreditCard card = CreditCard.builder()
                .id(cardId)
                .creditLimit(new BigDecimal("5000.00"))
                .name("Visa Gold")
                .build();

        when(creditCardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(invoiceRepository.sumUnpaidAmountByCreditCardId(cardId)).thenReturn(new BigDecimal("1200.00"));

        BigDecimal available = cardLimitService.getAvailableLimit(cardId);
        assertEquals(new BigDecimal("3800.00"), available);
    }

    @Test
    @DisplayName("Deve calcular limite utilizado derivado = creditLimit - availableLimit")
    void testGetUsedLimit() {
        UUID cardId = UUID.randomUUID();
        CreditCard card = CreditCard.builder()
                .id(cardId)
                .creditLimit(new BigDecimal("5000.00"))
                .name("Visa Gold")
                .build();

        when(creditCardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(invoiceRepository.sumUnpaidAmountByCreditCardId(cardId)).thenReturn(new BigDecimal("1200.00"));

        BigDecimal used = cardLimitService.getUsedLimit(cardId);
        assertEquals(new BigDecimal("1200.00"), used);
    }
}
