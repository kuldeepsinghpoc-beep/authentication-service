package com.wipro.ai.demo.repository;

import com.wipro.ai.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository interface for User entity data access operations.
 * 
 * This repository provides comprehensive data access methods for user management,
 * including custom queries for authentication, user lookup, and account validation.
 * All methods are optimized for performance with proper indexing on the database side.
 * 
 * Features:
 * - Standard CRUD operations through JpaRepository
 * - Custom finder methods for authentication scenarios
 * - Existence checks to prevent duplicate registrations
 * - Active user filtering for security
 * - Flexible login support (username or email)
 * 
 * @author Wipro AI Demo Team
 * @version 1.0.0
 * @since 2024-10-03
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their username.
     * 
     * @param username the username to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByUsername(String username);

    /**
     * Find a user by their email address.
     * 
     * @param email the email address to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Find a user by either username or email address.
     * This method supports flexible login where users can use either credential.
     * 
     * @param username the username to search for
     * @param email the email address to search for
     * @return Optional containing the user if found, empty otherwise
     */
    @Query("SELECT u FROM User u WHERE u.username = :username OR u.email = :email")
    Optional<User> findByUsernameOrEmail(@Param("username") String username, @Param("email") String email);

    /**
     * Check if a username already exists in the system.
     * Used during registration to prevent duplicate usernames.
     * 
     * @param username the username to check
     * @return true if username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Check if an email address already exists in the system.
     * Used during registration to prevent duplicate email addresses.
     * 
     * @param email the email address to check
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find an active user by their username.
     * Only returns users with active=true for security purposes.
     * 
     * @param username the username to search for
     * @return Optional containing the active user if found, empty otherwise
     */
    Optional<User> findByUsernameAndActiveTrue(String username);

    /**
     * Find an active user by their email address.
     * Only returns users with active=true for security purposes.
     * 
     * @param email the email address to search for
     * @return Optional containing the active user if found, empty otherwise
     */
    Optional<User> findByEmailAndActiveTrue(String email);

    /**
     * Find an active user by either username or email address.
     * Combines flexible login with active user filtering.
     * 
     * @param usernameOrEmail the username or email to search for
     * @return Optional containing the active user if found, empty otherwise
     */
    @Query("SELECT u FROM User u WHERE (u.username = :usernameOrEmail OR u.email = :usernameOrEmail) AND u.active = true")
    Optional<User> findByUsernameOrEmailAndActiveTrue(@Param("usernameOrEmail") String usernameOrEmail);

    /**
     * Check if a username exists and belongs to an active user.
     * 
     * @param username the username to check
     * @return true if an active user with this username exists, false otherwise
     */
    boolean existsByUsernameAndActiveTrue(String username);

    /**
     * Check if an email exists and belongs to an active user.
     * 
     * @param email the email to check
     * @return true if an active user with this email exists, false otherwise
     */
    boolean existsByEmailAndActiveTrue(String email);

    /**
     * Find users who haven't logged in since a specific date.
     * Useful for identifying inactive accounts.
     * 
     * @param date the cutoff date
     * @return list of users who haven't logged in since the specified date
     */
    @Query("SELECT u FROM User u WHERE u.lastLogin < :date OR u.lastLogin IS NULL")
    java.util.List<User> findUsersNotLoggedInSince(@Param("date") LocalDateTime date);

    /**
     * Count the total number of active users.
     * 
     * @return the count of active users
     */
    long countByActiveTrue();

    /**
     * Update the last login timestamp for a user.
     * 
     * @param username the username of the user
     * @param loginTime the login timestamp
     * @return the number of updated records (should be 1 for success)
     */
    @Query("UPDATE User u SET u.lastLogin = :loginTime WHERE u.username = :username")
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    int updateLastLogin(@Param("username") String username, @Param("loginTime") LocalDateTime loginTime);
}