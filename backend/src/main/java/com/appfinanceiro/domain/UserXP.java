package com.appfinanceiro.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_xp")
public class UserXP {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "current_level", nullable = false)
    private Integer currentLevel = 1;

    @Column(name = "current_xp", nullable = false)
    private Long currentXp = 0L;

    @Column(name = "total_xp", nullable = false)
    private Long totalXp = 0L;

    @Column(name = "streak_days", nullable = false)
    private Integer streakDays = 1;

    @Column(name = "last_activity_date")
    private LocalDate lastActivityDate;

    @Column(name = "financial_health_score", nullable = false)
    private Integer financialHealthScore = 70;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public UserXP() {}

    public UserXP(UUID id, User user, Integer currentLevel, Long currentXp, Long totalXp, Integer streakDays, LocalDate lastActivityDate, Integer financialHealthScore, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.user = user;
        this.currentLevel = currentLevel != null ? currentLevel : 1;
        this.currentXp = currentXp != null ? currentXp : 0L;
        this.totalXp = totalXp != null ? totalXp : 0L;
        this.streakDays = streakDays != null ? streakDays : 1;
        this.lastActivityDate = lastActivityDate;
        this.financialHealthScore = financialHealthScore != null ? financialHealthScore : 70;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt;
    }

    public static UserXPBuilder builder() {
        return new UserXPBuilder();
    }

    public static class UserXPBuilder {
        private UUID id;
        private User user;
        private Integer currentLevel = 1;
        private Long currentXp = 0L;
        private Long totalXp = 0L;
        private Integer streakDays = 1;
        private LocalDate lastActivityDate;
        private Integer financialHealthScore = 70;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt;

        public UserXPBuilder id(UUID id) { this.id = id; return this; }
        public UserXPBuilder user(User user) { this.user = user; return this; }
        public UserXPBuilder currentLevel(Integer currentLevel) { this.currentLevel = currentLevel; return this; }
        public UserXPBuilder currentXp(Long currentXp) { this.currentXp = currentXp; return this; }
        public UserXPBuilder totalXp(Long totalXp) { this.totalXp = totalXp; return this; }
        public UserXPBuilder streakDays(Integer streakDays) { this.streakDays = streakDays; return this; }
        public UserXPBuilder lastActivityDate(LocalDate lastActivityDate) { this.lastActivityDate = lastActivityDate; return this; }
        public UserXPBuilder financialHealthScore(Integer financialHealthScore) { this.financialHealthScore = financialHealthScore; return this; }
        public UserXPBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public UserXPBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public UserXP build() {
            return new UserXP(id, user, currentLevel, currentXp, totalXp, streakDays, lastActivityDate, financialHealthScore, createdAt, updatedAt);
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

    public Integer getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(Integer currentLevel) { this.currentLevel = currentLevel; }

    public Long getCurrentXp() { return currentXp; }
    public void setCurrentXp(Long currentXp) { this.currentXp = currentXp; }

    public Long getTotalXp() { return totalXp; }
    public void setTotalXp(Long totalXp) { this.totalXp = totalXp; }

    public Integer getStreakDays() { return streakDays; }
    public void setStreakDays(Integer streakDays) { this.streakDays = streakDays; }

    public LocalDate getLastActivityDate() { return lastActivityDate; }
    public void setLastActivityDate(LocalDate lastActivityDate) { this.lastActivityDate = lastActivityDate; }

    public Integer getFinancialHealthScore() { return financialHealthScore; }
    public void setFinancialHealthScore(Integer financialHealthScore) { this.financialHealthScore = financialHealthScore; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
