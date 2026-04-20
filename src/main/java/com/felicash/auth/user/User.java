package com.felicash.auth.user;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users",
        uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<Role> roles = new HashSet<>(Set.of(Role.ROLE_USER));

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // ─── Constructors ──────────────────────────────────────────────────────────

    public User() {}

    public User(UUID id, String name, String email, String password, Set<Role> roles, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.roles = roles != null ? roles : new HashSet<>(Set.of(Role.ROLE_USER));
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    // ─── Builder ───────────────────────────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private UUID id;
        private String name;
        private String email;
        private String password;
        private Set<Role> roles = new HashSet<>(Set.of(Role.ROLE_USER));
        private Instant createdAt = Instant.now();

        private Builder() {}

        public Builder id(UUID id)           { this.id = id; return this; }
        public Builder name(String name)     { this.name = name; return this; }
        public Builder email(String email)   { this.email = email; return this; }
        public Builder password(String pwd)  { this.password = pwd; return this; }
        public Builder roles(Set<Role> roles){ this.roles = roles; return this; }
        public Builder createdAt(Instant v)  { this.createdAt = v; return this; }

        public User build() {
            return new User(id, name, email, password, roles, createdAt);
        }
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public UUID getId()                      { return id; }
    public void setId(UUID id)               { this.id = id; }
    public String getName()                  { return name; }
    public void setName(String name)         { this.name = name; }
    public String getEmail()                 { return email; }
    public void setEmail(String email)       { this.email = email; }
    public String getPassword()              { return password; }
    public void setPassword(String password) { this.password = password; }
    public Set<Role> getRoles()              { return roles; }
    public void setRoles(Set<Role> roles)    { this.roles = roles; }
    public Instant getCreatedAt()            { return createdAt; }
    public void setCreatedAt(Instant v)      { this.createdAt = v; }
}
