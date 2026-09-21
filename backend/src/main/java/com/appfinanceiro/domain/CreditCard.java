package com.appfinanceiro.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "credit_cards")
public class CreditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "credit_limit", nullable = false, precision = 15, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "closing_day", nullable = false)
    private Integer closingDay;

    @Column(name = "due_day", nullable = false)
    private Integer dueDay;

    @Column(name = "card_brand", length = 50)
    private String cardBrand;

    @Column(length = 20)
    private String color;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public CreditCard() {}

    public CreditCard(UUID id, User user, String name, BigDecimal creditLimit,
                      Integer closingDay, Integer dueDay, String cardBrand, String color,
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.user = user;
        this.name = name;
        this.creditLimit = creditLimit;
        this.closingDay = closingDay;
        this.dueDay = dueDay;
        this.cardBrand = cardBrand;
        this.color = color;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CreditCardBuilder builder() {
        return new CreditCardBuilder();
    }

    public static class CreditCardBuilder {
        private UUID id;
        private User user;
        private String name;
        private BigDecimal creditLimit;
        private Integer closingDay;
        private Integer dueDay;
        private String cardBrand;
        private String color;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public CreditCardBuilder id(UUID id) { this.id = id; return this; }
        public CreditCardBuilder user(User user) { this.user = user; return this; }
        public CreditCardBuilder name(String name) { this.name = name; return this; }
        public CreditCardBuilder creditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; return this; }
        public CreditCardBuilder closingDay(Integer closingDay) { this.closingDay = closingDay; return this; }
        public CreditCardBuilder dueDay(Integer dueDay) { this.dueDay = dueDay; return this; }
        public CreditCardBuilder cardBrand(String cardBrand) { this.cardBrand = cardBrand; return this; }
        public CreditCardBuilder color(String color) { this.color = color; return this; }
        public CreditCardBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public CreditCardBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public CreditCard build() {
            return new CreditCard(id, user, name, creditLimit, closingDay, dueDay, cardBrand, color, createdAt, updatedAt);
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

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }

    public Integer getClosingDay() { return closingDay; }
    public void setClosingDay(Integer closingDay) { this.closingDay = closingDay; }

    public Integer getDueDay() { return dueDay; }
    public void setDueDay(Integer dueDay) { this.dueDay = dueDay; }

    public String getCardBrand() { return cardBrand; }
    public void setCardBrand(String cardBrand) { this.cardBrand = cardBrand; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
