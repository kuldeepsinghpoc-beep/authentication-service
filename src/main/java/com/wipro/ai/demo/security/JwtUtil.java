package com.wipro.ai.demo.security;

import com.wipro.ai.demo.exception.InvalidTokenException;
import com.wipro.ai.demo.exception.TokenExpiredException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class for JWT token operations including generation, validation, and claims extraction.
 * 
 * This class provides comprehensive JWT token management using HMAC-SHA512 algorithm
 * for secure token signing and validation. It supports both access tokens and refresh tokens
 * with configurable expiration times.
 * 
 * Features:
 * - JWT token generation with custom claims
 * - Token validation with signature verification
 * - Claims extraction (username, expiration, etc.)
 * - Support for refresh tokens
 * - Comprehensive error handling
 * - Configurable token expiration
 * 
 * @author Wipro AI Demo Team
 * @version 1.0.0
 * @since 2024-10-03
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    /**
     * Secret key for JWT signing from application properties.
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Access token expiration time in seconds from application properties.
     */
    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    /**
     * Refresh token expiration time in seconds from application properties.
     */
    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    /**
     * Generate a secret key from the configured secret string.
     * 
     * @return SecretKey for JWT signing
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Extract username from JWT token.
     * 
     * @param token the JWT token
     * @return the username
     * @throws InvalidTokenException if token is invalid
     */
    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception e) {
            logger.error("Error extracting username from token: {}", e.getMessage());
            throw new InvalidTokenException("Invalid token: Unable to extract username");
        }
    }

    /**
     * Extract expiration date from JWT token.
     * 
     * @param token the JWT token
     * @return the expiration date
     * @throws InvalidTokenException if token is invalid
     */
    public Date extractExpiration(String token) {
        try {
            return extractClaim(token, Claims::getExpiration);
        } catch (Exception e) {
            logger.error("Error extracting expiration from token: {}", e.getMessage());
            throw new InvalidTokenException("Invalid token: Unable to extract expiration");
        }
    }

    /**
     * Extract a specific claim from JWT token.
     * 
     * @param token the JWT token
     * @param claimsResolver function to extract the claim
     * @param <T> type of the claim
     * @return the extracted claim
     * @throws InvalidTokenException if token is invalid
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token has expired: {}", e.getMessage());
            throw new TokenExpiredException("Token has expired");
        } catch (JwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            throw new InvalidTokenException("Invalid token: " + e.getMessage());
        }
    }

    /**
     * Extract all claims from JWT token.
     * 
     * @param token the JWT token
     * @return all claims
     * @throws JwtException if token is invalid or expired
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Check if JWT token is expired.
     * 
     * @param token the JWT token
     * @return true if token is expired, false otherwise
     */
    public Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (TokenExpiredException e) {
            return true;
        } catch (Exception e) {
            logger.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Generate JWT access token for user.
     * 
     * @param userDetails the user details
     * @return the generated JWT token
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");
        return createToken(claims, userDetails.getUsername(), jwtExpiration);
    }

    /**
     * Generate JWT refresh token for user.
     * 
     * @param userDetails the user details
     * @return the generated refresh token
     */
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return createToken(claims, userDetails.getUsername(), refreshExpiration);
    }

    /**
     * Generate JWT token with custom claims and expiration.
     * 
     * @param extraClaims additional claims to include
     * @param username the username (subject)
     * @param expirationTime expiration time in seconds
     * @return the generated JWT token
     */
    public String generateToken(Map<String, Object> extraClaims, String username, Long expirationTime) {
        return createToken(extraClaims, username, expirationTime);
    }

    /**
     * Create JWT token with specified claims, subject, and expiration.
     * 
     * @param claims the claims to include
     * @param subject the subject (username)
     * @param expirationTime expiration time in seconds
     * @return the created JWT token
     */
    private String createToken(Map<String, Object> claims, String subject, Long expirationTime) {
        Instant now = Instant.now();
        Instant expiration = now.plus(expirationTime, ChronoUnit.SECONDS);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Validate JWT token against user details.
     * 
     * @param token the JWT token
     * @param userDetails the user details
     * @return true if token is valid, false otherwise
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            logger.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if token is a refresh token.
     * 
     * @param token the JWT token
     * @return true if token is a refresh token, false otherwise
     */
    public Boolean isRefreshToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            String tokenType = (String) claims.get("type");
            return "refresh".equals(tokenType);
        } catch (Exception e) {
            logger.error("Error checking token type: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get remaining time until token expiration in seconds.
     * 
     * @param token the JWT token
     * @return remaining time in seconds, or 0 if expired/invalid
     */
    public Long getRemainingTime(String token) {
        try {
            Date expiration = extractExpiration(token);
            long remainingMs = expiration.getTime() - System.currentTimeMillis();
            return Math.max(0, remainingMs / 1000);
        } catch (Exception e) {
            logger.error("Error calculating remaining time: {}", e.getMessage());
            return 0L;
        }
    }

    /**
     * Extract token from Authorization header.
     * 
     * @param authHeader the Authorization header value
     * @return the extracted token, or null if invalid format
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Get configured JWT expiration time.
     * 
     * @return JWT expiration time in seconds
     */
    public Long getJwtExpiration() {
        return jwtExpiration;
    }

    /**
     * Get configured refresh token expiration time.
     * 
     * @return refresh token expiration time in seconds
     */
    public Long getRefreshExpiration() {
        return refreshExpiration;
    }
}