package com.wipro.ai.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object for authentication responses.
 * 
 * This DTO contains the authentication result including JWT tokens and user information.
 * It's returned when a user successfully authenticates or refreshes their token.
 * 
 * Features:
 * - JWT access token for API authentication
 * - Refresh token for token renewal
 * - Token type specification (Bearer)
 * - Token expiration information
 * - User identification details
 * 
 * @author Wipro AI Demo Team
 * @version 1.0.0
 * @since 2024-10-03
 */
@Schema(description = "Authentication response containing JWT tokens and user information")
public class AuthResponse {

    /**
     * JWT access token for API authentication.
     */
    @Schema(description = "JWT access token for API authentication", example = "eyJhbGciOiJIUzUxMiJ9...")
    @JsonProperty("accessToken")
    private String accessToken;

    /**
     * JWT refresh token for token renewal.
     */
    @Schema(description = "JWT refresh token for renewing access tokens", example = "eyJhbGciOiJIUzUxMiJ9...")
    @JsonProperty("refreshToken")
    private String refreshToken;

    /**
     * Token type (typically "Bearer").
     */
    @Schema(description = "Token type", example = "Bearer")
    @JsonProperty("tokenType")
    private String tokenType = "Bearer";

    /**
     * Access token expiration time in seconds.
     */
    @Schema(description = "Access token expiration time in seconds", example = "86400")
    @JsonProperty("expiresIn")
    private Long expiresIn;

    /**
     * Username of the authenticated user.
     */
    @Schema(description = "Username of the authenticated user", example = "john_doe")
    @JsonProperty("username")
    private String username;

    /**
     * Email of the authenticated user.
     */
    @Schema(description = "Email of the authenticated user", example = "john@example.com")
    @JsonProperty("email")
    private String email;

    /**
     * Full name of the authenticated user.
     */
    @Schema(description = "Full name of the authenticated user", example = "John Doe")
    @JsonProperty("fullName")
    private String fullName;

    /**
     * Default constructor for JSON deserialization.
     */
    public AuthResponse() {
    }

    /**
     * Constructor with essential fields.
     * 
     * @param accessToken the JWT access token
     * @param refreshToken the JWT refresh token
     * @param expiresIn token expiration time in seconds
     */
    public AuthResponse(String accessToken, String refreshToken, Long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.tokenType = "Bearer";
    }

    /**
     * Constructor with all fields.
     * 
     * @param accessToken the JWT access token
     * @param refreshToken the JWT refresh token
     * @param expiresIn token expiration time in seconds
     * @param username the username
     * @param email the email
     * @param fullName the full name
     */
    public AuthResponse(String accessToken, String refreshToken, Long expiresIn, 
                       String username, String email, String fullName) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.tokenType = "Bearer";
    }

    // Getters and Setters

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public String toString() {
        return "AuthResponse{" +
               "tokenType='" + tokenType + '\'' +
               ", expiresIn=" + expiresIn +
               ", username='" + username + '\'' +
               ", email='" + email + '\'' +
               ", fullName='" + fullName + '\'' +
               ", accessToken='[PRESENT]'" +
               ", refreshToken='[PRESENT]'" +
               '}';
    }
}