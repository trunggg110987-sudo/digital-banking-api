package com.digital_banking_api.service.impl;

import com.digital_banking_api.dto.request.LoginRequest;
import com.digital_banking_api.dto.request.RegisterRequest;
import com.digital_banking_api.dto.response.LoginResponse;
import com.digital_banking_api.dto.response.RegisterResponse;
import com.digital_banking_api.entity.Role;
import com.digital_banking_api.entity.User;
import com.digital_banking_api.entity.UserStatus;
import com.digital_banking_api.exception.BadRequestException;
import com.digital_banking_api.exception.ResourceNotFoundException;
import com.digital_banking_api.exception.UnauthorizedException;
import com.digital_banking_api.repository.RoleRepository;
import com.digital_banking_api.repository.UserRepository;
import com.digital_banking_api.security.JwtTokenProvider;
import com.digital_banking_api.service.AuthService;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    @Override
    public RegisterResponse register(RegisterRequest request) {
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        if(userRepository.existsByPhone(request.getPhone())) {
            throw new BadRequestException("Phone already exists");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        Role role = roleRepository.findByName("CUSTOMER").orElseThrow(()
                -> new IllegalArgumentException("Role not found"));
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(encodedPassword);
        user.setRole(role);
        user.setFullName(request.getFullName());
        user.setStatus(UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);

        RegisterResponse response = new RegisterResponse();

        response.setId(savedUser.getId());
        response.setEmail(savedUser.getEmail());
        response.setFullName(savedUser.getFullName());
        response.setMessage("Register successfully");
        return response;
    }

    @Transactional
    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Email not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Wrong password");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Account is not active");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

        LoginResponse response = new LoginResponse();
        response.setRole(user.getRole().getName());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setRefreshToken(refreshToken);
        response.setAccessToken(accessToken);
        return response;
    }
}
