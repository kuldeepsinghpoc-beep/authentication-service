package com.wipro.ai.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for token refresh requests.
 * 
 * This DTO contains the refresh token used to obtain new access tokens
 * without requiring the user to re-authenticate.
 * 
 * @author Wipro AI Demo Team
 * @version 1.0.0
 * @since 2024-10-03
 */
@Schema(description = "Token refresh request containing the refresh token")
public class RefreshTokenRequest {

    /**
     * The refresh token used to obtain new access tokens.
     */
    @Schema(
        description = "Refresh token for obtaining new access tokens", 
        example = "eyJhbGciOiJIUzUxMiJ9...",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    /**
     * Default constructor for JSON deserialization.
     */
    public RefreshTokenRequest() {
    }

    /**
     * Constructor with refresh token.
     * 
     * @param refreshToken the refresh token
     */
    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // Getters and Setters

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public String toString() {
        return "RefreshTokenRequest{" +
               "refreshToken='[PRESENT]'" +
               '}';
    }
}