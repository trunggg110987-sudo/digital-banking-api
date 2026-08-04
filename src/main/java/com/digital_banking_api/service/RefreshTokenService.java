package com.digital_banking_api.service;

import com.digital_banking_api.entity.RefreshToken;
import com.digital_banking_api.entity.User;
import java.util.Optional;

public interface RefreshTokenService {
    RefreshToken generateRefreshToken(User user);

    boolean validateRefreshToken(String token);

    String getEmailFromRefreshToken(String token);

    void revokeRefreshToken(String token);

    void revokeAllUserTokens(Long userId);

    Optional<RefreshToken> findByToken(String token);
}
