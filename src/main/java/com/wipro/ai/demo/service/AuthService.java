package com.wipro.ai.demo.service;

import com.wipro.ai.demo.dto.*;
import com.wipro.ai.demo.exception.InvalidCredentialsException;
import com.wipro.ai.demo.exception.InvalidTokenException;
import com.wipro.ai.demo.exception.UserAlreadyExistsException;
import com.wipro.ai.demo.model.User;
import com.wipro.ai.demo.repository.UserRepository;
import com.wipro.ai.demo.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Core authentication service providing comprehensive user management and JWT-based authentication.
 * 
 * This service handles all authentication-related business logic including user registration,
 * authentication, token generation and validation, and session management. It integrates
 * with Spring Security to provide enterprise-grade security features.
 * 
 * Features:
 * - User registration with duplicate validation
 * - JWT token-based authentication and authorization
 * - Token refresh and blacklisting capabilities
 * - Password encryption with BCrypt
 * - Comprehensive error handling and validation
 * - Transaction management for data consistency
 * 
 * @author Wipro AI Demo Team
 * @version 1.0.0
 * @since 2024-10-03
 */
@Service
@Transactional
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * Constructor with dependency injection.
     * 
     * @param userRepository the user repository
     * @param passwordEncoder the password encoder
     * @param jwtUtil the JWT utility
     * @param authenticationManager the authentication manager
     * @param userDetailsService the user details service
     * @param tokenBlacklistService the token blacklist service
     */
    public AuthService(UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      JwtUtil jwtUtil,
                      AuthenticationManager authenticationManager,
                      UserDetailsService userDetailsService,
                      TokenBlacklistService tokenBlacklistService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    /**
     * Register a new user in the system.
     * 
     * Validates that the username and email are unique, encrypts the password
     * using BCrypt, and stores the user in the database.
     * 
     * @param request the registration request containing user details
     * @return UserResponse containing the created user information (no password)
     * @throws UserAlreadyExistsException if username or email already exists
     */
    public UserResponse registerUser(RegisterRequest request) {
        logger.info("Attempting to register user: {}", request.getUsername());

        // Validate registration request
        validateRegistrationRequest(request);

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            logger.warn("Registration failed - username already exists: {}", request.getUsername());
            throw new UserAlreadyExistsException("Username '" + request.getUsername() + "' is already taken");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("Registration failed - email already exists: {}", request.getEmail());
            throw new UserAlreadyExistsException("Email '" + request.getEmail() + "' is already registered");
        }

        // Create new user entity
        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            user.setPhoneNumber(request.getPhoneNumber().trim());
        }
        
        user.setActive(true);

        // Save user to database
        User savedUser = userRepository.save(user);
        logger.info("User registered successfully: {} (ID: {})", savedUser.getUsername(), savedUser.getId());

        return convertToUserResponse(savedUser);
    }

    /**
     * Authenticate a user and generate JWT tokens.
     * 
     * @param request the authentication request containing credentials
     * @return AuthResponse containing JWT tokens and user information
     * @throws InvalidCredentialsException if credentials are invalid
     */
    public AuthResponse authenticateUser(AuthRequest request) {
        String usernameOrEmail = request.getUsername().trim();
        logger.info("Attempting to authenticate user: {}", usernameOrEmail);

        try {
            // Authenticate using Spring Security
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(usernameOrEmail, request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            
            // Get user entity for additional information
            User user = findUserByUsernameOrEmail(usernameOrEmail);
            
            // Update last login timestamp
            updateLastLogin(user.getUsername());

            // Generate JWT tokens
            String accessToken = jwtUtil.generateToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            logger.info("User authenticated successfully: {}", user.getUsername());

            return new AuthResponse(
                accessToken,
                refreshToken,
                jwtUtil.getJwtExpiration(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName()
            );

        } catch (AuthenticationException e) {
            logger.warn("Authentication failed for user: {} - {}", usernameOrEmail, e.getMessage());
            throw new InvalidCredentialsException("Invalid username/email or password");
        }
    }

    /**
     * Refresh JWT tokens using a valid refresh token.
     * 
     * @param request the refresh token request
     * @return AuthResponse containing new JWT tokens
     * @throws InvalidTokenException if refresh token is invalid
     */
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        logger.debug("Attempting to refresh token");

        try {
            // Validate refresh token
            if (!jwtUtil.isRefreshToken(refreshToken)) {
                throw new InvalidTokenException("Invalid refresh token type");
            }

            // Extract username from refresh token
            String username = jwtUtil.extractUsername(refreshToken);
            
            // Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            // Validate refresh token
            if (!jwtUtil.validateToken(refreshToken, userDetails)) {
                throw new InvalidTokenException("Invalid or expired refresh token");
            }

            // Check if refresh token is blacklisted
            if (tokenBlacklistService.isTokenBlacklisted(refreshToken)) {
                throw new InvalidTokenException("Refresh token has been invalidated");
            }

            // Generate new tokens
            String newAccessToken = jwtUtil.generateToken(userDetails);
            String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);

            // Blacklist the old refresh token
            Date expiration = jwtUtil.extractExpiration(refreshToken);
            tokenBlacklistService.blacklistToken(refreshToken, 
                LocalDateTime.ofInstant(expiration.toInstant(), ZoneId.systemDefault()));

            // Get user information
            User user = findUserByUsernameOrEmail(username);

            logger.info("Token refreshed successfully for user: {}", username);

            return new AuthResponse(
                newAccessToken,
                newRefreshToken,
                jwtUtil.getJwtExpiration(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName()
            );

        } catch (Exception e) {
            logger.error("Token refresh failed: {}", e.getMessage());
            throw new InvalidTokenException("Failed to refresh token: " + e.getMessage());
        }
    }

    /**
     * Logout a user by blacklisting their JWT token.
     * 
     * @param token the JWT token to invalidate
     */
    public void logout(String token) {
        try {
            String username = jwtUtil.extractUsername(token);
            logger.info("Logging out user: {}", username);

            // Get token expiration for blacklist cleanup
            Date expiration = jwtUtil.extractExpiration(token);
            LocalDateTime expirationTime = LocalDateTime.ofInstant(expiration.toInstant(), ZoneId.systemDefault());

            // Add token to blacklist
            tokenBlacklistService.blacklistToken(token, expirationTime);

            logger.info("User logged out successfully: {}", username);

        } catch (Exception e) {
            logger.error("Logout failed: {}", e.getMessage());
            throw new InvalidTokenException("Failed to logout: " + e.getMessage());
        }
    }

    /**
     * Validate a JWT token.
     * 
     * @param token the JWT token to validate
     * @return true if token is valid, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean validateToken(String token) {
        try {
            // Check if token is blacklisted
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                logger.debug("Token validation failed - token is blacklisted");
                return false;
            }

            // Extract username and validate
            String username = jwtUtil.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            boolean isValid = jwtUtil.validateToken(token, userDetails);
            logger.debug("Token validation result for user {}: {}", username, isValid);
            
            return isValid;

        } catch (Exception e) {
            logger.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get current user information from JWT token.
     * 
     * @param username the username from the JWT token
     * @return UserResponse containing user information
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String username) {
        logger.debug("Getting current user information for: {}", username);

        User user = findUserByUsernameOrEmail(username);
        return convertToUserResponse(user);
    }

    /**
     * Update last login timestamp for a user.
     * 
     * @param username the username
     */
    private void updateLastLogin(String username) {
        try {
            userRepository.updateLastLogin(username, LocalDateTime.now());
            logger.debug("Updated last login for user: {}", username);
        } catch (Exception e) {
            logger.warn("Failed to update last login for user {}: {}", username, e.getMessage());
        }
    }

    /**
     * Find user by username or email.
     * 
     * @param usernameOrEmail the username or email
     * @return the User entity
     * @throws InvalidCredentialsException if user not found
     */
    private User findUserByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmailAndActiveTrue(usernameOrEmail)
                .orElseThrow(() -> new InvalidCredentialsException("User not found: " + usernameOrEmail));
    }

    /**
     * Convert User entity to UserResponse DTO.
     * 
     * @param user the User entity
     * @return UserResponse DTO
     */
    private UserResponse convertToUserResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getPhoneNumber(),
            user.getActive(),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            user.getLastLogin()
        );
    }

    /**
     * Validate registration request data.
     * 
     * @param request the registration request
     * @throws IllegalArgumentException if validation fails
     */
    private void validateRegistrationRequest(RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
    }

    /**
     * Get user statistics.
     * 
     * @return user statistics summary
     */
    @Transactional(readOnly = true)
    public String getUserStatistics() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByActiveTrue();
        return String.format("Total users: %d, Active users: %d", totalUsers, activeUsers);
    }
}