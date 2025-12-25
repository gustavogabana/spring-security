package com.example.demo.domain.user;

import com.example.demo.domain.enums.UserRole;

public record RegisterDTO(
        String login,
        String password,
        UserRole role
) {
}
