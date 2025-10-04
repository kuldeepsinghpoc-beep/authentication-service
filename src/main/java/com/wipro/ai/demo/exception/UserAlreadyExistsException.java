package com.wipro.ai.demo.exception;

/**
 * Exception thrown when a user attempts to register with a username or email that already exists.
 * 
 * @author Wipro AI Demo Team
 * @version 1.0.0
 * @since 2024-10-03
 */
public class UserAlreadyExistsException extends RuntimeException {

    /**
     * Constructs a new UserAlreadyExistsException with the specified detail message.
     *
     * @param message the detail message
     */
    public UserAlreadyExistsException(String message) {
        super(message);
    }

    /**
     * Constructs a new UserAlreadyExistsException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}