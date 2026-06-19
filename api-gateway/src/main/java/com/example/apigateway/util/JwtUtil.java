package com.example.apigateway.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    public void validateToken(String token) {

        Jwts.parserBuilder()
                .setSigningKey(
                        Keys.hmacShaKeyFor(
                                secret.getBytes()
                        )
                )
                .build()
                .parseClaimsJws(token);
    }

    public Claims extractClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(
                        Keys.hmacShaKeyFor(
                                secret.getBytes()
                        )
                )
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    public String extractUsername(String token) {

        return extractClaims(token)
                .getSubject();
    }
}