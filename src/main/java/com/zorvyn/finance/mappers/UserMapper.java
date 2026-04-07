package com.zorvyn.finance.mappers;

import com.zorvyn.finance.dto.response.UserResponse;
import com.zorvyn.finance.model.User;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UserMapper {

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
