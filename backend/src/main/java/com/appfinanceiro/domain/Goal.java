package com.appfinanceiro.domain;

import com.appfinanceiro.domain.enums.GoalStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "goals")
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "target_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal targetAmount;

    @Column(name = "current_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal currentAmount = BigDecimal.ZERO;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(name = "category_icon", length = 50)
    private String categoryIcon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GoalStatus status = GoalStatus.IN_PROGRESS;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Goal() {}

    public Goal(UUID id, User user, String title, String description, BigDecimal targetAmount, BigDecimal currentAmount, LocalDate targetDate, String categoryIcon, GoalStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.user = user;
        this.title = title;
        this.description = description;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount != null ? currentAmount : BigDecimal.ZERO;
        this.targetDate = targetDate;
        this.categoryIcon = categoryIcon;
        this.status = status != null ? status : GoalStatus.IN_PROGRESS;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static GoalBuilder builder() {
        return new GoalBuilder();
    }

    public static class GoalBuilder {
        private UUID id;
        private User user;
        private String title;
        private String description;
        private BigDecimal targetAmount;
        private BigDecimal currentAmount = BigDecimal.ZERO;
        private LocalDate targetDate;
        private String categoryIcon;
        private GoalStatus status = GoalStatus.IN_PROGRESS;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public GoalBuilder id(UUID id) { this.id = id; return this; }
        public GoalBuilder user(User user) { this.user = user; return this; }
        public GoalBuilder title(String title) { this.title = title; return this; }
        public GoalBuilder description(String description) { this.description = description; return this; }
        public GoalBuilder targetAmount(BigDecimal targetAmount) { this.targetAmount = targetAmount; return this; }
        public GoalBuilder currentAmount(BigDecimal currentAmount) { this.currentAmount = currentAmount; return this; }
        public GoalBuilder targetDate(LocalDate targetDate) { this.targetDate = targetDate; return this; }
        public GoalBuilder categoryIcon(String categoryIcon) { this.categoryIcon = categoryIcon; return this; }
        public GoalBuilder status(GoalStatus status) { this.status = status; return this; }
        public GoalBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public GoalBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Goal build() {
            return new Goal(id, user, title, description, targetAmount, currentAmount, targetDate, categoryIcon, status, createdAt, updatedAt);
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

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getTargetAmount() { return targetAmount; }
    public void setTargetAmount(BigDecimal targetAmount) { this.targetAmount = targetAmount; }

    public BigDecimal getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(BigDecimal currentAmount) { this.currentAmount = currentAmount; }

    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }

    public String getCategoryIcon() { return categoryIcon; }
    public void setCategoryIcon(String categoryIcon) { this.categoryIcon = categoryIcon; }

    public GoalStatus getStatus() { return status; }
    public void setStatus(GoalStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
