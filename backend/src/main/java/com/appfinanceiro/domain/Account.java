package com.appfinanceiro.domain;

import com.appfinanceiro.domain.enums.AccountType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AccountType type;

    @Column(name = "initial_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal initialBalance = BigDecimal.ZERO;

    @Column(name = "current_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(length = 20)
    private String color;

    @Column(length = 50)
    private String icon;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Account() {}

    public Account(UUID id, User user, String name, AccountType type, BigDecimal initialBalance, BigDecimal currentBalance, String bankName, String color, String icon, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.user = user;
        this.name = name;
        this.type = type;
        this.initialBalance = initialBalance != null ? initialBalance : BigDecimal.ZERO;
        this.currentBalance = currentBalance != null ? currentBalance : BigDecimal.ZERO;
        this.bankName = bankName;
        this.color = color;
        this.icon = icon;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static AccountBuilder builder() {
        return new AccountBuilder();
    }

    public static class AccountBuilder {
        private UUID id;
        private User user;
        private String name;
        private AccountType type;
        private BigDecimal initialBalance = BigDecimal.ZERO;
        private BigDecimal currentBalance = BigDecimal.ZERO;
        private String bankName;
        private String color;
        private String icon;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public AccountBuilder id(UUID id) { this.id = id; return this; }
        public AccountBuilder user(User user) { this.user = user; return this; }
        public AccountBuilder name(String name) { this.name = name; return this; }
        public AccountBuilder type(AccountType type) { this.type = type; return this; }
        public AccountBuilder initialBalance(BigDecimal initialBalance) { this.initialBalance = initialBalance; return this; }
        public AccountBuilder currentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; return this; }
        public AccountBuilder bankName(String bankName) { this.bankName = bankName; return this; }
        public AccountBuilder color(String color) { this.color = color; return this; }
        public AccountBuilder icon(String icon) { this.icon = icon; return this; }
        public AccountBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public AccountBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Account build() {
            return new Account(id, user, name, type, initialBalance, currentBalance, bankName, color, icon, createdAt, updatedAt);
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

    public AccountType getType() { return type; }
    public void setType(AccountType type) { this.type = type; }

    public BigDecimal getInitialBalance() { return initialBalance; }
    public void setInitialBalance(BigDecimal initialBalance) { this.initialBalance = initialBalance; }

    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
