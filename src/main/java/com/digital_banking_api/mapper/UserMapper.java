package com.digital_banking_api.mapper;

import com.digital_banking_api.dto.response.UserResponse;
import com.digital_banking_api.entity.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setPhone(user.getPhone());
        response.setStatus(user.getStatus());
        response.setRoleName(user.getRole() != null ? user.getRole().getName() : null);
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}
