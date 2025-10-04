package com.wipro.ai.demo.controller;

import com.wipro.ai.demo.dto.*;
import com.wipro.ai.demo.service.AuthService;
import com.wipro.ai.demo.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication-related operations.
 * 
 * This controller handles all authentication endpoints including user registration,
 * login, token refresh, validation, profile management, and logout.
 * 
 * @author Wipro AI Demo Team
 * @version 1.0.0
 * @since 2024-10-03
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication and user management endpoints")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Register a new user.
     * 
     * @param registerRequest the registration request data
     * @return success response with user details or error response
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Email already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SuccessResponse<UserResponse>> register(
            @Parameter(description = "User registration data") 
            @Valid @RequestBody RegisterRequest registerRequest) {
        
        logger.info("Registration attempt for email: {}", registerRequest.getEmail());
        
        try {
            UserResponse userResponse = authService.registerUser(registerRequest);
            
            logger.info("User registered successfully with ID: {}", userResponse.getId());
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new SuccessResponse<>(userResponse, "User registered successfully", 201));
                    
        } catch (Exception e) {
            logger.error("Registration failed for email: {}", registerRequest.getEmail(), e);
            throw e;
        }
    }

    /**
     * Authenticate user and generate JWT tokens.
     * 
     * @param authRequest the authentication request data
     * @return success response with JWT tokens or error response
     */
    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Authenticates user credentials and returns JWT tokens")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Authentication successful",
                    content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Account is inactive",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SuccessResponse<AuthResponse>> login(
            @Parameter(description = "User login credentials") 
            @Valid @RequestBody AuthRequest authRequest) {
        
        logger.info("Login attempt for user: {}", authRequest.getUsername());
        
        try {
            AuthResponse authResponse = authService.authenticateUser(authRequest);
            logger.info("User authenticated successfully: {}", authRequest.getUsername());
            
            return ResponseEntity.ok(
                    new SuccessResponse<>(authResponse, "Authentication successful"));
                    
        } catch (Exception e) {
            logger.error("Authentication failed for user: {}", authRequest.getUsername(), e);
            throw e;
        }
    }

    /**
     * Refresh JWT access token using refresh token.
     * 
     * @param refreshRequest the refresh token request
     * @return success response with new tokens or error response
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Generates new access token using refresh token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
                    content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SuccessResponse<AuthResponse>> refresh(
            @Parameter(description = "Refresh token data") 
            @Valid @RequestBody RefreshTokenRequest refreshRequest) {
        
        logger.info("Token refresh attempt");
        
        try {
            AuthResponse authResponse = authService.refreshToken(refreshRequest);
            logger.info("Token refreshed successfully");
            
            return ResponseEntity.ok(
                    new SuccessResponse<>(authResponse, "Token refreshed successfully"));
                    
        } catch (Exception e) {
            logger.error("Token refresh failed", e);
            throw e;
        }
    }

    /**
     * Validate JWT token.
     * 
     * @param request the HTTP request containing Authorization header
     * @return success response with validation result or error response
     */
    @GetMapping("/validate")
    @Operation(summary = "Validate JWT token", description = "Validates the provided JWT token")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token is valid",
                    content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid or expired token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SuccessResponse<Boolean>> validateToken(HttpServletRequest request) {
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            try {
                boolean isValid = authService.validateToken(token);
                String username = jwtUtil.extractUsername(token);
                
                logger.info("Token validation result for user {}: {}", username, isValid);
                
                return ResponseEntity.ok(
                        new SuccessResponse<>(isValid, "Token validation completed"));
                        
            } catch (Exception e) {
                logger.error("Token validation failed", e);
                return ResponseEntity.ok(
                        new SuccessResponse<>(false, "Token is invalid"));
            }
        }
        
        return ResponseEntity.ok(
                new SuccessResponse<>(false, "No token provided"));
    }

    /**
     * Get current user profile information.
     * 
     * @param authentication the authentication object from security context
     * @return success response with user profile or error response
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Retrieves the current authenticated user's profile")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SuccessResponse<UserResponse>> getCurrentUser(Authentication authentication) {
        
        String email = authentication.getName();
        logger.info("Profile request for user: {}", email);
        
        try {
            UserResponse userResponse = authService.getCurrentUser(email);
            
            logger.info("Profile retrieved successfully for user: {}", email);
            return ResponseEntity.ok(
                    new SuccessResponse<>(userResponse, "Profile retrieved successfully"));
                    
        } catch (Exception e) {
            logger.error("Failed to retrieve profile for user: {}", email, e);
            throw e;
        }
    }

    /**
     * Logout user and invalidate tokens.
     * 
     * @param request the HTTP request containing Authorization header
     * @return success response confirming logout
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Logs out the user and invalidates the JWT token")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout successful",
                    content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
        @ApiResponse(responseCode = "400", description = "No token provided",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SuccessResponse<String>> logout(HttpServletRequest request) {
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            try {
                String username = jwtUtil.extractUsername(token);
                authService.logout(token);
                
                // Clear security context
                SecurityContextHolder.clearContext();
                
                logger.info("User logged out successfully: {}", username);
                return ResponseEntity.ok(
                        new SuccessResponse<>("Logout successful", "User logged out successfully"));
                        
            } catch (Exception e) {
                logger.error("Logout failed", e);
                throw e;
            }
        }
        
        return ResponseEntity.badRequest()
                .body(new SuccessResponse<>("No token provided", "Authorization header missing", 400));
    }

    /**
     * Health check endpoint.
     * 
     * @return success response indicating service is running
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Checks if the authentication service is running")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Service is healthy",
                    content = @Content(schema = @Schema(implementation = SuccessResponse.class)))
    })
    public ResponseEntity<SuccessResponse<String>> health() {
        return ResponseEntity.ok(
                new SuccessResponse<>("Service is healthy", "Authentication service is running"));
    }
}