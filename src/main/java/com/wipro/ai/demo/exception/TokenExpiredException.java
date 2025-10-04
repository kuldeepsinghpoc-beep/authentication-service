package com.wipro.ai.demo.exception;

/**
 * Exception thrown when a JWT token has expired.
 * 
 * @author Wipro AI Demo Team
 * @version 1.0.0
 * @since 2024-10-03
 */
public class TokenExpiredException extends RuntimeException {

    /**
     * Constructs a new TokenExpiredException with the specified detail message.
     *
     * @param message the detail message
     */
    public TokenExpiredException(String message) {
        super(message);
    }

    /**
     * Constructs a new TokenExpiredException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public TokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}