package com.felicash.auth.transaction;

import com.felicash.auth.user.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionCategoryType type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // ─── Constructors ──────────────────────────────────────────────────────────

    public Category() {}

    public Category(UUID id, String name, TransactionCategoryType type, User user, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.user = user;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    // ─── Builder ───────────────────────────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private UUID id;
        private String name;
        private TransactionCategoryType type;
        private User user;
        private Instant createdAt = Instant.now();

        private Builder() {}

        public Builder id(UUID id)                                 { this.id = id; return this; }
        public Builder name(String name)                           { this.name = name; return this; }
        public Builder type(TransactionCategoryType type)          { this.type = type; return this; }
        public Builder user(User user)                             { this.user = user; return this; }
        public Builder createdAt(Instant createdAt)                { this.createdAt = createdAt; return this; }

        public Category build() {
            return new Category(id, name, type, user, createdAt);
        }
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public UUID getId()                                      { return id; }
    public void setId(UUID id)                               { this.id = id; }
    public String getName()                                  { return name; }
    public void setName(String name)                         { this.name = name; }
    public TransactionCategoryType getType()                 { return type; }
    public void setType(TransactionCategoryType type)        { this.type = type; }
    public User getUser()                                    { return user; }
    public void setUser(User user)                           { this.user = user; }
    public Instant getCreatedAt()                            { return createdAt; }
    public void setCreatedAt(Instant createdAt)              { this.createdAt = createdAt; }
}
