package com.felicash.auth.auth.token;

import com.felicash.auth.auth.AuthResponse;
import com.felicash.auth.security.JwtService;
import com.felicash.auth.user.User;
import com.felicash.auth.user.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
class TokenRotationServiceImpl implements TokenRotationService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    TokenRotationServiceImpl(RefreshTokenRepository refreshTokenRepository, JwtService jwtService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }

    // ─── Issue a brand-new access + refresh token pair ─────────────────────────

    @Override
    @Transactional
    public AuthResponse issueTokenPair(User user) {
        String accessToken = jwtService.generateAccessToken(new UserPrincipal(user));
        String rawRefreshToken = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(rawRefreshToken)
                .user(user)
                .expiresAt(jwtService.generateRefreshTokenExpiry())
                .build();

        refreshTokenRepository.save(refreshToken);
        return AuthResponse.of(accessToken, rawRefreshToken);
    }

    // ─── Validate, revoke old token, issue new pair (rotation) ─────────────────

    @Override
    @Transactional
    public AuthResponse rotateToken(String rawRefreshToken) {
        RefreshToken stored = refreshTokenRepository.findByToken(rawRefreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));

        if (!stored.isValid()) {
            throw new IllegalArgumentException("Refresh token is expired or revoked");
        }

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return issueTokenPair(stored.getUser());
    }

    // ─── Revoke all active tokens for a user ───────────────────────────────────

    @Override
    @Transactional
    public void revokeAllTokensForUser(User user) {
        refreshTokenRepository.revokeAllUserTokens(user);
    }
}

