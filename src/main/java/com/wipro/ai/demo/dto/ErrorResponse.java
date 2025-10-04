package com.wipro.ai.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard error response format for API endpoints.
 * 
 * This DTO provides a consistent error response structure across all API endpoints,
 * including validation errors, authentication failures, and business logic errors.
 * 
 * @author Wipro AI Demo Team
 * @version 1.0.0
 * @since 2024-10-03
 */
@Schema(description = "Error response containing error details and validation information")
public class ErrorResponse {

    /**
     * Error message describing what went wrong.
     */
    @Schema(description = "Error message", example = "Username is already taken")
    @JsonProperty("message")
    private String message;

    /**
     * HTTP status code.
     */
    @Schema(description = "HTTP status code", example = "409")
    @JsonProperty("status")
    private int status;

    /**
     * Timestamp when the error occurred.
     */
    @Schema(description = "Error timestamp", example = "2024-01-15T10:30:00")
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    /**
     * Request path where the error occurred.
     */
    @Schema(description = "Request path", example = "/api/auth/register")
    @JsonProperty("path")
    private String path;

    /**
     * List of validation errors (if applicable).
     */
    @Schema(description = "Validation error details")
    @JsonProperty("validationErrors")
    private List<String> validationErrors;

    /**
     * Error code for programmatic handling.
     */
    @Schema(description = "Error code", example = "USER_ALREADY_EXISTS")
    @JsonProperty("errorCode")
    private String errorCode;

    /**
     * Default constructor.
     */
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructor with essential fields.
     * 
     * @param message the error message
     * @param status the HTTP status code
     * @param path the request path
     */
    public ErrorResponse(String message, int status, String path) {
        this();
        this.message = message;
        this.status = status;
        this.path = path;
    }

    /**
     * Constructor with all fields.
     * 
     * @param message the error message
     * @param status the HTTP status code
     * @param path the request path
     * @param validationErrors list of validation errors
     * @param errorCode the error code
     */
    public ErrorResponse(String message, int status, String path, List<String> validationErrors, String errorCode) {
        this();
        this.message = message;
        this.status = status;
        this.path = path;
        this.validationErrors = validationErrors;
        this.errorCode = errorCode;
    }

    // Getters and Setters

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(List<String> validationErrors) {
        this.validationErrors = validationErrors;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
               "message='" + message + '\'' +
               ", status=" + status +
               ", timestamp=" + timestamp +
               ", path='" + path + '\'' +
               ", errorCode='" + errorCode + '\'' +
               ", validationErrors=" + validationErrors +
               '}';
    }
}