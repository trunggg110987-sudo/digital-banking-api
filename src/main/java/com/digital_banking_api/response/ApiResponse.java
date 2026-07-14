package com.digital_banking_api.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private T data;

    private LocalDateTime timestamp;

    private String message;

    private int status;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, LocalDateTime.now(), "Success", 200);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(data, LocalDateTime.now(), message, 200);
    }

    public static <T> ApiResponse<T> error(int status, String message) {
        return new ApiResponse<>(null, LocalDateTime.now(), message, status);
    }
}
