package com.felicash.auth.auth.token;

import com.felicash.auth.auth.AuthResponse;
import com.felicash.auth.user.User;

public interface TokenRotationService {
    AuthResponse issueTokenPair(User user);
    AuthResponse rotateToken(String rawRefreshToken);
    void revokeAllTokensForUser(User user);
}

