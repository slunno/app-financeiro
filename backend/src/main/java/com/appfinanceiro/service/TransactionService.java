package com.appfinanceiro.service;

import com.appfinanceiro.domain.*;
import com.appfinanceiro.domain.enums.InvoiceStatus;
import com.appfinanceiro.domain.enums.PaymentMethod;
import com.appfinanceiro.domain.enums.TransactionStatus;
import com.appfinanceiro.domain.enums.TransactionType;
import com.appfinanceiro.dto.request.TransactionFilterDTO;
import com.appfinanceiro.dto.request.TransactionRequestDTO;
import com.appfinanceiro.dto.response.TransactionResponseDTO;
import com.appfinanceiro.exception.ResourceNotFoundException;
import com.appfinanceiro.repository.*;
import com.appfinanceiro.service.gamification.GamificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
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
    public Page<TransactionResponseDTO> findAllByUserPaginated(UUID userId, TransactionFilterDTO filter, Pageable pageable) {
        Specification<Transaction> spec = Specification.where(TransactionSpecification.belongsToUser(userId));

        if (filter != null) {
            spec = spec.and(TransactionSpecification.dateAfterOrEqual(filter.startDate()));
            spec = spec.and(TransactionSpecification.dateBeforeOrEqual(filter.endDate()));
            spec = spec.and(TransactionSpecification.hasAccount(filter.accountId()));
            spec = spec.and(TransactionSpecification.hasCreditCard(filter.creditCardId()));
            spec = spec.and(TransactionSpecification.hasCategory(filter.categoryId()));
            spec = spec.and(TransactionSpecification.hasType(filter.type()));
            spec = spec.and(TransactionSpecification.hasStatus(filter.status()));
            spec = spec.and(TransactionSpecification.descriptionContains(filter.search()));
        }

        return transactionRepository.findAll(spec, pageable).map(this::mapToResponse);
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
            account = accountRepository.findByIdAndUserIdAndArchivedAtIsNull(request.accountId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Conta", "id", request.accountId()));
        }

        CreditCard creditCard = null;
        if (request.creditCardId() != null) {
            creditCard = creditCardRepository.findByIdAndUserId(request.creditCardId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cartão de crédito", "id", request.creditCardId()));
        }

        // Exclusividade mútua: cartão de crédito e conta não podem coexistir
        if (creditCard != null && account != null) {
            throw new IllegalArgumentException("Transação em cartão de crédito não pode ter conta vinculada. Escolha cartão OU conta.");
        }

        List<TransactionResponseDTO> createdTransactions = new ArrayList<>();

        if (request.installmentsCount() != null && request.installmentsCount() > 1 && creditCard != null) {
            createdTransactions = createInstallmentTransactions(user, category, creditCard, request);
        } else {
            createdTransactions = List.of(createSingleTransaction(user, account, category, creditCard, request));
        }

        gamificationService.processTransactionEvent(user);
        return createdTransactions;
    }

    @Transactional
    public TransactionResponseDTO update(UUID id, UUID userId, TransactionRequestDTO request) {
        Transaction existing = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transação", "id", id));

        // Não permitir edição de transferências pelo endpoint normal
        if (existing.isTransfer()) {
            throw new IllegalArgumentException("Transferências não podem ser editadas. Exclua e crie uma nova.");
        }

        User user = existing.getUser();
        Category category = categoryRepository.findByIdAndUserId(request.categoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", "id", request.categoryId()));

        Account account = null;
        if (request.accountId() != null) {
            account = accountRepository.findByIdAndUserIdAndArchivedAtIsNull(request.accountId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Conta", "id", request.accountId()));
        }

        CreditCard creditCard = null;
        if (request.creditCardId() != null) {
            creditCard = creditCardRepository.findByIdAndUserId(request.creditCardId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cartão de crédito", "id", request.creditCardId()));
        }

        if (creditCard != null && account != null) {
            throw new IllegalArgumentException("Transação em cartão de crédito não pode ter conta vinculada.");
        }

        // Reverter efeitos na fatura antiga (se havia cartão)
        if (existing.getCreditCardInvoice() != null) {
            CreditCardInvoice oldInvoice = existing.getCreditCardInvoice();
            oldInvoice.setTotalAmount(oldInvoice.getTotalAmount().subtract(existing.getAmount()));
            invoiceRepository.save(oldInvoice);
        }

        // Aplicar novos valores
        existing.setAccount(account);
        existing.setCategory(category);
        existing.setCreditCard(creditCard);
        existing.setDescription(request.description());
        existing.setAmount(request.amount());
        existing.setDate(request.date());
        existing.setType(request.type());
        existing.setPaymentMethod(request.paymentMethod());
        existing.setIsRecurring(request.isRecurring() != null ? request.isRecurring() : false);
        existing.setRecurrencePeriod(request.recurrencePeriod());
        existing.setNotes(request.notes());

        // Reaplicar efeitos na fatura nova (se agora tem cartão)
        if (creditCard != null) {
            CreditCardInvoice newInvoice = getOrCreateInvoiceForCardAndDate(creditCard, request.date());
            newInvoice.setTotalAmount(newInvoice.getTotalAmount().add(request.amount()));
            invoiceRepository.save(newInvoice);
            existing.setCreditCardInvoice(newInvoice);
        } else {
            existing.setCreditCardInvoice(null);
        }

        existing = transactionRepository.save(existing);
        return mapToResponse(existing);
    }

    @Transactional
    public void delete(UUID id, UUID userId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transação", "id", id));

        // Se é transferência, excluir o par
        if (transaction.isTransfer()) {
            List<Transaction> pair = transactionRepository.findByTransferGroupId(transaction.getTransferGroupId());
            for (Transaction t : pair) {
                transactionRepository.delete(t);
            }
            return;
        }

        // Se é parte de um parcelamento, excluir todas as parcelas
        if (transaction.getCreditCard() != null && transaction.getDescription().matches(".*\\(\\d+/\\d+\\)$")) {
            String baseDesc = transaction.getDescription().replaceAll("\\s*\\(\\d+/\\d+\\)$", "");
            List<Transaction> installments = transactionRepository.findInstallmentTransactions(
                    transaction.getCreditCard().getId(), baseDesc + " (%");

            for (Transaction inst : installments) {
                if (inst.getCreditCardInvoice() != null) {
                    CreditCardInvoice invoice = inst.getCreditCardInvoice();
                    invoice.setTotalAmount(invoice.getTotalAmount().subtract(inst.getAmount()));
                    invoiceRepository.save(invoice);
                }
                transactionRepository.delete(inst);
            }
            return;
        }

        // Reverter efeito na fatura
        if (transaction.getCreditCardInvoice() != null) {
            CreditCardInvoice invoice = transaction.getCreditCardInvoice();
            invoice.setTotalAmount(invoice.getTotalAmount().subtract(transaction.getAmount()));
            invoiceRepository.save(invoice);
        }

        transactionRepository.delete(transaction);
    }

    private TransactionResponseDTO createSingleTransaction(User user, Account account, Category category,
                                                            CreditCard creditCard, TransactionRequestDTO request) {
        CreditCardInvoice invoice = null;
        if (creditCard != null) {
            invoice = getOrCreateInvoiceForCardAndDate(creditCard, request.date());
            invoice.setTotalAmount(invoice.getTotalAmount().add(request.amount()));
            invoiceRepository.save(invoice);
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
        return mapToResponse(t);
    }

    private List<TransactionResponseDTO> createInstallmentTransactions(User user, Category category,
                                                                       CreditCard creditCard, TransactionRequestDTO request) {
        int totalInstallments = request.installmentsCount();
        BigDecimal totalAmount = request.amount();
        BigDecimal baseInstallmentAmount = totalAmount.divide(BigDecimal.valueOf(totalInstallments), 2, RoundingMode.HALF_UP);

        // Soma das N-1 primeiras parcelas
        BigDecimal sumFirst = baseInstallmentAmount.multiply(BigDecimal.valueOf(totalInstallments - 1));
        // Última parcela absorve a diferença para garantir que a soma bata com o total
        BigDecimal lastInstallmentAmount = totalAmount.subtract(sumFirst);

        List<TransactionResponseDTO> createdTransactions = new ArrayList<>();

        Installment installment = Installment.builder()
                .user(user)
                .creditCard(creditCard)
                .description(request.description())
                .totalAmount(totalAmount)
                .installmentAmount(baseInstallmentAmount)
                .currentInstallment(1)
                .totalInstallments(totalInstallments)
                .startDate(request.date())
                .build();
        installment = installmentRepository.save(installment);

        for (int i = 1; i <= totalInstallments; i++) {
            LocalDate installmentDate = request.date().plusMonths(i - 1);
            BigDecimal amount = (i == totalInstallments) ? lastInstallmentAmount : baseInstallmentAmount;
            CreditCardInvoice invoice = getOrCreateInvoiceForCardAndDate(creditCard, installmentDate);

            Transaction t = Transaction.builder()
                    .user(user)
                    .category(category)
                    .creditCard(creditCard)
                    .creditCardInvoice(invoice)
                    .description(request.description() + " (" + i + "/" + totalInstallments + ")")
                    .amount(amount)
                    .date(installmentDate)
                    .type(TransactionType.EXPENSE)
                    .status(TransactionStatus.COMPLETED)
                    .paymentMethod(request.paymentMethod())
                    .isRecurring(false)
                    .notes(request.notes())
                    .build();

            t = transactionRepository.save(t);
            invoice.setTotalAmount(invoice.getTotalAmount().add(amount));
            invoiceRepository.save(invoice);

            createdTransactions.add(mapToResponse(t));
        }

        return createdTransactions;
    }

    /**
     * Determina a fatura correta para uma compra no cartão numa data específica.
     * Regra: compra no dia do fechamento (>=) entra na fatura do mês seguinte.
     * Usa YearMonth para respeitar o último dia real do mês.
     */
    CreditCardInvoice getOrCreateInvoiceForCardAndDate(CreditCard card, LocalDate transactionDate) {
        int closingDay = card.getClosingDay();

        // Compra no dia do fechamento ou depois entra na fatura seguinte
        YearMonth referenceMonth;
        if (transactionDate.getDayOfMonth() >= closingDay) {
            referenceMonth = YearMonth.from(transactionDate).plusMonths(1);
        } else {
            referenceMonth = YearMonth.from(transactionDate);
        }

        int refMonth = referenceMonth.getMonthValue();
        int refYear = referenceMonth.getYear();

        return invoiceRepository.findByCreditCardIdAndReferenceMonthAndReferenceYear(card.getId(), refMonth, refYear)
                .orElseGet(() -> {
                    // Closing date: dia de fechamento no mês de referência (usando último dia real do mês)
                    int actualClosingDay = Math.min(closingDay, referenceMonth.lengthOfMonth());
                    LocalDate closingDate = referenceMonth.atDay(actualClosingDay);

                    // Due date: se dueDay < closingDay, vencimento cai no mês seguinte ao fechamento
                    int dueDay = card.getDueDay();
                    YearMonth dueMonth = (dueDay < closingDay) ? referenceMonth.plusMonths(1) : referenceMonth;
                    int actualDueDay = Math.min(dueDay, dueMonth.lengthOfMonth());
                    LocalDate dueDate = dueMonth.atDay(actualDueDay);

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
                t.getNotes(),
                t.getTransferGroupId(),
                t.getTransferDirection()
        );
    }
}
