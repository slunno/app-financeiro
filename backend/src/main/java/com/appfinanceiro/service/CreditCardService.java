package com.appfinanceiro.service;

import com.appfinanceiro.domain.*;
import com.appfinanceiro.domain.enums.InvoiceStatus;
import com.appfinanceiro.domain.enums.PaymentMethod;
import com.appfinanceiro.domain.enums.TransactionStatus;
import com.appfinanceiro.domain.enums.TransactionType;
import com.appfinanceiro.dto.request.CreditCardRequestDTO;
import com.appfinanceiro.dto.request.PayInvoiceRequestDTO;
import com.appfinanceiro.dto.response.CreditCardInvoiceResponseDTO;
import com.appfinanceiro.dto.response.CreditCardResponseDTO;
import com.appfinanceiro.exception.ResourceNotFoundException;
import com.appfinanceiro.repository.*;
import com.appfinanceiro.service.gamification.GamificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreditCardService {

    private final CreditCardRepository creditCardRepository;
    private final CreditCardInvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final CardLimitService cardLimitService;
    private final GamificationService gamificationService;

    private static final UUID INVOICE_PAYMENT_CATEGORY_ID = UUID.fromString("00000000-0000-0000-0000-00000000000B");

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

        card.setName(request.name());
        card.setCreditLimit(request.creditLimit());
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

    /**
     * Paga fatura do cartão de crédito.
     * - Requer accountId (de qual conta sai o dinheiro)
     * - Cria Transaction EXPENSE na conta com categoria "Pagamento de Fatura"
     * - Suporta pagamento parcial (amount < totalAmount → PARTIALLY_PAID)
     * - Idempotente: rejeita fatura já totalmente paga (409 Conflict)
     */
    @Transactional
    public CreditCardInvoiceResponseDTO payInvoice(UUID invoiceId, UUID userId, PayInvoiceRequestDTO request) {
        CreditCardInvoice invoice = invoiceRepository.findByIdAndUserId(invoiceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Fatura", "id", invoiceId));

        // Idempotência: rejeitar fatura já totalmente paga
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta fatura já foi paga integralmente.");
        }

        Account account = accountRepository.findByIdAndUserIdAndArchivedAtIsNull(request.accountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta", "id", request.accountId()));

        User user = account.getUser();

        Category invoicePaymentCategory = categoryRepository.findById(INVOICE_PAYMENT_CATEGORY_ID)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", "id", INVOICE_PAYMENT_CATEGORY_ID));

        // Determinar valor do pagamento
        BigDecimal remaining = invoice.getTotalAmount().subtract(invoice.getPaidAmount());
        BigDecimal paymentAmount = (request.amount() != null) ? request.amount() : remaining;

        if (paymentAmount.compareTo(remaining) > 0) {
            throw new IllegalArgumentException("Valor de pagamento excede o saldo restante da fatura (R$ " + remaining + ").");
        }

        // Criar transação de despesa na conta
        CreditCard card = invoice.getCreditCard();
        Transaction paymentTx = Transaction.builder()
                .user(user)
                .account(account)
                .category(invoicePaymentCategory)
                .description("Pagamento fatura " + card.getName() + " - " +
                        String.format("%02d/%d", invoice.getReferenceMonth(), invoice.getReferenceYear()))
                .amount(paymentAmount)
                .date(LocalDate.now())
                .type(TransactionType.EXPENSE)
                .status(TransactionStatus.COMPLETED)
                .paymentMethod(PaymentMethod.TRANSFER)
                .build();
        transactionRepository.save(paymentTx);

        // Atualizar fatura
        invoice.setPaidAmount(invoice.getPaidAmount().add(paymentAmount));
        if (invoice.getPaidAmount().compareTo(invoice.getTotalAmount()) >= 0) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }
        invoice = invoiceRepository.save(invoice);

        // Gamificação: pagamento em dia concede XP
        if (invoice.getDueDate() != null && !LocalDate.now().isAfter(invoice.getDueDate())) {
            gamificationService.processTransactionEvent(user);
        }

        return mapInvoiceToResponse(invoice);
    }

    public CreditCardResponseDTO mapToResponse(CreditCard card) {
        BigDecimal availableLimit = cardLimitService.getAvailableLimit(card.getId());
        BigDecimal usedLimit = card.getCreditLimit().subtract(availableLimit);
        if (usedLimit.compareTo(BigDecimal.ZERO) < 0) usedLimit = BigDecimal.ZERO;

        double usagePercentage = 0.0;
        if (card.getCreditLimit().compareTo(BigDecimal.ZERO) > 0) {
            usagePercentage = usedLimit.divide(card.getCreditLimit(), 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
        }

        return new CreditCardResponseDTO(
                card.getId(),
                card.getName(),
                card.getCreditLimit(),
                availableLimit,
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
