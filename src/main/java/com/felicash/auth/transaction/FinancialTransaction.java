package com.felicash.auth.transaction;

import com.felicash.auth.user.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "financial_transactions")
public class FinancialTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "EUR";

    @Column(nullable = false)
    private LocalDate transactionDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // ─── Constructors ──────────────────────────────────────────────────────────

    public FinancialTransaction() {}

    public FinancialTransaction(UUID id, BigDecimal amount, String currency, LocalDate transactionDate,
                                String description, Category category, User user, Instant createdAt) {
        this.id = id;
        this.amount = amount;
        this.currency = currency != null ? currency : "EUR";
        this.transactionDate = transactionDate;
        this.description = description;
        this.category = category;
        this.user = user;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    // ─── Builder ───────────────────────────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private UUID id;
        private BigDecimal amount;
        private String currency = "EUR";
        private LocalDate transactionDate;
        private String description;
        private Category category;
        private User user;
        private Instant createdAt = Instant.now();

        private Builder() {}

        public Builder id(UUID id)                           { this.id = id; return this; }
        public Builder amount(BigDecimal amount)             { this.amount = amount; return this; }
        public Builder currency(String currency)             { this.currency = currency; return this; }
        public Builder transactionDate(LocalDate date)       { this.transactionDate = date; return this; }
        public Builder description(String description)       { this.description = description; return this; }
        public Builder category(Category category)           { this.category = category; return this; }
        public Builder user(User user)                       { this.user = user; return this; }
        public Builder createdAt(Instant createdAt)          { this.createdAt = createdAt; return this; }

        public FinancialTransaction build() {
            return new FinancialTransaction(id, amount, currency, transactionDate, description,
                    category, user, createdAt);
        }
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public UUID getId()                                      { return id; }
    public void setId(UUID id)                               { this.id = id; }
    public BigDecimal getAmount()                            { return amount; }
    public void setAmount(BigDecimal amount)                 { this.amount = amount; }
    public String getCurrency()                              { return currency; }
    public void setCurrency(String currency)                 { this.currency = currency; }
    public LocalDate getTransactionDate()                    { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate){ this.transactionDate = transactionDate; }
    public String getDescription()                           { return description; }
    public void setDescription(String description)           { this.description = description; }
    public Category getCategory()                            { return category; }
    public void setCategory(Category category)               { this.category = category; }
    public User getUser()                                    { return user; }
    public void setUser(User user)                           { this.user = user; }
    public Instant getCreatedAt()                            { return createdAt; }
    public void setCreatedAt(Instant createdAt)              { this.createdAt = createdAt; }
}
