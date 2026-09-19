package com.appfinanceiro.domain;

import com.appfinanceiro.domain.enums.InvoiceStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "credit_card_invoices")
public class CreditCardInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_card_id", nullable = false)
    private CreditCard creditCard;

    @Column(name = "reference_month", nullable = false)
    private Integer referenceMonth;

    @Column(name = "reference_year", nullable = false)
    private Integer referenceYear;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "paid_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "closing_date", nullable = false)
    private LocalDate closingDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceStatus status = InvoiceStatus.OPEN;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public CreditCardInvoice() {}

    public CreditCardInvoice(UUID id, CreditCard creditCard, Integer referenceMonth, Integer referenceYear, BigDecimal totalAmount, BigDecimal paidAmount, LocalDate dueDate, LocalDate closingDate, InvoiceStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.creditCard = creditCard;
        this.referenceMonth = referenceMonth;
        this.referenceYear = referenceYear;
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
        this.paidAmount = paidAmount != null ? paidAmount : BigDecimal.ZERO;
        this.dueDate = dueDate;
        this.closingDate = closingDate;
        this.status = status != null ? status : InvoiceStatus.OPEN;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CreditCardInvoiceBuilder builder() {
        return new CreditCardInvoiceBuilder();
    }

    public static class CreditCardInvoiceBuilder {
        private UUID id;
        private CreditCard creditCard;
        private Integer referenceMonth;
        private Integer referenceYear;
        private BigDecimal totalAmount = BigDecimal.ZERO;
        private BigDecimal paidAmount = BigDecimal.ZERO;
        private LocalDate dueDate;
        private LocalDate closingDate;
        private InvoiceStatus status = InvoiceStatus.OPEN;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public CreditCardInvoiceBuilder id(UUID id) { this.id = id; return this; }
        public CreditCardInvoiceBuilder creditCard(CreditCard creditCard) { this.creditCard = creditCard; return this; }
        public CreditCardInvoiceBuilder referenceMonth(Integer referenceMonth) { this.referenceMonth = referenceMonth; return this; }
        public CreditCardInvoiceBuilder referenceYear(Integer referenceYear) { this.referenceYear = referenceYear; return this; }
        public CreditCardInvoiceBuilder totalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; return this; }
        public CreditCardInvoiceBuilder paidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; return this; }
        public CreditCardInvoiceBuilder dueDate(LocalDate dueDate) { this.dueDate = dueDate; return this; }
        public CreditCardInvoiceBuilder closingDate(LocalDate closingDate) { this.closingDate = closingDate; return this; }
        public CreditCardInvoiceBuilder status(InvoiceStatus status) { this.status = status; return this; }
        public CreditCardInvoiceBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public CreditCardInvoiceBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public CreditCardInvoice build() {
            return new CreditCardInvoice(id, creditCard, referenceMonth, referenceYear, totalAmount, paidAmount, dueDate, closingDate, status, createdAt, updatedAt);
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public CreditCard getCreditCard() { return creditCard; }
    public void setCreditCard(CreditCard creditCard) { this.creditCard = creditCard; }

    public Integer getReferenceMonth() { return referenceMonth; }
    public void setReferenceMonth(Integer referenceMonth) { this.referenceMonth = referenceMonth; }

    public Integer getReferenceYear() { return referenceYear; }
    public void setReferenceYear(Integer referenceYear) { this.referenceYear = referenceYear; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDate getClosingDate() { return closingDate; }
    public void setClosingDate(LocalDate closingDate) { this.closingDate = closingDate; }

    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
