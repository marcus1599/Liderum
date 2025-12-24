package com.example.Liderum.Security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;


@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationTime;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // Geração de token com múltiplas roles
    public String generateToken(String username, List<String> roles) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .claim("roles", roles) // melhor usar claim direto
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return getAllClaims(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = getAllClaims(token);
        Object rawRoles = claims.get("roles");

        if (rawRoles instanceof List<?>) {
            return ((List<?>) rawRoles).stream()
                    .map(Object::toString)
                    .toList();
        }
        return List.of(); // Retorna lista vazia se nada for encontrado
    }

    public boolean validateToken(String token) {
        try {
            getAllClaims(token); // valida assinatura e expiração
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims getAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
