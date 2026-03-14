package com.worldedu.worldeducation.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret:worldeducation-super-secret-key-for-jwt-token-generation-minimum-512-bits}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24 hours in milliseconds
    private Long expiration;

    /**
     * Generate JWT token for a user (legacy — no session binding)
     */
    public String generateToken(String userId, Long customerId, String userCategory) {
        return generateToken(userId, customerId, userCategory, null);
    }

    /**
     * Generate JWT token with session binding.
     * Embeds sessionId so every request can be validated against the DB session.
     */
    public String generateToken(String userId, Long customerId, String userCategory, Long sessionId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("customerId", customerId);
        claims.put("userCategory", userCategory);
        if (sessionId != null) {
            claims.put("sessionId", sessionId);
        }
        return createToken(claims, userId);
    }

    /**
     * Create JWT token with claims
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Get signing key from secret
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Extract username from token
     */
    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract customer ID from token
     */
    public Long extractCustomerId(String token) {
        return extractClaim(token, claims -> claims.get("customerId", Long.class));
    }

    /**
     * Extract user category from token
     */
    public String extractUserCategory(String token) {
        return extractClaim(token, claims -> claims.get("userCategory", String.class));
    }

    /**
     * Extract session ID from token (null if not present — legacy tokens without session binding)
     */
    public Long extractSessionId(String token) {
        return extractClaim(token, claims -> {
            Object val = claims.get("sessionId");
            if (val == null) return null;
            if (val instanceof Long) return (Long) val;
            if (val instanceof Integer) return ((Integer) val).longValue();
            if (val instanceof Number) return ((Number) val).longValue();
            return null;
        });
    }

    /**
     * Extract expiration date from token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract specific claim from token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Check if token is expired
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validate token
     */
    public Boolean validateToken(String token, String userId) {
        final String extractedUserId = extractUserId(token);
        return (extractedUserId.equals(userId) && !isTokenExpired(token));
    }

    /**
     * Validate token without user check
     */
    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            return false;
        }
    }
}
