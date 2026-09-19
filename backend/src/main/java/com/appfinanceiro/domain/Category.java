package com.appfinanceiro.domain;

import com.appfinanceiro.domain.enums.TransactionType;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String icon;

    @Column(nullable = false, length = 20)
    private String color;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(name = "is_system_default", nullable = false)
    private Boolean isSystemDefault = false;

    public Category() {}

    public Category(UUID id, User user, String name, String icon, String color, TransactionType type, Boolean isSystemDefault) {
        this.id = id;
        this.user = user;
        this.name = name;
        this.icon = icon;
        this.color = color;
        this.type = type;
        this.isSystemDefault = isSystemDefault != null ? isSystemDefault : false;
    }

    public static CategoryBuilder builder() {
        return new CategoryBuilder();
    }

    public static class CategoryBuilder {
        private UUID id;
        private User user;
        private String name;
        private String icon;
        private String color;
        private TransactionType type;
        private Boolean isSystemDefault = false;

        public CategoryBuilder id(UUID id) { this.id = id; return this; }
        public CategoryBuilder user(User user) { this.user = user; return this; }
        public CategoryBuilder name(String name) { this.name = name; return this; }
        public CategoryBuilder icon(String icon) { this.icon = icon; return this; }
        public CategoryBuilder color(String color) { this.color = color; return this; }
        public CategoryBuilder type(TransactionType type) { this.type = type; return this; }
        public CategoryBuilder isSystemDefault(Boolean isSystemDefault) { this.isSystemDefault = isSystemDefault; return this; }

        public Category build() {
            return new Category(id, user, name, icon, color, type, isSystemDefault);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public Boolean getIsSystemDefault() { return isSystemDefault; }
    public void setIsSystemDefault(Boolean isSystemDefault) { this.isSystemDefault = isSystemDefault; }
}
