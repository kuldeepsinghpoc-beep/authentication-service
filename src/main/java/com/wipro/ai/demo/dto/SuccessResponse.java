package com.wipro.ai.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Standard success response format for API endpoints.
 * 
 * This DTO provides a consistent success response structure across all API endpoints,
 * wrapping the actual data with metadata.
 * 
 * @param <T> the type of data being returned
 * @author Wipro AI Demo Team
 * @version 1.0.0
 * @since 2024-10-03
 */
@Schema(description = "Success response containing data and metadata")
public class SuccessResponse<T> {

    /**
     * The actual data being returned.
     */
    @Schema(description = "Response data")
    @JsonProperty("data")
    private T data;

    /**
     * Success message.
     */
    @Schema(description = "Success message", example = "Operation completed successfully")
    @JsonProperty("message")
    private String message;

    /**
     * HTTP status code.
     */
    @Schema(description = "HTTP status code", example = "200")
    @JsonProperty("status")
    private int status;

    /**
     * Timestamp when the response was created.
     */
    @Schema(description = "Response timestamp", example = "2024-01-15T10:30:00")
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    /**
     * Default constructor.
     */
    public SuccessResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructor with data only.
     * 
     * @param data the response data
     */
    public SuccessResponse(T data) {
        this();
        this.data = data;
        this.status = 200;
        this.message = "Success";
    }

    /**
     * Constructor with data and message.
     * 
     * @param data the response data
     * @param message the success message
     */
    public SuccessResponse(T data, String message) {
        this();
        this.data = data;
        this.message = message;
        this.status = 200;
    }

    /**
     * Constructor with all fields.
     * 
     * @param data the response data
     * @param message the success message
     * @param status the HTTP status code
     */
    public SuccessResponse(T data, String message, int status) {
        this();
        this.data = data;
        this.message = message;
        this.status = status;
    }

    // Getters and Setters

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

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

    @Override
    public String toString() {
        return "SuccessResponse{" +
               "data=" + data +
               ", message='" + message + '\'' +
               ", status=" + status +
               ", timestamp=" + timestamp +
               '}';
    }
}