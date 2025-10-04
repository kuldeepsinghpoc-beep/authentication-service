package com.wipro.ai.demo.exception;

import com.wipro.ai.demo.dto.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Global exception handler for the authentication service.
 * 
 * This handler provides centralized exception handling across all controllers,
 * ensuring consistent error responses and proper HTTP status codes. It handles
 * various types of exceptions including validation errors, authentication failures,
 * JWT-related issues, and generic application errors.
 * 
 * Features:
 * - Consistent error response format
 * - Comprehensive exception coverage
 * - Detailed logging for troubleshooting
 * - Validation error aggregation
 * - Security-aware error messages
 * 
 * @author Wipro AI Demo Team
 * @version 1.0.0
 * @since 2024-10-03
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle validation errors from @Valid annotations.
     * 
     * @param ex the validation exception
     * @param request the HTTP request
     * @return error response with validation details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        logger.warn("Validation error occurred: {}", ex.getMessage());
        
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = new ErrorResponse(
            "Validation failed",
            HttpStatus.BAD_REQUEST.value(),
            request.getRequestURI(),
            validationErrors.values().stream().toList(),
            "VALIDATION_ERROR"
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle constraint violation exceptions.
     * 
     * @param ex the constraint violation exception
     * @param request the HTTP request
     * @return error response with constraint violation details
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {
        
        logger.warn("Constraint violation occurred: {}", ex.getMessage());
        
        Map<String, String> validationErrors = new HashMap<>();
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        
        for (ConstraintViolation<?> violation : violations) {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            validationErrors.put(fieldName, errorMessage);
        }

        ErrorResponse errorResponse = new ErrorResponse(
            "Constraint violation",
            HttpStatus.BAD_REQUEST.value(),
            request.getRequestURI(),
            validationErrors.values().stream().toList(),
            "CONSTRAINT_VIOLATION"
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle user already exists exceptions.
     * 
     * @param ex the user already exists exception
     * @param request the HTTP request
     * @return error response for duplicate user
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(
            UserAlreadyExistsException ex, HttpServletRequest request) {
        
        logger.warn("User already exists: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            ex.getMessage(),
            HttpStatus.CONFLICT.value(),
            request.getRequestURI(),
            null,
            "USER_ALREADY_EXISTS"
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handle invalid credentials exceptions.
     * 
     * @param ex the invalid credentials exception
     * @param request the HTTP request
     * @return error response for authentication failure
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(
            InvalidCredentialsException ex, HttpServletRequest request) {
        
        logger.warn("Invalid credentials: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            "Invalid username or password",
            HttpStatus.UNAUTHORIZED.value(),
            request.getRequestURI(),
            null,
            "INVALID_CREDENTIALS"
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handle invalid token exceptions.
     * 
     * @param ex the invalid token exception
     * @param request the HTTP request
     * @return error response for token validation failure
     */
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(
            InvalidTokenException ex, HttpServletRequest request) {
        
        logger.warn("Invalid token: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            "Invalid or expired token",
            HttpStatus.UNAUTHORIZED.value(),
            request.getRequestURI(),
            null,
            "INVALID_TOKEN"
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handle user not found exceptions.
     * 
     * @param ex the user not found exception
     * @param request the HTTP request
     * @return error response for missing user
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(
            UserNotFoundException ex, HttpServletRequest request) {
        
        logger.warn("User not found: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            ex.getMessage(),
            HttpStatus.NOT_FOUND.value(),
            request.getRequestURI(),
            null,
            "USER_NOT_FOUND"
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handle Spring Security authentication exceptions.
     * 
     * @param ex the authentication exception
     * @param request the HTTP request
     * @return error response for authentication failure
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {
        
        logger.warn("Authentication failed: {}", ex.getMessage());

        String message = "Authentication failed";
        String errorCode = "AUTHENTICATION_FAILED";

        if (ex instanceof BadCredentialsException) {
            message = "Invalid username or password";
            errorCode = "INVALID_CREDENTIALS";
        } else if (ex instanceof DisabledException) {
            message = "Account is disabled";
            errorCode = "ACCOUNT_DISABLED";
        } else if (ex instanceof InsufficientAuthenticationException) {
            message = "Authentication required";
            errorCode = "AUTHENTICATION_REQUIRED";
        }

        ErrorResponse errorResponse = new ErrorResponse(
            message,
            HttpStatus.UNAUTHORIZED.value(),
            request.getRequestURI(),
            null,
            errorCode
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handle access denied exceptions.
     * 
     * @param ex the access denied exception
     * @param request the HTTP request
     * @return error response for authorization failure
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        
        logger.warn("Access denied: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            "Access denied - insufficient privileges",
            HttpStatus.FORBIDDEN.value(),
            request.getRequestURI(),
            null,
            "ACCESS_DENIED"
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Handle JWT-related exceptions.
     * 
     * @param ex the JWT exception
     * @param request the HTTP request
     * @return error response for JWT processing failure
     */
    @ExceptionHandler({JwtException.class, ExpiredJwtException.class})
    public ResponseEntity<ErrorResponse> handleJwtException(
            JwtException ex, HttpServletRequest request) {
        
        logger.warn("JWT error: {}", ex.getMessage());

        String message = "Invalid token";
        String errorCode = "INVALID_TOKEN";

        if (ex instanceof ExpiredJwtException) {
            message = "Token has expired";
            errorCode = "TOKEN_EXPIRED";
        }

        ErrorResponse errorResponse = new ErrorResponse(
            message,
            HttpStatus.UNAUTHORIZED.value(),
            request.getRequestURI(),
            null,
            errorCode
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handle HTTP method not supported exceptions.
     * 
     * @param ex the method not supported exception
     * @param request the HTTP request
     * @return error response for unsupported HTTP method
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        
        logger.warn("Method not supported: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            "HTTP method '" + ex.getMethod() + "' is not supported for this endpoint",
            HttpStatus.METHOD_NOT_ALLOWED.value(),
            request.getRequestURI(),
            null,
            "METHOD_NOT_ALLOWED"
        );

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }

    /**
     * Handle missing request parameter exceptions.
     * 
     * @param ex the missing parameter exception
     * @param request the HTTP request
     * @return error response for missing parameter
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        
        logger.warn("Missing parameter: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            "Missing required parameter: " + ex.getParameterName(),
            HttpStatus.BAD_REQUEST.value(),
            request.getRequestURI(),
            null,
            "MISSING_PARAMETER"
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle method argument type mismatch exceptions.
     * 
     * @param ex the type mismatch exception
     * @param request the HTTP request
     * @return error response for parameter type mismatch
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        
        logger.warn("Type mismatch: {}", ex.getMessage());

        Class<?> requiredType = ex.getRequiredType();
        String typeName = (requiredType != null) ? requiredType.getSimpleName() : "unknown";
        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s", 
                ex.getValue(), ex.getName(), typeName);

        ErrorResponse errorResponse = new ErrorResponse(
            message,
            HttpStatus.BAD_REQUEST.value(),
            request.getRequestURI(),
            null,
            "TYPE_MISMATCH"
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle HTTP message not readable exceptions.
     * 
     * @param ex the message not readable exception
     * @param request the HTTP request
     * @return error response for malformed JSON
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        
        logger.warn("Message not readable: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            "Malformed JSON request",
            HttpStatus.BAD_REQUEST.value(),
            request.getRequestURI(),
            null,
            "MALFORMED_JSON"
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle no handler found exceptions (404 errors).
     * 
     * @param ex the no handler found exception
     * @param request the HTTP request
     * @return error response for endpoint not found
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpServletRequest request) {
        
        logger.warn("Endpoint not found: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            "Endpoint not found: " + ex.getRequestURL(),
            HttpStatus.NOT_FOUND.value(),
            request.getRequestURI(),
            null,
            "ENDPOINT_NOT_FOUND"
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handle all other unhandled exceptions.
     * 
     * @param ex the generic exception
     * @param request the HTTP request
     * @return error response for internal server error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        logger.error("Unhandled exception occurred", ex);

        ErrorResponse errorResponse = new ErrorResponse(
            "An unexpected error occurred. Please try again later.",
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            request.getRequestURI(),
            null,
            "INTERNAL_SERVER_ERROR"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}