package com.wipro.ai.demo.controller;

import com.wipro.ai.demo.dto.AuthRequest;
import com.wipro.ai.demo.dto.AuthResponse;
import com.wipro.ai.demo.dto.RefreshTokenRequest;
import com.wipro.ai.demo.dto.RegisterRequest;
import com.wipro.ai.demo.model.User;
import com.wipro.ai.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive integration tests for AuthController.
 * 
 * These tests verify the complete authentication flow including:
 * - User registration with validation
 * - User authentication and token generation
 * - Token refresh functionality
 * - Protected endpoint access with JWT
 * - Error handling and edge cases
 * 
 * @author Wipro AI Demo Team
 * @version 1.0.0
 * @since 2024-10-03
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebMvc
@Transactional
@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String baseUrl;
    private RegisterRequest validRegisterRequest;
    private AuthRequest validAuthRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/auth";

        // Clean database
        userRepository.deleteAll();

        // Create valid register request
        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setUsername("testuser");
        validRegisterRequest.setEmail("test@example.com");
        validRegisterRequest.setPassword("password123");
        validRegisterRequest.setFirstName("Test");
        validRegisterRequest.setLastName("User");

        // Create valid auth request
        validAuthRequest = new AuthRequest();
        validAuthRequest.setUsername("testuser");
        validAuthRequest.setPassword("password123");

        // Create and save test user
        testUser = new User();
        testUser.setUsername("existinguser");
        testUser.setEmail("existing@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setFirstName("Existing");
        testUser.setLastName("User");
        testUser.setActive(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        testUser = userRepository.save(testUser);
    }

    @Nested
    @DisplayName("User Registration Tests")
    class UserRegistrationTests {

        @Test
        @DisplayName("Should register user successfully with valid data")
        void registerUser_WithValidData_ShouldReturn201() {
            // Act
            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/register",
                    validRegisterRequest,
                    String.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).contains("testuser");
            assertThat(response.getBody()).contains("test@example.com");

            // Verify user was saved to database
            assertThat(userRepository.findByUsername("testuser")).isPresent();
        }

        @Test
        @DisplayName("Should return 409 when username already exists")
        void registerUser_WithExistingUsername_ShouldReturn409() {
            // Arrange
            validRegisterRequest.setUsername("existinguser");
            validRegisterRequest.setEmail("newemail@example.com");

            // Act
            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/register",
                    validRegisterRequest,
                    String.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).contains("Username");
            assertThat(response.getBody()).contains("already taken");
        }

        @Test
        @DisplayName("Should return 409 when email already exists")
        void registerUser_WithExistingEmail_ShouldReturn409() {
            // Arrange
            validRegisterRequest.setUsername("newusername");
            validRegisterRequest.setEmail("existing@example.com");

            // Act
            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/register",
                    validRegisterRequest,
                    String.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).contains("Email");
            assertThat(response.getBody()).contains("already registered");
        }

        @Test
        @DisplayName("Should return 400 with validation errors")
        void registerUser_WithInvalidData_ShouldReturn400() {
            // Arrange
            RegisterRequest invalidRequest = new RegisterRequest();
            invalidRequest.setUsername("ab"); // Too short
            invalidRequest.setEmail("invalid-email"); // Invalid format
            invalidRequest.setPassword("123"); // Too short

            // Act
            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/register",
                    invalidRequest,
                    String.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).contains("Validation failed");
        }
    }

    @Nested
    @DisplayName("User Authentication Tests")
    class UserAuthenticationTests {

        @Test
        @DisplayName("Should authenticate user successfully with valid credentials")
        void authenticateUser_WithValidCredentials_ShouldReturn200() {
            // Arrange
            AuthRequest authRequest = new AuthRequest();
            authRequest.setUsername("existinguser");
            authRequest.setPassword("password123");

            // Act
            ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                    baseUrl + "/login",
                    authRequest,
                    AuthResponse.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            
            AuthResponse authResponse = response.getBody();
            assertThat(authResponse).isNotNull();
            assertThat(authResponse.getAccessToken()).isNotNull();
            assertThat(authResponse.getRefreshToken()).isNotNull();
            assertThat(authResponse.getTokenType()).isEqualTo("Bearer");
            assertThat(authResponse.getUsername()).isEqualTo("existinguser");
        }

        @Test
        @DisplayName("Should return 401 with invalid credentials")
        void authenticateUser_WithInvalidCredentials_ShouldReturn401() {
            // Arrange
            AuthRequest authRequest = new AuthRequest();
            authRequest.setUsername("existinguser");
            authRequest.setPassword("wrongpassword");

            // Act
            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/login",
                    authRequest,
                    String.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody()).contains("Invalid credentials");
        }

        @Test
        @DisplayName("Should return 401 with non-existent user")
        void authenticateUser_WithNonExistentUser_ShouldReturn401() {
            // Arrange
            AuthRequest authRequest = new AuthRequest();
            authRequest.setUsername("nonexistent");
            authRequest.setPassword("password123");

            // Act
            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/login",
                    authRequest,
                    String.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should authenticate with email address")
        void authenticateUser_WithEmail_ShouldReturn200() {
            // Arrange
            AuthRequest authRequest = new AuthRequest();
            authRequest.setUsername("existing@example.com"); // Using email as username
            authRequest.setPassword("password123");

            // Act
            ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                    baseUrl + "/login",
                    authRequest,
                    AuthResponse.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getAccessToken()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Token Refresh Tests")
    class TokenRefreshTests {

        @Test
        @DisplayName("Should refresh token successfully with valid refresh token")
        void refreshToken_WithValidToken_ShouldReturn200() {
            // Arrange - First authenticate to get tokens
            AuthRequest authRequest = new AuthRequest();
            authRequest.setUsername("existinguser");
            authRequest.setPassword("password123");

            ResponseEntity<AuthResponse> loginResponse = restTemplate.postForEntity(
                    baseUrl + "/login",
                    authRequest,
                    AuthResponse.class
            );

            String refreshToken = loginResponse.getBody().getRefreshToken();

            RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
            refreshRequest.setRefreshToken(refreshToken);

            // Act
            ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                    baseUrl + "/refresh",
                    refreshRequest,
                    AuthResponse.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getAccessToken()).isNotNull();
            assertThat(response.getBody().getRefreshToken()).isNotNull();
            assertThat(response.getBody().getAccessToken()).isNotEqualTo(loginResponse.getBody().getAccessToken());
        }

        @Test
        @DisplayName("Should return 401 with invalid refresh token")
        void refreshToken_WithInvalidToken_ShouldReturn401() {
            // Arrange
            RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
            refreshRequest.setRefreshToken("invalid-refresh-token");

            // Act
            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/refresh",
                    refreshRequest,
                    String.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody()).contains("Invalid");
        }
    }

    @Nested
    @DisplayName("Protected Endpoint Tests")
    class ProtectedEndpointTests {

        @Test
        @DisplayName("Should access protected endpoint with valid token")
        void getCurrentUser_WithValidToken_ShouldReturn200() {
            // Arrange - Get access token
            String accessToken = authenticateAndGetToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Act
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/me",
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).contains("existinguser");
            assertThat(response.getBody()).contains("existing@example.com");
        }

        @Test
        @DisplayName("Should return 401 without token")
        void getCurrentUser_WithoutToken_ShouldReturn401() {
            // Act
            ResponseEntity<String> response = restTemplate.getForEntity(
                    baseUrl + "/me",
                    String.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should return 401 with invalid token")
        void getCurrentUser_WithInvalidToken_ShouldReturn401() {
            // Arrange
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth("invalid-token");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Act
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/me",
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should validate token successfully")
        void validateToken_WithValidToken_ShouldReturn200() {
            // Arrange
            String accessToken = authenticateAndGetToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Act
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/validate",
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).contains("true");
        }

        @Test
        @DisplayName("Should logout user successfully")
        void logout_WithValidToken_ShouldReturn200() {
            // Arrange
            String accessToken = authenticateAndGetToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Act
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/logout",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).contains("Logout successful");
        }
    }

    @Nested
    @DisplayName("Health Check Tests")
    class HealthCheckTests {

        @Test
        @DisplayName("Should return health status")
        void healthCheck_ShouldReturn200() {
            // Act
            ResponseEntity<String> response = restTemplate.getForEntity(
                    baseUrl + "/health",
                    String.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).contains("Service is healthy");
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle malformed JSON")
        void endpoint_WithMalformedJson_ShouldReturn400() {
            // Arrange
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>("{invalid json", headers);

            // Act
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/register",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Should handle unsupported HTTP method")
        void endpoint_WithUnsupportedMethod_ShouldReturn405() {
            // Act
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/register",
                    HttpMethod.DELETE,
                    null,
                    String.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        }

        @Test
        @DisplayName("Should handle non-existent endpoint")
        void nonExistentEndpoint_ShouldReturn404() {
            // Act
            ResponseEntity<String> response = restTemplate.getForEntity(
                    baseUrl + "/nonexistent",
                    String.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Helper method to authenticate and get access token.
     */
    private String authenticateAndGetToken() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("existinguser");
        authRequest.setPassword("password123");

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                baseUrl + "/login",
                authRequest,
                AuthResponse.class
        );

        return response.getBody().getAccessToken();
    }
}