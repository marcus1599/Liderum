package com.example.Liderum.Security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Objects;


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
    public String generateToken(CustomUserDetails user, List<String> roles) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiration)
                .claim("roles", roles) // melhor usar claim direto
                .claim("uid", user.getUserId())
                .claim("cv", user.getCredentialVersion())
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return getAllClaims(token).getSubject();
    }

    public boolean identifiesCurrentUser(String token, CustomUserDetails user) {
        try {
            Claims claims = getAllClaims(token);
            return Objects.equals(claims.getSubject(), user.getUsername())
                    && numericClaimEquals(claims.get("uid"), user.getUserId())
                    && numericClaimEquals(claims.get("cv"), user.getCredentialVersion());
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private boolean numericClaimEquals(Object claim, long expected) {
        return claim instanceof Number number
                && number.doubleValue() == number.longValue()
                && number.longValue() == expected;
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
