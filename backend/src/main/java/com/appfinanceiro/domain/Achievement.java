package com.appfinanceiro.domain;

import com.appfinanceiro.domain.enums.AchievementCategory;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "achievements")
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    private String icon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AchievementCategory category;

    @Column(name = "xp_reward", nullable = false)
    private Long xpReward;

    @Column(name = "condition_type", nullable = false, length = 50)
    private String conditionType;

    @Column(name = "condition_threshold", nullable = false)
    private Long conditionThreshold;

    public Achievement() {}

    public Achievement(UUID id, String code, String title, String description, String icon, AchievementCategory category, Long xpReward, String conditionType, Long conditionThreshold) {
        this.id = id;
        this.code = code;
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.category = category;
        this.xpReward = xpReward;
        this.conditionType = conditionType;
        this.conditionThreshold = conditionThreshold;
    }

    public static AchievementBuilder builder() {
        return new AchievementBuilder();
    }

    public static class AchievementBuilder {
        private UUID id;
        private String code;
        private String title;
        private String description;
        private String icon;
        private AchievementCategory category;
        private Long xpReward;
        private String conditionType;
        private Long conditionThreshold;

        public AchievementBuilder id(UUID id) { this.id = id; return this; }
        public AchievementBuilder code(String code) { this.code = code; return this; }
        public AchievementBuilder title(String title) { this.title = title; return this; }
        public AchievementBuilder description(String description) { this.description = description; return this; }
        public AchievementBuilder icon(String icon) { this.icon = icon; return this; }
        public AchievementBuilder category(AchievementCategory category) { this.category = category; return this; }
        public AchievementBuilder xpReward(Long xpReward) { this.xpReward = xpReward; return this; }
        public AchievementBuilder conditionType(String conditionType) { this.conditionType = conditionType; return this; }
        public AchievementBuilder conditionThreshold(Long conditionThreshold) { this.conditionThreshold = conditionThreshold; return this; }

        public Achievement build() {
            return new Achievement(id, code, title, description, icon, category, xpReward, conditionType, conditionThreshold);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public AchievementCategory getCategory() { return category; }
    public void setCategory(AchievementCategory category) { this.category = category; }

    public Long getXpReward() { return xpReward; }
    public void setXpReward(Long xpReward) { this.xpReward = xpReward; }

    public String getConditionType() { return conditionType; }
    public void setConditionType(String conditionType) { this.conditionType = conditionType; }

    public Long getConditionThreshold() { return conditionThreshold; }
    public void setConditionThreshold(Long conditionThreshold) { this.conditionThreshold = conditionThreshold; }
}
