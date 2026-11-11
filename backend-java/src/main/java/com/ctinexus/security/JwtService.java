package com.ctinexus.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    @Value("${auth.jwt.secret:}")
    private String configuredSecret;

    @Value("${auth.jwt.expiration-ms:3600000}")
    private long expirationMs;

    private SecretKey signingKey;

    @PostConstruct
    void initialize() {
        if (configuredSecret == null || configuredSecret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET precisa ser configurada.");
        }

        byte[] keyBytes = configuredSecret.startsWith("base64:")
                ? Decoders.BASE64.decode(configuredSecret.substring("base64:".length()))
                : configuredSecret.getBytes(StandardCharsets.UTF_8);
        signingKey = Keys.hmacShaKeyFor(keyBytes);

        if (expirationMs <= 0) {
            throw new IllegalStateException("JWT_EXPIRATION_MS precisa ser positivo.");
        }
    }

    public String issueToken(String email, int tokenVersion) {
        Date issuedAt = new Date();
        return Jwts.builder()
                .subject(email)
                .claim("tokenVersion", tokenVersion)
                .issuedAt(issuedAt)
                .expiration(new Date(issuedAt.getTime() + expirationMs))
                .signWith(signingKey)
                .compact();
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public int extractTokenVersion(String token) {
        return parseClaims(token).get("tokenVersion", Integer.class);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
