package com.example.demo.domain.user;

public record AuthenticationDTO(
        String login,
        String password
) {
}
