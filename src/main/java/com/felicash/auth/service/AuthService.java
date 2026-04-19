package com.felicash.auth.service;

import com.felicash.auth.config.JwtProperties;
import com.felicash.auth.domain.RefreshToken;
import com.felicash.auth.domain.User;
import com.felicash.auth.dto.AuthResponse;
import com.felicash.auth.dto.LoginRequest;
import com.felicash.auth.dto.RefreshRequest;
import com.felicash.auth.dto.RegisterRequest;
import com.felicash.auth.repository.RefreshTokenRepository;
import com.felicash.auth.repository.UserRepository;
import com.felicash.auth.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final JwtProperties jwtProperties;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       JwtProperties jwtProperties) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.jwtProperties = jwtProperties;
    }

    // ─── Register ──────────────────────────────────────────────────────────────

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
        return issueTokenPair(user);
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
        refreshTokenRepository.revokeAllUserTokens(user);

        return issueTokenPair(user);
    }

    // ─── Refresh ───────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));

        if (!storedToken.isValid()) {
            throw new IllegalArgumentException("Refresh token is expired or revoked");
        }

        // Rotate: revoke current token, issue a fresh pair
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        return issueTokenPair(storedToken.getUser());
    }

    // ─── Logout ────────────────────────────────────────────────────────────────

    @Transactional
    public void logout(String email) {
        userRepository.findByEmail(email).ifPresent(refreshTokenRepository::revokeAllUserTokens);
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private AuthResponse issueTokenPair(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String rawRefreshToken = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(rawRefreshToken)
                .user(user)
                .expiresAt(Instant.now().plusMillis(jwtProperties.getRefreshTokenExpiration()))
                .build();

        refreshTokenRepository.save(refreshToken);

        return AuthResponse.of(accessToken, rawRefreshToken);
    }
}

