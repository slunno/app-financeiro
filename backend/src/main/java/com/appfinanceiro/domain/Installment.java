package com.appfinanceiro.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "installments")
public class Installment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_card_id")
    private CreditCard creditCard;

    @Column(nullable = false, length = 200)
    private String description;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "installment_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal installmentAmount;

    @Column(name = "current_installment", nullable = false)
    private Integer currentInstallment;

    @Column(name = "total_installments", nullable = false)
    private Integer totalInstallments;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Installment() {}

    public Installment(UUID id, User user, Transaction transaction, CreditCard creditCard, String description, BigDecimal totalAmount, BigDecimal installmentAmount, Integer currentInstallment, Integer totalInstallments, LocalDate startDate, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.transaction = transaction;
        this.creditCard = creditCard;
        this.description = description;
        this.totalAmount = totalAmount;
        this.installmentAmount = installmentAmount;
        this.currentInstallment = currentInstallment;
        this.totalInstallments = totalInstallments;
        this.startDate = startDate;
        this.createdAt = createdAt;
    }

    public static InstallmentBuilder builder() {
        return new InstallmentBuilder();
    }

    public static class InstallmentBuilder {
        private UUID id;
        private User user;
        private Transaction transaction;
        private CreditCard creditCard;
        private String description;
        private BigDecimal totalAmount;
        private BigDecimal installmentAmount;
        private Integer currentInstallment;
        private Integer totalInstallments;
        private LocalDate startDate;
        private LocalDateTime createdAt;

        public InstallmentBuilder id(UUID id) { this.id = id; return this; }
        public InstallmentBuilder user(User user) { this.user = user; return this; }
        public InstallmentBuilder transaction(Transaction transaction) { this.transaction = transaction; return this; }
        public InstallmentBuilder creditCard(CreditCard creditCard) { this.creditCard = creditCard; return this; }
        public InstallmentBuilder description(String description) { this.description = description; return this; }
        public InstallmentBuilder totalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; return this; }
        public InstallmentBuilder installmentAmount(BigDecimal installmentAmount) { this.installmentAmount = installmentAmount; return this; }
        public InstallmentBuilder currentInstallment(Integer currentInstallment) { this.currentInstallment = currentInstallment; return this; }
        public InstallmentBuilder totalInstallments(Integer totalInstallments) { this.totalInstallments = totalInstallments; return this; }
        public InstallmentBuilder startDate(LocalDate startDate) { this.startDate = startDate; return this; }
        public InstallmentBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Installment build() {
            return new Installment(id, user, transaction, creditCard, description, totalAmount, installmentAmount, currentInstallment, totalInstallments, startDate, createdAt);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Transaction getTransaction() { return transaction; }
    public void setTransaction(Transaction transaction) { this.transaction = transaction; }

    public CreditCard getCreditCard() { return creditCard; }
    public void setCreditCard(CreditCard creditCard) { this.creditCard = creditCard; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getInstallmentAmount() { return installmentAmount; }
    public void setInstallmentAmount(BigDecimal installmentAmount) { this.installmentAmount = installmentAmount; }

    public Integer getCurrentInstallment() { return currentInstallment; }
    public void setCurrentInstallment(Integer currentInstallment) { this.currentInstallment = currentInstallment; }

    public Integer getTotalInstallments() { return totalInstallments; }
    public void setTotalInstallments(Integer totalInstallments) { this.totalInstallments = totalInstallments; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
