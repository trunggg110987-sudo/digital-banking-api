package com.digital_banking_api.controller;

import com.digital_banking_api.dto.request.LoginRequest;
import com.digital_banking_api.dto.request.RegisterRequest;
import com.digital_banking_api.dto.request.RefreshTokenRequest;
import com.digital_banking_api.dto.response.LoginResponse;
import com.digital_banking_api.dto.response.RegisterResponse;
import com.digital_banking_api.entity.User;
import com.digital_banking_api.response.ApiResponse;
import com.digital_banking_api.service.AuthService;
import com.digital_banking_api.service.RefreshTokenService;
import com.digital_banking_api.repository.UserRepository;
import com.digital_banking_api.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication APIs")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(AuthService authService,
                          RefreshTokenService refreshTokenService,
                          UserRepository userRepository,
                          JwtTokenProvider jwtTokenProvider) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        RegisterResponse response = authService.register(registerRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/login")
    @Operation(summary = "User Login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!refreshTokenService.validateRefreshToken(refreshToken)) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "Invalid refresh token"));
        }

        String email = refreshTokenService.getEmailFromRefreshToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        LoginResponse response = new LoginResponse();
        response.setAccessToken(jwtTokenProvider.generateAccessToken(user.getEmail()));
        response.setRefreshToken(refreshToken);
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setRole(user.getRole().getName());

        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        refreshTokenService.revokeRefreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
    }
}
