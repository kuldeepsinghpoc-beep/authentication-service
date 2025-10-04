package com.wipro.ai.demo.exception;

/**
 * Exception thrown when a JWT token is invalid or malformed.
 * 
 * @author Wipro AI Demo Team
 * @version 1.0.0
 * @since 2024-10-03
 */
public class InvalidTokenException extends RuntimeException {

    /**
     * Constructs a new InvalidTokenException with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidTokenException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidTokenException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}