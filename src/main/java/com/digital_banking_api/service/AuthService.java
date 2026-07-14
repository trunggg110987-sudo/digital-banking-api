package com.digital_banking_api.service;

import com.digital_banking_api.dto.request.LoginRequest;
import com.digital_banking_api.dto.request.RegisterRequest;
import com.digital_banking_api.dto.response.LoginResponse;
import com.digital_banking_api.dto.response.RegisterResponse;

public interface AuthService {
    RegisterResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
}
