package com.appfinanceiro.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "goal_contributions")
public class GoalContribution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "contribution_date", nullable = false)
    private LocalDate contributionDate;

    @Column(length = 255)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public GoalContribution() {}

    public GoalContribution(UUID id, Goal goal, BigDecimal amount, LocalDate contributionDate, String notes, LocalDateTime createdAt) {
        this.id = id;
        this.goal = goal;
        this.amount = amount;
        this.contributionDate = contributionDate;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public static GoalContributionBuilder builder() {
        return new GoalContributionBuilder();
    }

    public static class GoalContributionBuilder {
        private UUID id;
        private Goal goal;
        private BigDecimal amount;
        private LocalDate contributionDate;
        private String notes;
        private LocalDateTime createdAt;

        public GoalContributionBuilder id(UUID id) { this.id = id; return this; }
        public GoalContributionBuilder goal(Goal goal) { this.goal = goal; return this; }
        public GoalContributionBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public GoalContributionBuilder contributionDate(LocalDate contributionDate) { this.contributionDate = contributionDate; return this; }
        public GoalContributionBuilder notes(String notes) { this.notes = notes; return this; }
        public GoalContributionBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public GoalContribution build() {
            return new GoalContribution(id, goal, amount, contributionDate, notes, createdAt);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Goal getGoal() { return goal; }
    public void setGoal(Goal goal) { this.goal = goal; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDate getContributionDate() { return contributionDate; }
    public void setContributionDate(LocalDate contributionDate) { this.contributionDate = contributionDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
