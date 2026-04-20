package com.felicash.auth.auth.token;

import com.felicash.auth.user.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Instant expiresAt;

    private boolean revoked = false;

    // ─── Constructors ──────────────────────────────────────────────────────────

    public RefreshToken() {}

    public RefreshToken(UUID id, String token, User user, Instant expiresAt, boolean revoked) {
        this.id = id;
        this.token = token;
        this.user = user;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
    }

    // ─── Builder ───────────────────────────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private UUID id;
        private String token;
        private User user;
        private Instant expiresAt;
        private boolean revoked = false;

        private Builder() {}

        public Builder id(UUID id)          { this.id = id; return this; }
        public Builder token(String token)  { this.token = token; return this; }
        public Builder user(User user)      { this.user = user; return this; }
        public Builder expiresAt(Instant v) { this.expiresAt = v; return this; }
        public Builder revoked(boolean v)   { this.revoked = v; return this; }

        public RefreshToken build() {
            return new RefreshToken(id, token, user, expiresAt, revoked);
        }
    }

    // ─── Business logic ────────────────────────────────────────────────────────

    public boolean isExpired() { return Instant.now().isAfter(expiresAt); }
    public boolean isValid()   { return !revoked && !isExpired(); }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public UUID getId()                 { return id; }
    public void setId(UUID id)          { this.id = id; }
    public String getToken()            { return token; }
    public void setToken(String token)  { this.token = token; }
    public User getUser()               { return user; }
    public void setUser(User user)      { this.user = user; }
    public Instant getExpiresAt()       { return expiresAt; }
    public void setExpiresAt(Instant v) { this.expiresAt = v; }
    public boolean isRevoked()          { return revoked; }
    public void setRevoked(boolean v)   { this.revoked = v; }
}

