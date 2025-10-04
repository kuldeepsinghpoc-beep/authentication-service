package com.wipro.ai.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main application class for the Authentication Service.
 * 
 * This Spring Boot application provides a comprehensive JWT-based authentication service
 * with user management, secure token handling, and RESTful API endpoints.
 * 
 * Features:
 * - JWT token-based authentication and authorization
 * - User registration and login functionality
 * - Secure password encryption with BCrypt
 * - Token refresh and blacklisting capabilities
 * - Comprehensive validation and error handling
 * - Swagger/OpenAPI documentation
 * - H2 and MySQL database support
 * 
 * @author Wipro AI Demo Team
 * @version 1.0.0
 * @since 2024-10-03
 */
@SpringBootApplication
@EnableJpaRepositories
public class AuthenticationServiceApplication {

    /**
     * Main method to start the Spring Boot application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(AuthenticationServiceApplication.class, args);
    }
}