package com.wipro.ai.demo.security;

import com.wipro.ai.demo.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter that processes JWT tokens on each request.
 * 
 * This filter extracts JWT tokens from the Authorization header, validates them,
 * and sets the security context for authenticated requests. It also checks
 * against the token blacklist to prevent use of logged-out tokens.
 * 
 * Features:
 * - Extract JWT tokens from Authorization header
 * - Validate tokens using JwtUtil
 * - Check against token blacklist
 * - Set Spring Security context for valid tokens
 * - Comprehensive error handling and logging
 * - Skip processing for public endpoints
 * 
 * @author Wipro AI Demo Team
 * @version 1.0.0
 * @since 2024-10-03
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * Constructor with dependency injection.
     * 
     * @param jwtUtil the JWT utility service
     * @param userDetailsService the custom user details service
     * @param tokenBlacklistService the token blacklist service
     */
    public JwtAuthenticationFilter(JwtUtil jwtUtil, 
                                 CustomUserDetailsService userDetailsService,
                                 TokenBlacklistService tokenBlacklistService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    /**
     * Filter method that processes each HTTP request for JWT authentication.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                  @NonNull HttpServletResponse response, 
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            String authHeader = request.getHeader("Authorization");
            String token = null;
            String username = null;

            // Extract token from Authorization header
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
                logger.debug("JWT token found in request header");

                try {
                    username = jwtUtil.extractUsername(token);
                    logger.debug("Username extracted from token: {}", username);
                } catch (Exception e) {
                    logger.warn("Failed to extract username from token: {}", e.getMessage());
                }
            } else {
                logger.debug("No JWT token found in request header");
            }

            // Process token if we have username and no existing authentication
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // Check if token is blacklisted
                if (tokenBlacklistService.isTokenBlacklisted(token)) {
                    logger.warn("Attempted to use blacklisted token for user: {}", username);
                    // Don't set authentication, let request proceed without authentication
                } else {
                    try {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        
                        // Validate token
                        if (jwtUtil.validateToken(token, userDetails)) {
                            // Create authentication token
                            UsernamePasswordAuthenticationToken authToken = 
                                new UsernamePasswordAuthenticationToken(
                                    userDetails, 
                                    null, 
                                    userDetails.getAuthorities()
                                );
                            
                            // Set authentication details
                            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            
                            // Set authentication in security context
                            SecurityContextHolder.getContext().setAuthentication(authToken);
                            
                            logger.debug("Successfully authenticated user: {}", username);
                        } else {
                            logger.warn("Invalid JWT token for user: {}", username);
                        }
                    } catch (Exception e) {
                        logger.error("Error during JWT authentication for user {}: {}", username, e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Unexpected error in JWT authentication filter: {}", e.getMessage());
        }

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Determine if this filter should be skipped for the current request.
     * Override this method to skip JWT processing for specific endpoints.
     * 
     * @param request the HTTP request
     * @return true if filter should be skipped, false otherwise
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // Skip JWT processing for public endpoints
        return path.startsWith("/api/auth/register") || 
               path.startsWith("/api/auth/login") ||
               path.startsWith("/h2-console") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/actuator/health");
    }
}