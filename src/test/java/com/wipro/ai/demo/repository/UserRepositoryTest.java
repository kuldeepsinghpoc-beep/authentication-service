package com.wipro.ai.demo.repository;

import com.wipro.ai.demo.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive integration tests for UserRepository.
 * 
 * Tests verify:
 * - Basic CRUD operations
 * - Custom query methods
 * - Unique constraints
 * - Data validation
 * - Entity relationships
 * 
 * @author Wipro AI Demo Team
 * @version 1.0.0
 * @since 2024-10-03
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository Integration Tests")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setActive(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperationsTests {

        @Test
        @DisplayName("Should save user successfully")
        void save_ValidUser_ShouldPersistUser() {
            // Act
            User savedUser = userRepository.save(testUser);

            // Assert
            assertThat(savedUser).isNotNull();
            assertThat(savedUser.getId()).isNotNull();
            assertThat(savedUser.getUsername()).isEqualTo(testUser.getUsername());
            assertThat(savedUser.getEmail()).isEqualTo(testUser.getEmail());
            assertThat(savedUser.getCreatedAt()).isNotNull();
            assertThat(savedUser.getUpdatedAt()).isNotNull();

            // Verify persistence
            User foundUser = entityManager.find(User.class, savedUser.getId());
            assertThat(foundUser).isEqualTo(savedUser);
        }

        @Test
        @DisplayName("Should find user by ID")
        void findById_ExistingUser_ShouldReturnUser() {
            // Arrange
            User savedUser = entityManager.persistAndFlush(testUser);

            // Act
            Optional<User> foundUser = userRepository.findById(savedUser.getId());

            // Assert
            assertThat(foundUser).isPresent();
            assertThat(foundUser.get().getUsername()).isEqualTo(testUser.getUsername());
            assertThat(foundUser.get().getEmail()).isEqualTo(testUser.getEmail());
        }

        @Test
        @DisplayName("Should return empty when user not found by ID")
        void findById_NonExistentUser_ShouldReturnEmpty() {
            // Act
            Optional<User> foundUser = userRepository.findById(999L);

            // Assert
            assertThat(foundUser).isEmpty();
        }

        @Test
        @DisplayName("Should update user successfully")
        void save_UpdateExistingUser_ShouldUpdateUser() {
            // Arrange
            User savedUser = entityManager.persistAndFlush(testUser);
            entityManager.detach(savedUser);

            // Act
            savedUser.setFirstName("Updated");
            savedUser.setLastName("Name");
            savedUser.setUpdatedAt(LocalDateTime.now());
            User updatedUser = userRepository.save(savedUser);

            // Assert
            assertThat(updatedUser.getFirstName()).isEqualTo("Updated");
            assertThat(updatedUser.getLastName()).isEqualTo("Name");

            // Verify persistence
            User foundUser = entityManager.find(User.class, updatedUser.getId());
            assertThat(foundUser.getFirstName()).isEqualTo("Updated");
            assertThat(foundUser.getLastName()).isEqualTo("Name");
        }

        @Test
        @DisplayName("Should delete user successfully")
        void delete_ExistingUser_ShouldDeleteUser() {
            // Arrange
            User savedUser = entityManager.persistAndFlush(testUser);
            Long userId = savedUser.getId();

            // Act
            userRepository.delete(savedUser);
            entityManager.flush();

            // Assert
            User foundUser = entityManager.find(User.class, userId);
            assertThat(foundUser).isNull();
        }
    }

    @Nested
    @DisplayName("Custom Query Methods")
    class CustomQueryTests {

        @Test
        @DisplayName("Should find user by username")
        void findByUsername_ExistingUser_ShouldReturnUser() {
            // Arrange
            entityManager.persistAndFlush(testUser);

            // Act
            Optional<User> foundUser = userRepository.findByUsername(testUser.getUsername());

            // Assert
            assertThat(foundUser).isPresent();
            assertThat(foundUser.get().getUsername()).isEqualTo(testUser.getUsername());
            assertThat(foundUser.get().getEmail()).isEqualTo(testUser.getEmail());
        }

        @Test
        @DisplayName("Should return empty for non-existent username")
        void findByUsername_NonExistentUser_ShouldReturnEmpty() {
            // Act
            Optional<User> foundUser = userRepository.findByUsername("nonexistent");

            // Assert
            assertThat(foundUser).isEmpty();
        }

        @Test
        @DisplayName("Should find user by email")
        void findByEmail_ExistingUser_ShouldReturnUser() {
            // Arrange
            entityManager.persistAndFlush(testUser);

            // Act
            Optional<User> foundUser = userRepository.findByEmail(testUser.getEmail());

            // Assert
            assertThat(foundUser).isPresent();
            assertThat(foundUser.get().getEmail()).isEqualTo(testUser.getEmail());
            assertThat(foundUser.get().getUsername()).isEqualTo(testUser.getUsername());
        }

        @Test
        @DisplayName("Should find user by username or email")
        void findByUsernameOrEmail_WithUsername_ShouldReturnUser() {
            // Arrange
            entityManager.persistAndFlush(testUser);

            // Act
            Optional<User> foundUser = userRepository.findByUsernameOrEmail(testUser.getUsername(), testUser.getUsername());

            // Assert
            assertThat(foundUser).isPresent();
            assertThat(foundUser.get().getUsername()).isEqualTo(testUser.getUsername());
        }

        @Test
        @DisplayName("Should find user by username or email with email")
        void findByUsernameOrEmail_WithEmail_ShouldReturnUser() {
            // Arrange
            entityManager.persistAndFlush(testUser);

            // Act
            Optional<User> foundUser = userRepository.findByUsernameOrEmail(testUser.getEmail(), testUser.getEmail());

            // Assert
            assertThat(foundUser).isPresent();
            assertThat(foundUser.get().getEmail()).isEqualTo(testUser.getEmail());
        }

        @Test
        @DisplayName("Should check if username exists")
        void existsByUsername_ExistingUser_ShouldReturnTrue() {
            // Arrange
            entityManager.persistAndFlush(testUser);

            // Act
            boolean exists = userRepository.existsByUsername(testUser.getUsername());

            // Assert
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should check if username does not exist")
        void existsByUsername_NonExistentUser_ShouldReturnFalse() {
            // Act
            boolean exists = userRepository.existsByUsername("nonexistent");

            // Assert
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("Should check if email exists")
        void existsByEmail_ExistingUser_ShouldReturnTrue() {
            // Arrange
            entityManager.persistAndFlush(testUser);

            // Act
            boolean exists = userRepository.existsByEmail(testUser.getEmail());

            // Assert
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should count active users")
        void countByActiveTrue_WithActiveUsers_ShouldReturnCount() {
            // Arrange
            entityManager.persistAndFlush(testUser);

            User inactiveUser = new User();
            inactiveUser.setUsername("inactive");
            inactiveUser.setEmail("inactive@example.com");
            inactiveUser.setPassword("password");
            inactiveUser.setFirstName("Inactive");
            inactiveUser.setLastName("User");
            inactiveUser.setActive(false);
            entityManager.persistAndFlush(inactiveUser);

            // Act
            long activeCount = userRepository.countByActiveTrue();

            // Assert
            assertThat(activeCount).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Constraint Validation Tests")
    class ConstraintTests {

        @Test
        @DisplayName("Should enforce unique username constraint")
        void save_DuplicateUsername_ShouldThrowException() {
            // Arrange
            entityManager.persistAndFlush(testUser);

            User duplicateUser = new User();
            duplicateUser.setUsername(testUser.getUsername()); // Same username
            duplicateUser.setEmail("different@example.com"); // Different email
            duplicateUser.setPassword("password");
            duplicateUser.setFirstName("Different");
            duplicateUser.setLastName("User");
            duplicateUser.setActive(true);

            // Act & Assert
            assertThatThrownBy(() -> {
                userRepository.save(duplicateUser);
                entityManager.flush();
            }).isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Should enforce unique email constraint")
        void save_DuplicateEmail_ShouldThrowException() {
            // Arrange
            entityManager.persistAndFlush(testUser);

            User duplicateUser = new User();
            duplicateUser.setUsername("differentuser");
            duplicateUser.setEmail(testUser.getEmail()); // Same email
            duplicateUser.setPassword("password");
            duplicateUser.setFirstName("Different");
            duplicateUser.setLastName("User");
            duplicateUser.setActive(true);

            // Act & Assert
            assertThatThrownBy(() -> {
                userRepository.save(duplicateUser);
                entityManager.flush();
            }).isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Should enforce not null constraints")
        void save_NullRequiredFields_ShouldThrowException() {
            // Arrange
            User invalidUser = new User();
            // Leaving required fields null

            // Act & Assert
            assertThatThrownBy(() -> {
                userRepository.save(invalidUser);
                entityManager.flush();
            }).isInstanceOf(Exception.class); // Validation or constraint violation
        }
    }

    @Nested
    @DisplayName("Data Integrity Tests")
    class DataIntegrityTests {

        @Test
        @DisplayName("Should handle case-sensitive usernames")
        void save_CaseSensitiveUsernames_ShouldAllowBoth() {
            // Arrange
            entityManager.persistAndFlush(testUser);

            User upperCaseUser = new User();
            upperCaseUser.setUsername(testUser.getUsername().toUpperCase());
            upperCaseUser.setEmail("uppercase@example.com");
            upperCaseUser.setPassword("password");
            upperCaseUser.setFirstName("Upper");
            upperCaseUser.setLastName("Case");
            upperCaseUser.setActive(true);

            // Act
            User savedUser = userRepository.save(upperCaseUser);

            // Assert
            assertThat(savedUser).isNotNull();
            assertThat(savedUser.getId()).isNotNull();
            assertThat(savedUser.getUsername()).isEqualTo(testUser.getUsername().toUpperCase());
        }

        @Test
        @DisplayName("Should handle special characters in names")
        void save_SpecialCharactersInNames_ShouldSaveSuccessfully() {
            // Arrange
            testUser.setFirstName("José");
            testUser.setLastName("O'Connor");

            // Act
            User savedUser = userRepository.save(testUser);

            // Assert
            assertThat(savedUser).isNotNull();
            assertThat(savedUser.getFirstName()).isEqualTo("José");
            assertThat(savedUser.getLastName()).isEqualTo("O'Connor");
        }

        @Test
        @DisplayName("Should handle long field values within limits")
        void save_LongFieldValues_ShouldSaveSuccessfully() {
            // Arrange
            String longName = "A".repeat(50); // Max length for names
            testUser.setFirstName(longName);
            testUser.setLastName(longName);

            // Act
            User savedUser = userRepository.save(testUser);

            // Assert
            assertThat(savedUser).isNotNull();
            assertThat(savedUser.getFirstName()).isEqualTo(longName);
            assertThat(savedUser.getLastName()).isEqualTo(longName);
        }
    }
}