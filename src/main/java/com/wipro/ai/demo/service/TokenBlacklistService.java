package com.wipro.ai.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing JWT token blacklist to handle logout functionality.
 * 
 * This service maintains a blacklist of invalidated JWT tokens to prevent their reuse
 * after logout. In a production environment, this should be replaced with a distributed
 * cache like Redis for scalability across multiple application instances.
 * 
 * Features:
 * - Thread-safe token blacklist management
 * - Automatic cleanup of expired tokens
 * - Memory-efficient storage
 * - Configurable cleanup intervals
 * 
 * @author Wipro AI Demo Team
 * @version 1.0.0
 * @since 2024-10-03
 */
@Service
public class TokenBlacklistService {

    private static final Logger logger = LoggerFactory.getLogger(TokenBlacklistService.class);

    /**
     * Thread-safe map to store blacklisted tokens with their expiration times.
     * Key: JWT token, Value: Expiration timestamp
     */
    private final ConcurrentHashMap<String, LocalDateTime> blacklistedTokens = new ConcurrentHashMap<>();

    /**
     * Scheduled executor for automatic cleanup of expired tokens.
     */
    private final ScheduledExecutorService cleanupExecutor = Executors.newScheduledThreadPool(1);

    /**
     * Constructor that starts the automatic cleanup process.
     */
    public TokenBlacklistService() {
        // Schedule cleanup every hour
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredTokens, 1, 1, TimeUnit.HOURS);
        logger.info("Token blacklist service initialized with automatic cleanup");
    }

    /**
     * Add a token to the blacklist.
     * 
     * @param token the JWT token to blacklist
     * @param expirationTime when the token expires (used for cleanup)
     */
    public void blacklistToken(String token, LocalDateTime expirationTime) {
        if (token == null || token.trim().isEmpty()) {
            logger.warn("Attempted to blacklist null or empty token");
            return;
        }

        blacklistedTokens.put(token, expirationTime);
        logger.debug("Token blacklisted successfully. Total blacklisted tokens: {}", blacklistedTokens.size());
    }

    /**
     * Check if a token is blacklisted.
     * 
     * @param token the JWT token to check
     * @return true if token is blacklisted, false otherwise
     */
    public boolean isTokenBlacklisted(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        boolean isBlacklisted = blacklistedTokens.containsKey(token);
        if (isBlacklisted) {
            logger.debug("Token found in blacklist");
        }
        return isBlacklisted;
    }

    /**
     * Remove a token from the blacklist.
     * This method is typically used for testing or administrative purposes.
     * 
     * @param token the JWT token to remove from blacklist
     * @return true if token was removed, false if it wasn't blacklisted
     */
    public boolean removeFromBlacklist(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        LocalDateTime removed = blacklistedTokens.remove(token);
        boolean wasRemoved = removed != null;
        if (wasRemoved) {
            logger.debug("Token removed from blacklist successfully");
        }
        return wasRemoved;
    }

    /**
     * Get the current number of blacklisted tokens.
     * 
     * @return the count of blacklisted tokens
     */
    public int getBlacklistedTokenCount() {
        return blacklistedTokens.size();
    }

    /**
     * Clear all blacklisted tokens.
     * This method should be used with caution, typically only for testing.
     */
    public void clearAllTokens() {
        int clearedCount = blacklistedTokens.size();
        blacklistedTokens.clear();
        logger.info("Cleared {} blacklisted tokens", clearedCount);
    }

    /**
     * Cleanup expired tokens from the blacklist.
     * This method is called automatically by the scheduled executor.
     */
    private void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        int initialSize = blacklistedTokens.size();

        // Remove expired tokens
        blacklistedTokens.entrySet().removeIf(entry -> {
            LocalDateTime expiration = entry.getValue();
            return expiration.isBefore(now);
        });

        int finalSize = blacklistedTokens.size();
        int removedCount = initialSize - finalSize;

        if (removedCount > 0) {
            logger.info("Cleaned up {} expired tokens from blacklist. Remaining: {}", removedCount, finalSize);
        } else {
            logger.debug("No expired tokens to cleanup. Current blacklist size: {}", finalSize);
        }
    }

    /**
     * Get statistics about the blacklist.
     * 
     * @return a string with blacklist statistics
     */
    public String getStatistics() {
        LocalDateTime now = LocalDateTime.now();
        long expiredCount = blacklistedTokens.values().stream()
                .mapToLong(expiration -> expiration.isBefore(now) ? 1 : 0)
                .sum();
        
        return String.format("Total blacklisted tokens: %d, Expired tokens: %d, Active tokens: %d",
                blacklistedTokens.size(), expiredCount, blacklistedTokens.size() - expiredCount);
    }

    /**
     * Shutdown the cleanup executor when the service is destroyed.
     */
    public void shutdown() {
        cleanupExecutor.shutdown();
        try {
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("Token blacklist service shutdown completed");
    }
}