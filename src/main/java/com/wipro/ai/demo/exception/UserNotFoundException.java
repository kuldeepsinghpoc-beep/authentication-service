package com.wipro.ai.demo.exception;

/**
 * Exception thrown when a requested user cannot be found.
 * 
 * This exception is typically thrown during operations that require
 * an existing user, such as profile retrieval, updates, or role assignments.
 * 
 * @author Wipro AI Demo Team
 * @version 1.0.0
 * @since 2024-10-03
 */
public class UserNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new UserNotFoundException with a default message.
     */
    public UserNotFoundException() {
        super("User not found");
    }

    /**
     * Constructs a new UserNotFoundException with the specified detail message.
     * 
     * @param message the detail message explaining the cause of the exception
     */
    public UserNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new UserNotFoundException with the specified detail message and cause.
     * 
     * @param message the detail message explaining the cause of the exception
     * @param cause the cause of the exception
     */
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new UserNotFoundException for a specific user identifier.
     * 
     * @param identifier the user identifier (username, email, or ID)
     * @return a new UserNotFoundException with a descriptive message
     */
    public static UserNotFoundException forIdentifier(String identifier) {
        return new UserNotFoundException("User not found with identifier: " + identifier);
    }

    /**
     * Constructs a new UserNotFoundException for a specific user ID.
     * 
     * @param userId the user ID
     * @return a new UserNotFoundException with a descriptive message
     */
    public static UserNotFoundException forId(Long userId) {
        return new UserNotFoundException("User not found with ID: " + userId);
    }

    /**
     * Constructs a new UserNotFoundException for a specific email.
     * 
     * @param email the user email
     * @return a new UserNotFoundException with a descriptive message
     */
    public static UserNotFoundException forEmail(String email) {
        return new UserNotFoundException("User not found with email: " + email);
    }

    /**
     * Constructs a new UserNotFoundException for a specific username.
     * 
     * @param username the username
     * @return a new UserNotFoundException with a descriptive message
     */
    public static UserNotFoundException forUsername(String username) {
        return new UserNotFoundException("User not found with username: " + username);
    }
}