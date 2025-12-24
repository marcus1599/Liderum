package com.example.Liderum.Security.Jwt;

import com.example.Liderum.Enums.GuildRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationTime;

    private Key signingKey;

    @PostConstruct
    public void init() {
        signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (JwtException e) {
            // Pode logar aqui se desejar
            return null;
        }
    }

    public GuildRole extractRole(String token) {
        try {
            String roleStr = extractAllClaims(token).get("role", String.class);
            if (roleStr == null) return null;
            return GuildRole.valueOf(roleStr);
        } catch (JwtException | IllegalArgumentException e) {
            // JwtException: token inválido ou expirado
            // IllegalArgumentException: valor inválido para enum
            return null;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails, GuildRole role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role.name());
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, String username) {
        final String tokenUsername = extractUsername(token);
        return tokenUsername != null && tokenUsername.equals(username) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        return expiration != null && expiration.before(new Date());
    }

    private Date extractExpiration(String token) {
        try {
            return extractClaim(token, Claims::getExpiration);
        } catch (JwtException e) {
            return null;
        }
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            // token inválido, expirado, malformado, etc.
            return null;
        }
    }
}
