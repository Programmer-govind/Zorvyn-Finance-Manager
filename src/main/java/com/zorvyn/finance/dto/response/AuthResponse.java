package com.zorvyn.finance.dto.response;

import com.zorvyn.finance.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String tokenType;
    private Long expiresIn;
    private Long userId;
    private String fullName;
    private String email;
    private Role role;

    public static AuthResponse of(String token, long expiresIn,
                                   Long userId, String fullName,
                                   String email, Role role) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .userId(userId)
                .fullName(fullName)
                .email(email)
                .role(role)
                .build();
    }
}