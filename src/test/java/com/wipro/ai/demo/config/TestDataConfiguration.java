package com.wipro.ai.demo.config;

import com.wipro.ai.demo.dto.*;
import com.wipro.ai.demo.model.User;

import java.time.LocalDateTime;

/**
 * Test data configuration providing mock data and test fixtures.
 * 
 * This class contains factory methods for creating test objects with valid data
 * for use in unit and integration tests.
 * 
 * @author Wipro AI Demo Team
 * @version 1.0.0
 * @since 2024-10-03
 */
public class TestDataConfiguration {

    /**
     * Create a test user with default values.
     * 
     * @return a User object with test data
     */
    public static User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("hashedPassword");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPhoneNumber("+1234567890");
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());
        return user;
    }

    /**
     * Create a test user with custom username and email.
     * 
     * @param username the username
     * @param email the email
     * @return a User object with custom data
     */
    public static User createTestUser(String username, String email) {
        User user = createTestUser();
        user.setUsername(username);
        user.setEmail(email);
        return user;
    }

    /**
     * Create an inactive test user.
     * 
     * @return an inactive User object
     */
    public static User createInactiveTestUser() {
        User user = createTestUser();
        user.setUsername("inactiveuser");
        user.setEmail("inactive@example.com");
        user.setActive(false);
        return user;
    }

    /**
     * Create a valid register request with default values.
     * 
     * @return a RegisterRequest object with test data
     */
    public static RegisterRequest createValidRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        request.setFirstName("New");
        request.setLastName("User");
        request.setPhoneNumber("+1234567890");
        return request;
    }

    /**
     * Create a register request with custom data.
     * 
     * @param username the username
     * @param email the email
     * @param password the password
     * @return a RegisterRequest object with custom data
     */
    public static RegisterRequest createRegisterRequest(String username, String email, String password) {
        RegisterRequest request = createValidRegisterRequest();
        request.setUsername(username);
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    /**
     * Create an invalid register request (missing required fields).
     * 
     * @return an invalid RegisterRequest object
     */
    public static RegisterRequest createInvalidRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("ab"); // Too short
        request.setEmail("invalid-email"); // Invalid format
        request.setPassword("123"); // Too short
        return request;
    }

    /**
     * Create a valid auth request with default values.
     * 
     * @return an AuthRequest object with test data
     */
    public static AuthRequest createValidAuthRequest() {
        AuthRequest request = new AuthRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        return request;
    }

    /**
     * Create an auth request with custom credentials.
     * 
     * @param username the username
     * @param password the password
     * @return an AuthRequest object with custom data
     */
    public static AuthRequest createAuthRequest(String username, String password) {
        AuthRequest request = new AuthRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }

    /**
     * Create an invalid auth request (missing password).
     * 
     * @return an invalid AuthRequest object
     */
    public static AuthRequest createInvalidAuthRequest() {
        AuthRequest request = new AuthRequest();
        request.setUsername("testuser");
        // Missing password
        return request;
    }

    /**
     * Create a valid auth response with default values.
     * 
     * @return an AuthResponse object with test data
     */
    public static AuthResponse createValidAuthResponse() {
        AuthResponse response = new AuthResponse();
        response.setAccessToken("eyJhbGciOiJIUzUxMiJ9.test.access.token");
        response.setRefreshToken("eyJhbGciOiJIUzUxMiJ9.test.refresh.token");
        response.setTokenType("Bearer");
        response.setExpiresIn(3600L);
        response.setUsername("testuser");
        response.setEmail("test@example.com");
        response.setFullName("Test User");
        return response;
    }

    /**
     * Create a refresh token request with default values.
     * 
     * @return a RefreshTokenRequest object with test data
     */
    public static RefreshTokenRequest createValidRefreshTokenRequest() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("eyJhbGciOiJIUzUxMiJ9.test.refresh.token");
        return request;
    }

    /**
     * Create an invalid refresh token request.
     * 
     * @return an invalid RefreshTokenRequest object
     */
    public static RefreshTokenRequest createInvalidRefreshTokenRequest() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalid.refresh.token");
        return request;
    }

    /**
     * Create a user response from a user entity.
     * 
     * @param user the user entity
     * @return a UserResponse object
     */
    public static UserResponse createUserResponse(User user) {
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
     * Create a valid user response with default values.
     * 
     * @return a UserResponse object with test data
     */
    public static UserResponse createValidUserResponse() {
        return createUserResponse(createTestUser());
    }

    /**
     * Create test JWT tokens.
     * 
     * @return array with [accessToken, refreshToken]
     */
    public static String[] createTestTokens() {
        return new String[] {
                "eyJhbGciOiJIUzUxMiJ9.test.access.token",
                "eyJhbGciOiJIUzUxMiJ9.test.refresh.token"
        };
    }

    /**
     * Create an expired JWT token.
     * 
     * @return an expired token string
     */
    public static String createExpiredToken() {
        return "eyJhbGciOiJIUzUxMiJ9.expired.token";
    }

    /**
     * Create an invalid JWT token.
     * 
     * @return an invalid token string
     */
    public static String createInvalidToken() {
        return "invalid.jwt.token";
    }

    /**
     * Create test credentials for authentication.
     * 
     * @return array with [username, password, email]
     */
    public static String[] createTestCredentials() {
        return new String[] {
                "testuser",
                "password123",
                "test@example.com"
        };
    }

    /**
     * Create multiple test users for bulk operations.
     * 
     * @param count the number of users to create
     * @return array of User objects
     */
    public static User[] createMultipleTestUsers(int count) {
        User[] users = new User[count];
        for (int i = 0; i < count; i++) {
            users[i] = createTestUser("user" + i, "user" + i + "@example.com");
            users[i].setId((long) (i + 1));
        }
        return users;
    }

    /**
     * Create test user with validation errors.
     * 
     * @return a User object with invalid data
     */
    public static User createInvalidTestUser() {
        User user = new User();
        user.setUsername("ab"); // Too short
        user.setEmail("invalid-email"); // Invalid format
        user.setFirstName(""); // Empty
        user.setLastName(""); // Empty
        return user;
    }
}