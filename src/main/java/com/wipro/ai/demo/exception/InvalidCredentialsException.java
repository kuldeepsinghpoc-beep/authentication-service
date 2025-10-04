package com.wipro.ai.demo.exception;

/**
 * Exception thrown when authentication fails due to invalid credentials.
 * 
 * @author Wipro AI Demo Team
 * @version 1.0.0
 * @since 2024-10-03
 */
public class InvalidCredentialsException extends RuntimeException {

    /**
     * Constructs a new InvalidCredentialsException with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidCredentialsException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidCredentialsException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}