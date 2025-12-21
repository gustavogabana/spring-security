package com.example.demo.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.demo.entity.User;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class TokenConfig {

    private String secret = "secret";

    public String generateToken(User user) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.create()
                .withClaim("userId", user.getId())
                .withSubject(user.getEmail())
                .withExpiresAt(Instant.now().plusSeconds(86400))
                .withIssuedAt(Instant.now())
                .sign(algorithm);
    }

    public Optional<JWTUserData> validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            DecodedJWT decode = JWT.require(algorithm).build().verify(token);
            Long userId = decode.getClaim("userId").asLong();
            String email = decode.getSubject();
            JWTUserData jwtUserData = JWTUserData.builder()
                    .userId(userId)
                    .email(email)
                    .build();
            return Optional.of(jwtUserData);
        } catch (JWTVerificationException e) {
            System.out.println("Erro ao validar token: " + e.getMessage());
            return Optional.empty();
        }
    }

}
