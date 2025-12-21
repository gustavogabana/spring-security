package com.example.demo.config;

import lombok.Builder;

@Builder
public record JWTUserData(
        Long userId,
        String email
) { }
