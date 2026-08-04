package com.digital_banking_api.service.impl;

import com.digital_banking_api.dto.request.LoginRequest;
import com.digital_banking_api.dto.request.RegisterRequest;
import com.digital_banking_api.dto.response.LoginResponse;
import com.digital_banking_api.dto.response.RegisterResponse;
import com.digital_banking_api.entity.Role;
import com.digital_banking_api.entity.User;
import com.digital_banking_api.enums.UserStatus;
import com.digital_banking_api.exception.BadRequestException;
import com.digital_banking_api.exception.ResourceNotFoundException;
import com.digital_banking_api.exception.UnauthorizedException;
import com.digital_banking_api.repository.RoleRepository;
import com.digital_banking_api.repository.UserRepository;
import com.digital_banking_api.security.JwtTokenProvider;
import com.digital_banking_api.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock UserRepository userRepository;
    @Mock RoleRepository roleRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtTokenProvider jwtTokenProvider;
    @Mock RefreshTokenService refreshTokenService;
    @InjectMocks AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setName("CUSTOMER");

        registerRequest = new RegisterRequest("test@example.com", "password123", "Test User", "1234567890");
        loginRequest = new LoginRequest("test@example.com", "password123");

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encoded");
        user.setFullName("Test User");
        user.setPhone("1234567890");
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(role);
    }

    @Test
    void register_success() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPhone(registerRequest.getPhone())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encoded");
        when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenReturn(user);

        RegisterResponse response = authService.register(registerRequest);

        assertEquals("test@example.com", response.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_emailExists() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);
        assertThrows(BadRequestException.class, () -> authService.register(registerRequest));
    }

    @Test
    void login_success() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(user.getEmail())).thenReturn("access");
        when(refreshTokenService.generateRefreshToken(user)).thenReturn(new com.digital_banking_api.entity.RefreshToken("refresh", user, java.time.LocalDateTime.now().plusDays(7), false));

        LoginResponse response = authService.login(loginRequest);

        assertEquals("access", response.getAccessToken());
        assertEquals("refresh", response.getRefreshToken());
    }

    @Test
    void login_wrongPassword() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(false);
        assertThrows(UnauthorizedException.class, () -> authService.login(loginRequest));
    }

    @Test
    void login_userNotFound() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> authService.login(loginRequest));
    }
}
