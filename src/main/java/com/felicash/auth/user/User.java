package com.felicash.auth.user;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users",
        uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class User implements UserDetails {

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

    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    private boolean enabled = true;

    // ─── Constructors ──────────────────────────────────────────────────────────

    public User() {}

    public User(UUID id, String name, String email, String password, Set<Role> roles,
                Instant createdAt, boolean accountNonExpired, boolean accountNonLocked,
                boolean credentialsNonExpired, boolean enabled) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.roles = roles != null ? roles : new HashSet<>(Set.of(Role.ROLE_USER));
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
        this.enabled = enabled;
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
        private boolean accountNonExpired = true;
        private boolean accountNonLocked = true;
        private boolean credentialsNonExpired = true;
        private boolean enabled = true;

        private Builder() {}

        public Builder id(UUID id)                      { this.id = id; return this; }
        public Builder name(String name)                { this.name = name; return this; }
        public Builder email(String email)              { this.email = email; return this; }
        public Builder password(String password)        { this.password = password; return this; }
        public Builder roles(Set<Role> roles)           { this.roles = roles; return this; }
        public Builder createdAt(Instant v)             { this.createdAt = v; return this; }
        public Builder accountNonExpired(boolean v)     { this.accountNonExpired = v; return this; }
        public Builder accountNonLocked(boolean v)      { this.accountNonLocked = v; return this; }
        public Builder credentialsNonExpired(boolean v) { this.credentialsNonExpired = v; return this; }
        public Builder enabled(boolean v)               { this.enabled = v; return this; }

        public User build() {
            return new User(id, name, email, password, roles, createdAt,
                    accountNonExpired, accountNonLocked, credentialsNonExpired, enabled);
        }
    }

    // ─── UserDetails ───────────────────────────────────────────────────────────

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(r -> new SimpleGrantedAuthority(r.name())).toList();
    }

    @Override public String getUsername()               { return email; }
    @Override public boolean isAccountNonExpired()      { return accountNonExpired; }
    @Override public boolean isAccountNonLocked()       { return accountNonLocked; }
    @Override public boolean isCredentialsNonExpired()  { return credentialsNonExpired; }
    @Override public boolean isEnabled()                { return enabled; }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public UUID getId()                             { return id; }
    public void setId(UUID id)                      { this.id = id; }
    public String getName()                         { return name; }
    public void setName(String name)                { this.name = name; }
    public String getEmail()                        { return email; }
    public void setEmail(String email)              { this.email = email; }
    @Override public String getPassword()           { return password; }
    public void setPassword(String password)        { this.password = password; }
    public Set<Role> getRoles()                     { return roles; }
    public void setRoles(Set<Role> roles)           { this.roles = roles; }
    public Instant getCreatedAt()                   { return createdAt; }
    public void setCreatedAt(Instant v)             { this.createdAt = v; }
    public void setAccountNonExpired(boolean v)     { this.accountNonExpired = v; }
    public void setAccountNonLocked(boolean v)      { this.accountNonLocked = v; }
    public void setCredentialsNonExpired(boolean v) { this.credentialsNonExpired = v; }
    public void setEnabled(boolean v)               { this.enabled = v; }
}

