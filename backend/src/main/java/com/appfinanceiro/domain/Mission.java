package com.appfinanceiro.domain;

import com.appfinanceiro.domain.enums.MissionDifficulty;
import com.appfinanceiro.domain.enums.MissionFrequency;
import com.appfinanceiro.domain.enums.MissionStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "missions")
public class Mission {

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MissionFrequency frequency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MissionDifficulty difficulty;

    @Column(name = "xp_reward", nullable = false)
    private Long xpReward;

    @Column(name = "target_count", nullable = false)
    private Integer targetCount;

    @Column(name = "current_count", nullable = false)
    private Integer currentCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MissionStatus status = MissionStatus.ACTIVE;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Mission() {}

    public Mission(UUID id, User user, String title, String description, MissionFrequency frequency, MissionDifficulty difficulty, Long xpReward, Integer targetCount, Integer currentCount, MissionStatus status, LocalDate startDate, LocalDate endDate, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.title = title;
        this.description = description;
        this.frequency = frequency;
        this.difficulty = difficulty;
        this.xpReward = xpReward;
        this.targetCount = targetCount;
        this.currentCount = currentCount != null ? currentCount : 0;
        this.status = status != null ? status : MissionStatus.ACTIVE;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = createdAt;
    }

    public static MissionBuilder builder() {
        return new MissionBuilder();
    }

    public static class MissionBuilder {
        private UUID id;
        private User user;
        private String title;
        private String description;
        private MissionFrequency frequency;
        private MissionDifficulty difficulty;
        private Long xpReward;
        private Integer targetCount;
        private Integer currentCount = 0;
        private MissionStatus status = MissionStatus.ACTIVE;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalDateTime createdAt;

        public MissionBuilder id(UUID id) { this.id = id; return this; }
        public MissionBuilder user(User user) { this.user = user; return this; }
        public MissionBuilder title(String title) { this.title = title; return this; }
        public MissionBuilder description(String description) { this.description = description; return this; }
        public MissionBuilder frequency(MissionFrequency frequency) { this.frequency = frequency; return this; }
        public MissionBuilder difficulty(MissionDifficulty difficulty) { this.difficulty = difficulty; return this; }
        public MissionBuilder xpReward(Long xpReward) { this.xpReward = xpReward; return this; }
        public MissionBuilder targetCount(Integer targetCount) { this.targetCount = targetCount; return this; }
        public MissionBuilder currentCount(Integer currentCount) { this.currentCount = currentCount; return this; }
        public MissionBuilder status(MissionStatus status) { this.status = status; return this; }
        public MissionBuilder startDate(LocalDate startDate) { this.startDate = startDate; return this; }
        public MissionBuilder endDate(LocalDate endDate) { this.endDate = endDate; return this; }
        public MissionBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Mission build() {
            return new Mission(id, user, title, description, frequency, difficulty, xpReward, targetCount, currentCount, status, startDate, endDate, createdAt);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public MissionFrequency getFrequency() { return frequency; }
    public void setFrequency(MissionFrequency frequency) { this.frequency = frequency; }

    public MissionDifficulty getDifficulty() { return difficulty; }
    public void setDifficulty(MissionDifficulty difficulty) { this.difficulty = difficulty; }

    public Long getXpReward() { return xpReward; }
    public void setXpReward(Long xpReward) { this.xpReward = xpReward; }

    public Integer getTargetCount() { return targetCount; }
    public void setTargetCount(Integer targetCount) { this.targetCount = targetCount; }

    public Integer getCurrentCount() { return currentCount; }
    public void setCurrentCount(Integer currentCount) { this.currentCount = currentCount; }

    public MissionStatus getStatus() { return status; }
    public void setStatus(MissionStatus status) { this.status = status; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
