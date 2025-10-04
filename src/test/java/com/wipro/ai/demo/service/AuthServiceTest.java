package com.wipro.ai.demo.service;

import com.wipro.ai.demo.dto.*;
import com.wipro.ai.demo.exception.InvalidCredentialsException;
import com.wipro.ai.demo.exception.InvalidTokenException;
import com.wipro.ai.demo.exception.UserAlreadyExistsException;
import com.wipro.ai.demo.exception.UserNotFoundException;
import com.wipro.ai.demo.model.User;
import com.wipro.ai.demo.repository.UserRepository;
import com.wipro.ai.demo.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for AuthService.
 * 
 * Tests cover all business logic scenarios including:
 * - User registration (success and failure cases)
 * - Authentication (valid and invalid credentials)
 * - Token operations (generation, validation, refresh)
 * - User management operations
 * - Edge cases and error conditions
 * 
 * @author Wipro AI Demo Team
 * @version 1.0.0
 * @since 2024-10-03
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private RegisterRequest validRegisterRequest;
    private AuthRequest validAuthRequest;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setActive(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        // Create valid register request
        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setUsername("newuser");
        validRegisterRequest.setEmail("newuser@example.com");
        validRegisterRequest.setPassword("password123");
        validRegisterRequest.setFirstName("New");
        validRegisterRequest.setLastName("User");

        // Create valid auth request
        validAuthRequest = new AuthRequest();
        validAuthRequest.setUsername("testuser");
        validAuthRequest.setPassword("password123");
    }

    @Nested
    @DisplayName("User Registration Tests")
    class UserRegistrationTests {

        @Test
        @DisplayName("Should register user successfully with valid data")
        void registerUser_WithValidData_ShouldReturnUserResponse() {
            // Arrange
            when(userRepository.existsByUsername(validRegisterRequest.getUsername())).thenReturn(false);
            when(userRepository.existsByEmail(validRegisterRequest.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(validRegisterRequest.getPassword())).thenReturn("hashedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // Act
            UserResponse result = authService.registerUser(validRegisterRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo(testUser.getUsername());
            assertThat(result.getEmail()).isEqualTo(testUser.getEmail());
            assertThat(result.getFirstName()).isEqualTo(testUser.getFirstName());
            assertThat(result.getLastName()).isEqualTo(testUser.getLastName());

            verify(userRepository).existsByUsername(validRegisterRequest.getUsername());
            verify(userRepository).existsByEmail(validRegisterRequest.getEmail());
            verify(passwordEncoder).encode(validRegisterRequest.getPassword());
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when username already exists")
        void registerUser_WithExistingUsername_ShouldThrowUserAlreadyExistsException() {
            // Arrange
            when(userRepository.existsByUsername(validRegisterRequest.getUsername())).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> authService.registerUser(validRegisterRequest))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .hasMessageContaining("Username")
                    .hasMessageContaining(validRegisterRequest.getUsername());

            verify(userRepository).existsByUsername(validRegisterRequest.getUsername());
            verify(userRepository, never()).existsByEmail(any());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void registerUser_WithExistingEmail_ShouldThrowUserAlreadyExistsException() {
            // Arrange
            when(userRepository.existsByUsername(validRegisterRequest.getUsername())).thenReturn(false);
            when(userRepository.existsByEmail(validRegisterRequest.getEmail())).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> authService.registerUser(validRegisterRequest))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .hasMessageContaining("Email")
                    .hasMessageContaining(validRegisterRequest.getEmail());

            verify(userRepository).existsByUsername(validRegisterRequest.getUsername());
            verify(userRepository).existsByEmail(validRegisterRequest.getEmail());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should handle null values gracefully")
        void registerUser_WithNullValues_ShouldThrowException() {
            // Act & Assert
            assertThatThrownBy(() -> authService.registerUser(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("User Authentication Tests")
    class UserAuthenticationTests {

        @Mock
        private Authentication authentication;

        @Mock
        private UserDetails userDetails;

        @Test
        @DisplayName("Should authenticate user successfully with valid credentials")
        void authenticateUser_WithValidCredentials_ShouldReturnAuthResponse() {
            // Arrange
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(userDetails.getUsername()).thenReturn(testUser.getUsername());
            when(userRepository.findByUsernameOrEmail(validAuthRequest.getUsername(), validAuthRequest.getUsername()))
                    .thenReturn(Optional.of(testUser));
            when(jwtUtil.generateToken(userDetails)).thenReturn("access-token");
            when(jwtUtil.generateRefreshToken(userDetails)).thenReturn("refresh-token");

            // Act
            AuthResponse result = authService.authenticateUser(validAuthRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo("access-token");
            assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
            assertThat(result.getTokenType()).isEqualTo("Bearer");
            assertThat(result.getUsername()).isEqualTo(testUser.getUsername());
            assertThat(result.getEmail()).isEqualTo(testUser.getEmail());

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(jwtUtil).generateToken(userDetails);
            verify(jwtUtil).generateRefreshToken(userDetails);
            verify(userRepository).save(testUser); // For updating last login
        }

        @Test
        @DisplayName("Should throw exception with invalid credentials")
        void authenticateUser_WithInvalidCredentials_ShouldThrowInvalidCredentialsException() {
            // Arrange
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Invalid credentials"));

            // Act & Assert
            assertThatThrownBy(() -> authService.authenticateUser(validAuthRequest))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessageContaining("Invalid credentials");

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(jwtUtil, never()).generateToken(any());
            verify(jwtUtil, never()).generateRefreshToken(any());
        }

        @Test
        @DisplayName("Should throw exception when user not found after authentication")
        void authenticateUser_UserNotFoundAfterAuth_ShouldThrowUserNotFoundException() {
            // Arrange
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(userDetails.getUsername()).thenReturn(testUser.getUsername());
            when(userRepository.findByUsernameOrEmail(validAuthRequest.getUsername(), validAuthRequest.getUsername()))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authService.authenticateUser(validAuthRequest))
                    .isInstanceOf(UserNotFoundException.class);

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }
    }

    @Nested
    @DisplayName("Token Management Tests")
    class TokenManagementTests {

        @Test
        @DisplayName("Should refresh token successfully with valid refresh token")
        void refreshToken_WithValidToken_ShouldReturnNewAuthResponse() {
            // Arrange
            RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
            refreshRequest.setRefreshToken("valid-refresh-token");

            when(jwtUtil.isRefreshToken("valid-refresh-token")).thenReturn(true);
            when(jwtUtil.extractUsername("valid-refresh-token")).thenReturn(testUser.getUsername());
            when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
            when(userDetailsService.loadUserByUsername(testUser.getUsername())).thenReturn(mock(UserDetails.class));
            when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("new-access-token");
            when(jwtUtil.generateRefreshToken(any(UserDetails.class))).thenReturn("new-refresh-token");

            // Act
            AuthResponse result = authService.refreshToken(refreshRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo("new-access-token");
            assertThat(result.getRefreshToken()).isEqualTo("new-refresh-token");

            verify(jwtUtil).isRefreshToken("valid-refresh-token");
            verify(jwtUtil).extractUsername("valid-refresh-token");
            verify(jwtUtil).generateToken(any(UserDetails.class));
            verify(jwtUtil).generateRefreshToken(any(UserDetails.class));
        }

        @Test
        @DisplayName("Should throw exception with invalid refresh token")
        void refreshToken_WithInvalidToken_ShouldThrowInvalidTokenException() {
            // Arrange
            RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
            refreshRequest.setRefreshToken("invalid-refresh-token");

            when(jwtUtil.isRefreshToken("invalid-refresh-token")).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> authService.refreshToken(refreshRequest))
                    .isInstanceOf(InvalidTokenException.class)
                    .hasMessageContaining("Invalid refresh token");

            verify(jwtUtil).isRefreshToken("invalid-refresh-token");
            verify(jwtUtil, never()).generateToken(any());
        }

        @Test
        @DisplayName("Should validate token successfully")
        void validateToken_WithValidToken_ShouldReturnTrue() {
            // Arrange
            String validToken = "valid-token";
            when(tokenBlacklistService.isTokenBlacklisted(validToken)).thenReturn(false);
            when(jwtUtil.extractUsername(validToken)).thenReturn(testUser.getUsername());
            when(userDetailsService.loadUserByUsername(testUser.getUsername())).thenReturn(mock(UserDetails.class));
            when(jwtUtil.validateToken(eq(validToken), any(UserDetails.class))).thenReturn(true);

            // Act
            boolean result = authService.validateToken(validToken);

            // Assert
            assertThat(result).isTrue();

            verify(tokenBlacklistService).isTokenBlacklisted(validToken);
            verify(jwtUtil).extractUsername(validToken);
            verify(jwtUtil).validateToken(eq(validToken), any(UserDetails.class));
        }

        @Test
        @DisplayName("Should return false for blacklisted token")
        void validateToken_WithBlacklistedToken_ShouldReturnFalse() {
            // Arrange
            String blacklistedToken = "blacklisted-token";
            when(tokenBlacklistService.isTokenBlacklisted(blacklistedToken)).thenReturn(true);

            // Act
            boolean result = authService.validateToken(blacklistedToken);

            // Assert
            assertThat(result).isFalse();

            verify(tokenBlacklistService).isTokenBlacklisted(blacklistedToken);
            verify(jwtUtil, never()).extractUsername(any());
        }

        @Test
        @DisplayName("Should logout user and blacklist token")
        void logout_WithValidToken_ShouldBlacklistToken() {
            // Arrange
            String token = "valid-token";
            when(jwtUtil.extractExpiration(token)).thenReturn(new java.util.Date(System.currentTimeMillis() + 3600000));

            // Act
            authService.logout(token);

            // Assert
            verify(tokenBlacklistService).blacklistToken(eq(token), any());
        }
    }

    @Nested
    @DisplayName("User Management Tests")
    class UserManagementTests {

        @Test
        @DisplayName("Should get current user successfully")
        void getCurrentUser_WithValidUsername_ShouldReturnUserResponse() {
            // Arrange
            when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));

            // Act
            UserResponse result = authService.getCurrentUser(testUser.getUsername());

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo(testUser.getUsername());
            assertThat(result.getEmail()).isEqualTo(testUser.getEmail());

            verify(userRepository).findByUsername(testUser.getUsername());
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void getCurrentUser_WithInvalidUsername_ShouldThrowUserNotFoundException() {
            // Arrange
            String invalidUsername = "nonexistent";
            when(userRepository.findByUsername(invalidUsername)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authService.getCurrentUser(invalidUsername))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining(invalidUsername);

            verify(userRepository).findByUsername(invalidUsername);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty strings gracefully")
        void registerUser_WithEmptyStrings_ShouldThrowException() {
            // Arrange
            RegisterRequest emptyRequest = new RegisterRequest();
            emptyRequest.setUsername("");
            emptyRequest.setEmail("");
            emptyRequest.setPassword("");
            emptyRequest.setFirstName("");
            emptyRequest.setLastName("");

            // Act & Assert - This should be caught by validation, but let's test service behavior
            when(userRepository.existsByUsername("")).thenReturn(false);
            when(userRepository.existsByEmail("")).thenReturn(false);

            // This would typically be handled by validation annotations
            assertThatThrownBy(() -> authService.registerUser(emptyRequest))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Should handle database errors gracefully")
        void registerUser_WithDatabaseError_ShouldThrowException() {
            // Arrange
            when(userRepository.existsByUsername(validRegisterRequest.getUsername())).thenReturn(false);
            when(userRepository.existsByEmail(validRegisterRequest.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(validRegisterRequest.getPassword())).thenReturn("hashedPassword");
            when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

            // Act & Assert
            assertThatThrownBy(() -> authService.registerUser(validRegisterRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Database error");
        }

        @Test
        @DisplayName("Should get user statistics")
        void getUserStatistics_ShouldReturnStatistics() {
            // Arrange
            when(userRepository.count()).thenReturn(10L);
            when(userRepository.countByActiveTrue()).thenReturn(8L);
            when(userRepository.countByActiveTrue()).thenReturn(8L);

            // Act
            String result = authService.getUserStatistics();

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).contains("10"); // total users
            assertThat(result).contains("8");  // active users

            verify(userRepository, atLeastOnce()).count();
            verify(userRepository, atLeastOnce()).countByActiveTrue();
        }
    }
}