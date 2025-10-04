package com.wipro.ai.demo.security;

import com.wipro.ai.demo.model.User;
import com.wipro.ai.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Custom implementation of Spring Security's UserDetailsService.
 * 
 * This service loads user-specific data for authentication and authorization.
 * It integrates with the User entity and repository to provide user details
 * to Spring Security for authentication processes.
 * 
 * Features:
 * - Load user by username or email (flexible login)
 * - Convert User entity to Spring Security UserDetails
 * - Handle active/inactive user accounts
 * - Comprehensive error handling and logging
 * - Role and authority management
 * 
 * @author Wipro AI Demo Team
 * @version 1.0.0
 * @since 2024-10-03
 */
@Service
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;

    /**
     * Constructor with dependency injection.
     * 
     * @param userRepository the user repository
     */
    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Load user details by username or email.
     * This method supports flexible login where users can use either credential.
     * 
     * @param usernameOrEmail the username or email
     * @return UserDetails for the authenticated user
     * @throws UsernameNotFoundException if user is not found or inactive
     */
    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        logger.debug("Loading user details for: {}", usernameOrEmail);

        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
            logger.warn("Attempted to load user with null or empty username/email");
            throw new UsernameNotFoundException("Username or email cannot be empty");
        }

        // Find user by username or email and ensure they are active
        User user = userRepository.findByUsernameOrEmailAndActiveTrue(usernameOrEmail.trim())
                .orElseThrow(() -> {
                    logger.warn("User not found or inactive: {}", usernameOrEmail);
                    return new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail);
                });

        logger.debug("User found successfully: {} (ID: {})", user.getUsername(), user.getId());
        return createUserDetails(user);
    }

    /**
     * Convert User entity to Spring Security UserDetails.
     * 
     * @param user the User entity
     * @return UserDetails implementation
     */
    private UserDetails createUserDetails(User user) {
        Collection<GrantedAuthority> authorities = getAuthorities(user);

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(!user.getActive())
                .credentialsExpired(false)
                .disabled(!user.getActive())
                .build();
    }

    /**
     * Get authorities (roles) for the user.
     * Currently, all users have ROLE_USER. This can be extended to support
     * different roles based on user type or permissions.
     * 
     * @param user the User entity
     * @return collection of granted authorities
     */
    private Collection<GrantedAuthority> getAuthorities(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Default role for all users
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        
        // Future enhancement: Add role-based authorities
        // if (user.isAdmin()) {
        //     authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        // }
        
        logger.debug("User {} has authorities: {}", user.getUsername(), authorities);
        return authorities;
    }

    /**
     * Load user details by user ID.
     * This method can be used for loading user details when you have the user ID.
     * 
     * @param userId the user ID
     * @return UserDetails for the user
     * @throws UsernameNotFoundException if user is not found
     */
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        logger.debug("Loading user details for ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {}", userId);
                    return new UsernameNotFoundException("User not found with ID: " + userId);
                });

        if (!user.getActive()) {
            logger.warn("Attempted to load inactive user with ID: {}", userId);
            throw new UsernameNotFoundException("User account is inactive");
        }

        logger.debug("User found successfully by ID: {} (Username: {})", userId, user.getUsername());
        return createUserDetails(user);
    }

    /**
     * Check if a user exists and is active.
     * 
     * @param usernameOrEmail the username or email to check
     * @return true if user exists and is active, false otherwise
     */
    public boolean userExistsAndActive(String usernameOrEmail) {
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
            return false;
        }

        return userRepository.findByUsernameOrEmailAndActiveTrue(usernameOrEmail.trim()).isPresent();
    }

    /**
     * Get the User entity for a given username or email.
     * This method is useful when you need the full User entity, not just UserDetails.
     * 
     * @param usernameOrEmail the username or email
     * @return the User entity
     * @throws UsernameNotFoundException if user is not found
     */
    public User getUserByUsernameOrEmail(String usernameOrEmail) throws UsernameNotFoundException {
        return userRepository.findByUsernameOrEmailAndActiveTrue(usernameOrEmail.trim())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + usernameOrEmail));
    }
}