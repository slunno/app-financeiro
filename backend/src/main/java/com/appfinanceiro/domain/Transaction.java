package com.appfinanceiro.domain;

import com.appfinanceiro.domain.enums.PaymentMethod;
import com.appfinanceiro.domain.enums.TransactionStatus;
import com.appfinanceiro.domain.enums.TransactionType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_card_id")
    private CreditCard creditCard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_card_invoice_id")
    private CreditCardInvoice creditCardInvoice;

    @Column(nullable = false, length = 200)
    private String description;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status = TransactionStatus.COMPLETED;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Column(name = "is_recurring", nullable = false)
    private Boolean isRecurring = false;

    @Column(name = "recurrence_period", length = 20)
    private String recurrencePeriod;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Transaction() {}

    public Transaction(UUID id, User user, Account account, Category category, CreditCard creditCard, CreditCardInvoice creditCardInvoice, String description, BigDecimal amount, LocalDate date, TransactionType type, TransactionStatus status, PaymentMethod paymentMethod, Boolean isRecurring, String recurrencePeriod, String notes, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.user = user;
        this.account = account;
        this.category = category;
        this.creditCard = creditCard;
        this.creditCardInvoice = creditCardInvoice;
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.type = type;
        this.status = status != null ? status : TransactionStatus.COMPLETED;
        this.paymentMethod = paymentMethod;
        this.isRecurring = isRecurring != null ? isRecurring : false;
        this.recurrencePeriod = recurrencePeriod;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static TransactionBuilder builder() {
        return new TransactionBuilder();
    }

    public static class TransactionBuilder {
        private UUID id;
        private User user;
        private Account account;
        private Category category;
        private CreditCard creditCard;
        private CreditCardInvoice creditCardInvoice;
        private String description;
        private BigDecimal amount;
        private LocalDate date;
        private TransactionType type;
        private TransactionStatus status = TransactionStatus.COMPLETED;
        private PaymentMethod paymentMethod;
        private Boolean isRecurring = false;
        private String recurrencePeriod;
        private String notes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public TransactionBuilder id(UUID id) { this.id = id; return this; }
        public TransactionBuilder user(User user) { this.user = user; return this; }
        public TransactionBuilder account(Account account) { this.account = account; return this; }
        public TransactionBuilder category(Category category) { this.category = category; return this; }
        public TransactionBuilder creditCard(CreditCard creditCard) { this.creditCard = creditCard; return this; }
        public TransactionBuilder creditCardInvoice(CreditCardInvoice creditCardInvoice) { this.creditCardInvoice = creditCardInvoice; return this; }
        public TransactionBuilder description(String description) { this.description = description; return this; }
        public TransactionBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public TransactionBuilder date(LocalDate date) { this.date = date; return this; }
        public TransactionBuilder type(TransactionType type) { this.type = type; return this; }
        public TransactionBuilder status(TransactionStatus status) { this.status = status; return this; }
        public TransactionBuilder paymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; return this; }
        public TransactionBuilder isRecurring(Boolean isRecurring) { this.isRecurring = isRecurring; return this; }
        public TransactionBuilder recurrencePeriod(String recurrencePeriod) { this.recurrencePeriod = recurrencePeriod; return this; }
        public TransactionBuilder notes(String notes) { this.notes = notes; return this; }
        public TransactionBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public TransactionBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Transaction build() {
            return new Transaction(id, user, account, category, creditCard, creditCardInvoice, description, amount, date, type, status, paymentMethod, isRecurring, recurrencePeriod, notes, createdAt, updatedAt);
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public CreditCard getCreditCard() { return creditCard; }
    public void setCreditCard(CreditCard creditCard) { this.creditCard = creditCard; }

    public CreditCardInvoice getCreditCardInvoice() { return creditCardInvoice; }
    public void setCreditCardInvoice(CreditCardInvoice creditCardInvoice) { this.creditCardInvoice = creditCardInvoice; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public Boolean getIsRecurring() { return isRecurring; }
    public void setIsRecurring(Boolean isRecurring) { this.isRecurring = isRecurring; }

    public String getRecurrencePeriod() { return recurrencePeriod; }
    public void setRecurrencePeriod(String recurrencePeriod) { this.recurrencePeriod = recurrencePeriod; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
