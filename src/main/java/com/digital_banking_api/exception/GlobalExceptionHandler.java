package com.digital_banking_api.exception;

import com.digital_banking_api.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler{

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(404).body(ApiResponse.error(404, ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.status(400).body(ApiResponse.error(400, ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(401).body(ApiResponse.error(401, ex.getMessage()));
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ApiResponse<Void>> handleInsufficientBalance(InsufficientBalanceException ex) {
        return ResponseEntity.status(400).body(ApiResponse.error(400, ex.getMessage()));
    }

    @ExceptionHandler(AccountAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountAlreadyExists(AccountAlreadyExistsException ex) {
        return ResponseEntity.status(400).body(ApiResponse.error(400, ex.getMessage()));
    }
}
