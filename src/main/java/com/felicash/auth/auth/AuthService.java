package com.felicash.auth.auth;

import com.felicash.auth.auth.token.TokenRotationService;
import com.felicash.auth.user.User;
import com.felicash.auth.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService implements UserRegistrationFacade {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRotationService tokenRotationService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       TokenRotationService tokenRotationService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenRotationService = tokenRotationService;
        this.authenticationManager = authenticationManager;
    }

    // ─── Register ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered: " + request.email());
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build();

        userRepository.save(user);
        return tokenRotationService.issueTokenPair(user);
    }

    // ─── Login ─────────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalStateException("User not found after authentication"));

        // Revoke all previous refresh tokens (single-session security)
        tokenRotationService.revokeAllTokensForUser(user);

        return tokenRotationService.issueTokenPair(user);
    }

    // ─── Refresh ───────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        return tokenRotationService.rotateToken(request.refreshToken());
    }

    // ─── Logout ────────────────────────────────────────────────────────────────

    @Transactional
    public void logout(String email) {
        userRepository.findByEmail(email).ifPresent(tokenRotationService::revokeAllTokensForUser);
    }
}

