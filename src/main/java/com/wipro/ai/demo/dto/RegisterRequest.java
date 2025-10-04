package com.wipro.ai.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for user registration requests.
 * 
 * This DTO contains all the information required to register a new user account.
 * It includes comprehensive validation to ensure data integrity and security.
 * 
 * Features:
 * - Comprehensive input validation
 * - Swagger/OpenAPI documentation
 * - Security-focused design
 * - Clear validation error messages
 * 
 * @author Wipro AI Demo Team
 * @version 1.0.0
 * @since 2024-10-03
 */
@Schema(description = "User registration request containing all required user information")
public class RegisterRequest {

    /**
     * Unique username for the new account.
     */
    @Schema(
        description = "Unique username for the account", 
        example = "john_doe",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 3,
        maxLength = 50
    )
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    /**
     * Unique email address for the new account.
     */
    @Schema(
        description = "Valid email address", 
        example = "john@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    /**
     * Password for the new account.
     */
    @Schema(
        description = "Password for the account", 
        example = "securePassword123",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 6,
        maxLength = 100
    )
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    /**
     * User's first name.
     */
    @Schema(
        description = "User's first name", 
        example = "John",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 50
    )
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    /**
     * User's last name.
     */
    @Schema(
        description = "User's last name", 
        example = "Doe",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 50
    )
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    /**
     * Optional phone number for the user.
     */
    @Schema(
        description = "User's phone number (optional)", 
        example = "+1234567890",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        maxLength = 15
    )
    @Size(max = 15, message = "Phone number must not exceed 15 characters")
    private String phoneNumber;

    /**
     * Default constructor for JSON deserialization.
     */
    public RegisterRequest() {
    }

    /**
     * Constructor with required fields.
     * 
     * @param username the username
     * @param email the email address
     * @param password the password
     * @param firstName the first name
     * @param lastName the last name
     */
    public RegisterRequest(String username, String email, String password, String firstName, String lastName) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    /**
     * Constructor with all fields.
     * 
     * @param username the username
     * @param email the email address
     * @param password the password
     * @param firstName the first name
     * @param lastName the last name
     * @param phoneNumber the phone number
     */
    public RegisterRequest(String username, String email, String password, 
                          String firstName, String lastName, String phoneNumber) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
    }

    // Getters and Setters

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return "RegisterRequest{" +
               "username='" + username + '\'' +
               ", email='" + email + '\'' +
               ", firstName='" + firstName + '\'' +
               ", lastName='" + lastName + '\'' +
               ", phoneNumber='" + phoneNumber + '\'' +
               ", password='[PROTECTED]'" +
               '}';
    }
}