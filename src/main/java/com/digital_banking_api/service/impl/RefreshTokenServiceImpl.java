package com.digital_banking_api.service.impl;

import com.digital_banking_api.entity.RefreshToken;
import com.digital_banking_api.entity.User;
import com.digital_banking_api.repository.RefreshTokenRepository;
import com.digital_banking_api.repository.UserRepository;
import com.digital_banking_api.security.JwtTokenProvider;
import com.digital_banking_api.service.RefreshTokenService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    
    private static final long REFRESH_TOKEN_EXPIRE_TIME_DAYS = 7;
    
    @Override
    public RefreshToken generateRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);
        String token = jwtTokenProvider.generateRefreshToken(user.getEmail());
        
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRE_TIME_DAYS));
        refreshToken.setRevoked(false);
        
        return refreshTokenRepository.save(refreshToken);
    }
    
    @Override
    public boolean validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElse(null);
        
        if (refreshToken == null) {
            return false;
        }
        
        if (refreshToken.isRevoked()) {
            return false;
        }
        
        if (LocalDateTime.now().isAfter(refreshToken.getExpiryDate())) {
            return false;
        }
        
        return jwtTokenProvider.validateToken(token);
    }
    
    @Override
    public String getEmailFromRefreshToken(String token) {
        return jwtTokenProvider.extractEmailFromToken(token);
    }
    
    @Override
    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElse(null);
        
        if (refreshToken != null) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
        }
    }
    
    @Override
    public void revokeAllUserTokens(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        
        if (user != null) {
            List<RefreshToken> tokens = refreshTokenRepository.findByUserAndRevokedFalse(user);
            tokens.forEach(token -> token.setRevoked(true));
            refreshTokenRepository.saveAll(tokens);
        }
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
}
