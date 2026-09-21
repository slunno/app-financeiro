package com.appfinanceiro.service;

import com.appfinanceiro.domain.CreditCard;
import com.appfinanceiro.exception.ResourceNotFoundException;
import com.appfinanceiro.repository.CreditCardInvoiceRepository;
import com.appfinanceiro.repository.CreditCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardLimitService {

    private final CreditCardRepository creditCardRepository;
    private final CreditCardInvoiceRepository invoiceRepository;

    /**
     * Calcula o limite disponível derivado:
     * disponível = credit_limit - Σ(faturas não pagas: total - paid)
     */
    @Transactional(readOnly = true)
    public BigDecimal getAvailableLimit(UUID cardId) {
        CreditCard card = creditCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Cartão de crédito", "id", cardId));

        BigDecimal unpaidAmount = invoiceRepository.sumUnpaidAmountByCreditCardId(cardId);
        BigDecimal available = card.getCreditLimit().subtract(unpaidAmount);

        return available.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : available;
    }

    /**
     * Calcula o limite utilizado derivado.
     */
    @Transactional(readOnly = true)
    public BigDecimal getUsedLimit(UUID cardId) {
        CreditCard card = creditCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Cartão de crédito", "id", cardId));

        return card.getCreditLimit().subtract(getAvailableLimit(cardId));
    }
}
