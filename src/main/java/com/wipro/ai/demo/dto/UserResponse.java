package com.wipro.ai.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for user information responses.
 * 
 * This DTO contains user profile information that can be safely returned to clients.
 * It excludes sensitive information like passwords and includes only necessary user data.
 * 
 * Features:
 * - Secure user data representation (no sensitive fields)
 * - Comprehensive user profile information
 * - Swagger/OpenAPI documentation
 * - JSON serialization optimization
 * 
 * @author Wipro AI Demo Team
 * @version 1.0.0
 * @since 2024-10-03
 */
@Schema(description = "User profile information response")
public class UserResponse {

    /**
     * Unique identifier for the user.
     */
    @Schema(description = "Unique user identifier", example = "1")
    @JsonProperty("id")
    private Long id;

    /**
     * User's username.
     */
    @Schema(description = "Username of the user", example = "john_doe")
    @JsonProperty("username")
    private String username;

    /**
     * User's email address.
     */
    @Schema(description = "Email address of the user", example = "john@example.com")
    @JsonProperty("email")
    private String email;

    /**
     * User's first name.
     */
    @Schema(description = "First name of the user", example = "John")
    @JsonProperty("firstName")
    private String firstName;

    /**
     * User's last name.
     */
    @Schema(description = "Last name of the user", example = "Doe")
    @JsonProperty("lastName")
    private String lastName;

    /**
     * User's full name (computed from first and last name).
     */
    @Schema(description = "Full name of the user", example = "John Doe")
    @JsonProperty("fullName")
    private String fullName;

    /**
     * User's phone number (optional).
     */
    @Schema(description = "Phone number of the user", example = "+1234567890")
    @JsonProperty("phoneNumber")
    private String phoneNumber;

    /**
     * Whether the user account is active.
     */
    @Schema(description = "Whether the user account is active", example = "true")
    @JsonProperty("active")
    private Boolean active;

    /**
     * When the user account was created.
     */
    @Schema(description = "Account creation timestamp", example = "2024-01-15T10:30:00")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    /**
     * When the user account was last updated.
     */
    @Schema(description = "Last account update timestamp", example = "2024-01-20T14:45:00")
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    /**
     * When the user last logged in.
     */
    @Schema(description = "Last login timestamp", example = "2024-01-25T09:15:00")
    @JsonProperty("lastLogin")
    private LocalDateTime lastLogin;

    /**
     * Default constructor for JSON deserialization.
     */
    public UserResponse() {
    }

    /**
     * Constructor with essential fields.
     * 
     * @param id the user ID
     * @param username the username
     * @param email the email address
     * @param firstName the first name
     * @param lastName the last name
     */
    public UserResponse(Long id, String username, String email, String firstName, String lastName) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = firstName + " " + lastName;
    }

    /**
     * Constructor with all fields.
     * 
     * @param id the user ID
     * @param username the username
     * @param email the email address
     * @param firstName the first name
     * @param lastName the last name
     * @param phoneNumber the phone number
     * @param active whether the account is active
     * @param createdAt when the account was created
     * @param updatedAt when the account was last updated
     * @param lastLogin when the user last logged in
     */
    public UserResponse(Long id, String username, String email, String firstName, String lastName,
                       String phoneNumber, Boolean active, LocalDateTime createdAt, 
                       LocalDateTime updatedAt, LocalDateTime lastLogin) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = firstName + " " + lastName;
        this.phoneNumber = phoneNumber;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastLogin = lastLogin;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        updateFullName();
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        updateFullName();
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    /**
     * Helper method to update the full name when first or last name changes.
     */
    private void updateFullName() {
        if (firstName != null && lastName != null) {
            this.fullName = firstName + " " + lastName;
        }
    }

    @Override
    public String toString() {
        return "UserResponse{" +
               "id=" + id +
               ", username='" + username + '\'' +
               ", email='" + email + '\'' +
               ", fullName='" + fullName + '\'' +
               ", phoneNumber='" + phoneNumber + '\'' +
               ", active=" + active +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               ", lastLogin=" + lastLogin +
               '}';
    }
}