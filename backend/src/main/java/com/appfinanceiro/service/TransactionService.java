package com.appfinanceiro.service;

import com.appfinanceiro.domain.*;
import com.appfinanceiro.domain.enums.InvoiceStatus;
import com.appfinanceiro.domain.enums.TransactionStatus;
import com.appfinanceiro.domain.enums.TransactionType;
import com.appfinanceiro.dto.request.TransactionRequestDTO;
import com.appfinanceiro.dto.response.TransactionResponseDTO;
import com.appfinanceiro.exception.ResourceNotFoundException;
import com.appfinanceiro.repository.*;
import com.appfinanceiro.service.gamification.GamificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final CreditCardRepository creditCardRepository;
    private final CreditCardInvoiceRepository invoiceRepository;
    private final InstallmentRepository installmentRepository;
    private final GamificationService gamificationService;

    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> findAllByUser(UUID userId) {
        return transactionRepository.findByUserIdOrderByDateDesc(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponseDTO findByIdAndUser(UUID id, UUID userId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transação", "id", id));
        return mapToResponse(transaction);
    }

    @Transactional
    public List<TransactionResponseDTO> create(UUID userId, TransactionRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", userId));

        Category category = categoryRepository.findByIdAndUserId(request.categoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", "id", request.categoryId()));

        Account account = null;
        if (request.accountId() != null) {
            account = accountRepository.findByIdAndUserId(request.accountId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Conta", "id", request.accountId()));
        }

        CreditCard creditCard = null;
        if (request.creditCardId() != null) {
            creditCard = creditCardRepository.findByIdAndUserId(request.creditCardId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cartão de crédito", "id", request.creditCardId()));
        }

        List<TransactionResponseDTO> createdTransactions = new ArrayList<>();

        // Se for parcelado (installmentsCount > 1)
        if (request.installmentsCount() != null && request.installmentsCount() > 1 && creditCard != null) {
            int totalInstallments = request.installmentsCount();
            BigDecimal installmentAmount = request.amount().divide(BigDecimal.valueOf(totalInstallments), 2, RoundingMode.HALF_UP);

            for (int i = 1; i <= totalInstallments; i++) {
                LocalDate installmentDate = request.date().plusMonths(i - 1);
                CreditCardInvoice invoice = getOrCreateInvoiceForCardAndDate(creditCard, installmentDate);

                Transaction t = Transaction.builder()
                        .user(user)
                        .category(category)
                        .creditCard(creditCard)
                        .creditCardInvoice(invoice)
                        .description(request.description() + " (" + i + "/" + totalInstallments + ")")
                        .amount(installmentAmount)
                        .date(installmentDate)
                        .type(TransactionType.EXPENSE)
                        .status(TransactionStatus.COMPLETED)
                        .paymentMethod(request.paymentMethod())
                        .isRecurring(false)
                        .notes(request.notes())
                        .build();

                t = transactionRepository.save(t);
                invoice.setTotalAmount(invoice.getTotalAmount().add(installmentAmount));
                invoiceRepository.save(invoice);

                createdTransactions.add(mapToResponse(t));
            }

            creditCard.setAvailableLimit(creditCard.getAvailableLimit().subtract(request.amount()));
            creditCardRepository.save(creditCard);

            Installment installment = Installment.builder()
                    .user(user)
                    .creditCard(creditCard)
                    .description(request.description())
                    .totalAmount(request.amount())
                    .installmentAmount(installmentAmount)
                    .currentInstallment(1)
                    .totalInstallments(totalInstallments)
                    .startDate(request.date())
                    .build();
            installmentRepository.save(installment);

        } else {
            // Transação normal
            CreditCardInvoice invoice = null;
            if (creditCard != null) {
                invoice = getOrCreateInvoiceForCardAndDate(creditCard, request.date());
                invoice.setTotalAmount(invoice.getTotalAmount().add(request.amount()));
                invoiceRepository.save(invoice);

                creditCard.setAvailableLimit(creditCard.getAvailableLimit().subtract(request.amount()));
                creditCardRepository.save(creditCard);
            }

            Transaction t = Transaction.builder()
                    .user(user)
                    .account(account)
                    .category(category)
                    .creditCard(creditCard)
                    .creditCardInvoice(invoice)
                    .description(request.description())
                    .amount(request.amount())
                    .date(request.date())
                    .type(request.type())
                    .status(TransactionStatus.COMPLETED)
                    .paymentMethod(request.paymentMethod())
                    .isRecurring(request.isRecurring() != null ? request.isRecurring() : false)
                    .recurrencePeriod(request.recurrencePeriod())
                    .notes(request.notes())
                    .build();

            t = transactionRepository.save(t);

            // Atualiza saldo da conta se houver conta vinculada
            if (account != null) {
                if (request.type() == TransactionType.INCOME) {
                    account.setCurrentBalance(account.getCurrentBalance().add(request.amount()));
                } else if (request.type() == TransactionType.EXPENSE) {
                    account.setCurrentBalance(account.getCurrentBalance().subtract(request.amount()));
                }
                accountRepository.save(account);
            }

            createdTransactions.add(mapToResponse(t));
        }

        // Aciona o motor de gamificação para recompensar o usuário
        gamificationService.processTransactionEvent(user);

        return createdTransactions;
    }

    @Transactional
    public void delete(UUID id, UUID userId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transação", "id", id));

        // Reverte efeitos de saldo/limite
        if (transaction.getAccount() != null) {
            Account account = transaction.getAccount();
            if (transaction.getType() == TransactionType.INCOME) {
                account.setCurrentBalance(account.getCurrentBalance().subtract(transaction.getAmount()));
            } else if (transaction.getType() == TransactionType.EXPENSE) {
                account.setCurrentBalance(account.getCurrentBalance().add(transaction.getAmount()));
            }
            accountRepository.save(account);
        }

        if (transaction.getCreditCard() != null) {
            CreditCard card = transaction.getCreditCard();
            card.setAvailableLimit(card.getAvailableLimit().add(transaction.getAmount()));
            creditCardRepository.save(card);

            if (transaction.getCreditCardInvoice() != null) {
                CreditCardInvoice invoice = transaction.getCreditCardInvoice();
                invoice.setTotalAmount(invoice.getTotalAmount().subtract(transaction.getAmount()));
                invoiceRepository.save(invoice);
            }
        }

        transactionRepository.delete(transaction);
    }

    private CreditCardInvoice getOrCreateInvoiceForCardAndDate(CreditCard card, LocalDate date) {
        int month = date.getMonthValue();
        int year = date.getYear();

        // Se o dia da transação for posterior ao dia de fechamento, entra na fatura do mês seguinte
        if (date.getDayOfMonth() >= card.getClosingDay()) {
            LocalDate nextMonth = date.plusMonths(1);
            month = nextMonth.getMonthValue();
            year = nextMonth.getYear();
        }

        final int refMonth = month;
        final int refYear = year;

        return invoiceRepository.findByCreditCardIdAndReferenceMonthAndReferenceYear(card.getId(), refMonth, refYear)
                .orElseGet(() -> {
                    LocalDate dueDate = LocalDate.of(refYear, refMonth, Math.min(card.getDueDay(), 28));
                    LocalDate closingDate = LocalDate.of(refYear, refMonth, Math.min(card.getClosingDay(), 28)).minusDays(7);

                    CreditCardInvoice newInvoice = CreditCardInvoice.builder()
                            .creditCard(card)
                            .referenceMonth(refMonth)
                            .referenceYear(refYear)
                            .totalAmount(BigDecimal.ZERO)
                            .paidAmount(BigDecimal.ZERO)
                            .dueDate(dueDate)
                            .closingDate(closingDate)
                            .status(InvoiceStatus.OPEN)
                            .build();

                    return invoiceRepository.save(newInvoice);
                });
    }

    public TransactionResponseDTO mapToResponse(Transaction t) {
        return new TransactionResponseDTO(
                t.getId(),
                t.getAccount() != null ? t.getAccount().getId() : null,
                t.getAccount() != null ? t.getAccount().getName() : null,
                t.getCategory().getId(),
                t.getCategory().getName(),
                t.getCategory().getIcon(),
                t.getCategory().getColor(),
                t.getCreditCard() != null ? t.getCreditCard().getId() : null,
                t.getCreditCard() != null ? t.getCreditCard().getName() : null,
                t.getDescription(),
                t.getAmount(),
                t.getDate(),
                t.getType(),
                t.getStatus(),
                t.getPaymentMethod(),
                t.getIsRecurring(),
                t.getNotes()
        );
    }
}
