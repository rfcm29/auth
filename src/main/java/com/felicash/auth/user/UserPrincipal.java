package com.felicash.auth.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * Adapter that bridges the domain {@link User} entity and Spring Security's {@link UserDetails}.
 * Keeps security concerns out of the JPA entity.
 */
public final class UserPrincipal implements UserDetails {

    private final User user;

    public UserPrincipal(User user) {
        this.user = user;
    }

    // ─── Expose the underlying domain object ──────────────────────────────────

    public User getUser() {
        return user;
    }

    // ─── UserDetails ──────────────────────────────────────────────────────────

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority(r.name()))
                .toList();
    }

    @Override public String getPassword()              { return user.getPassword(); }
    @Override public String getUsername()              { return user.getEmail(); }
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }
}

