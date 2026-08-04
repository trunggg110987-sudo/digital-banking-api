package com.digital_banking_api.service.impl;

import com.digital_banking_api.entity.RefreshToken;
import com.digital_banking_api.entity.Role;
import com.digital_banking_api.entity.User;
import com.digital_banking_api.repository.RefreshTokenRepository;
import com.digital_banking_api.repository.UserRepository;
import com.digital_banking_api.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock UserRepository userRepository;
    @Mock JwtTokenProvider jwtTokenProvider;
    @InjectMocks RefreshTokenServiceImpl refreshTokenService;

    @Test
    void generateRefreshToken_success() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setRole(new Role());
        when(jwtTokenProvider.generateRefreshToken(user.getEmail())).thenReturn("refresh");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken token = refreshTokenService.generateRefreshToken(user);

        assertEquals("refresh", token.getToken());
    }

    @Test
    void validateRefreshToken_falseWhenMissing() {
        when(refreshTokenRepository.findByToken("bad")).thenReturn(Optional.empty());
        assertFalse(refreshTokenService.validateRefreshToken("bad"));
    }
}
