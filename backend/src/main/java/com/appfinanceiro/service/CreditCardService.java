package com.appfinanceiro.service;

import com.appfinanceiro.domain.CreditCard;
import com.appfinanceiro.domain.CreditCardInvoice;
import com.appfinanceiro.domain.User;
import com.appfinanceiro.domain.enums.InvoiceStatus;
import com.appfinanceiro.dto.request.CreditCardRequestDTO;
import com.appfinanceiro.dto.response.CreditCardInvoiceResponseDTO;
import com.appfinanceiro.dto.response.CreditCardResponseDTO;
import com.appfinanceiro.exception.ResourceNotFoundException;
import com.appfinanceiro.repository.CreditCardInvoiceRepository;
import com.appfinanceiro.repository.CreditCardRepository;
import com.appfinanceiro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreditCardService {

    private final CreditCardRepository creditCardRepository;
    private final CreditCardInvoiceRepository invoiceRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CreditCardResponseDTO> findAllByUser(UUID userId) {
        return creditCardRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CreditCardResponseDTO findByIdAndUser(UUID id, UUID userId) {
        CreditCard card = creditCardRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cartão de crédito", "id", id));
        return mapToResponse(card);
    }

    @Transactional
    public CreditCardResponseDTO create(UUID userId, CreditCardRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", userId));

        CreditCard card = CreditCard.builder()
                .user(user)
                .name(request.name())
                .creditLimit(request.creditLimit())
                .availableLimit(request.creditLimit())
                .closingDay(request.closingDay())
                .dueDay(request.dueDay())
                .cardBrand(request.cardBrand() != null ? request.cardBrand() : "Mastercard")
                .color(request.color() != null ? request.color() : "#3B82F6")
                .build();

        card = creditCardRepository.save(card);
        return mapToResponse(card);
    }

    @Transactional
    public CreditCardResponseDTO update(UUID id, UUID userId, CreditCardRequestDTO request) {
        CreditCard card = creditCardRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cartão de crédito", "id", id));

        BigDecimal diff = request.creditLimit().subtract(card.getCreditLimit());
        card.setName(request.name());
        card.setCreditLimit(request.creditLimit());
        card.setAvailableLimit(card.getAvailableLimit().add(diff));
        card.setClosingDay(request.closingDay());
        card.setDueDay(request.dueDay());
        if (request.cardBrand() != null) card.setCardBrand(request.cardBrand());
        if (request.color() != null) card.setColor(request.color());

        card = creditCardRepository.save(card);
        return mapToResponse(card);
    }

    @Transactional
    public void delete(UUID id, UUID userId) {
        CreditCard card = creditCardRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cartão de crédito", "id", id));
        creditCardRepository.delete(card);
    }

    @Transactional(readOnly = true)
    public List<CreditCardInvoiceResponseDTO> findInvoicesByCard(UUID cardId, UUID userId) {
        CreditCard card = creditCardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cartão de crédito", "id", cardId));

        return invoiceRepository.findByCreditCardId(card.getId()).stream()
                .map(this::mapInvoiceToResponse)
                .toList();
    }

    @Transactional
    public CreditCardInvoiceResponseDTO payInvoice(UUID invoiceId, UUID userId) {
        CreditCardInvoice invoice = invoiceRepository.findByIdAndUserId(invoiceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Fatura", "id", invoiceId));

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAmount(invoice.getTotalAmount());
        invoice = invoiceRepository.save(invoice);

        CreditCard card = invoice.getCreditCard();
        card.setAvailableLimit(card.getAvailableLimit().add(invoice.getTotalAmount()));
        if (card.getAvailableLimit().compareTo(card.getCreditLimit()) > 0) {
            card.setAvailableLimit(card.getCreditLimit());
        }
        creditCardRepository.save(card);

        return mapInvoiceToResponse(invoice);
    }

    public CreditCardResponseDTO mapToResponse(CreditCard card) {
        BigDecimal usedLimit = card.getCreditLimit().subtract(card.getAvailableLimit());
        if (usedLimit.compareTo(BigDecimal.ZERO) < 0) usedLimit = BigDecimal.ZERO;

        double usagePercentage = 0.0;
        if (card.getCreditLimit().compareTo(BigDecimal.ZERO) > 0) {
            usagePercentage = usedLimit.divide(card.getCreditLimit(), 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
        }

        return new CreditCardResponseDTO(
                card.getId(),
                card.getName(),
                card.getCreditLimit(),
                card.getAvailableLimit(),
                usedLimit,
                usagePercentage,
                card.getClosingDay(),
                card.getDueDay(),
                card.getCardBrand(),
                card.getColor()
        );
    }

    public CreditCardInvoiceResponseDTO mapInvoiceToResponse(CreditCardInvoice invoice) {
        return new CreditCardInvoiceResponseDTO(
                invoice.getId(),
                invoice.getCreditCard().getId(),
                invoice.getCreditCard().getName(),
                invoice.getReferenceMonth(),
                invoice.getReferenceYear(),
                invoice.getTotalAmount(),
                invoice.getPaidAmount(),
                invoice.getDueDate(),
                invoice.getClosingDate(),
                invoice.getStatus()
        );
    }
}
