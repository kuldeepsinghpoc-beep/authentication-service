package com.wipro.ai.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for user authentication requests.
 * 
 * This DTO represents the login credentials submitted by users for authentication.
 * It supports flexible login where users can provide either their username or
 * email address along with their password.
 * 
 * Features:
 * - Comprehensive input validation
 * - Swagger/OpenAPI documentation
 * - Support for username or email login
 * - Security-focused design
 * 
 * @author Wipro AI Demo Team
 * @version 1.0.0
 * @since 2024-10-03
 */
@Schema(description = "Authentication request containing login credentials")
public class AuthRequest {

    /**
     * Username or email address for authentication.
     * Users can login with either their username or email.
     */
    @Schema(
        description = "Username or email address for login", 
        example = "john_doe",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Username or email is required")
    @Size(min = 3, max = 100, message = "Username or email must be between 3 and 100 characters")
    private String username;

    /**
     * Password for authentication.
     */
    @Schema(
        description = "User password", 
        example = "securePassword123",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    /**
     * Default constructor for JSON deserialization.
     */
    public AuthRequest() {
    }

    /**
     * Constructor with all fields.
     * 
     * @param username the username or email
     * @param password the password
     */
    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters and Setters

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "AuthRequest{" +
               "username='" + username + '\'' +
               ", password='[PROTECTED]'" +
               '}';
    }
}