package com.xypay.xypay.service;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Service for handling idempotent requests to prevent duplicate transactions.
 */
@Service
public class IdempotencyService {
    
    private static final Logger logger = LoggerFactory.getLogger(IdempotencyService.class);
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    private static final String IDEMPOTENCY_KEY_PREFIX = "idempotency:";
    private static final long IDEMPOTENCY_TIMEOUT_HOURS = 24; // 24 hour timeout
    
    /**
     * Get idempotency key from request headers.
     * 
     * @param request HTTP request object
     * @return Idempotency key
     */
    public String getIdempotencyKey(HttpServletRequest request) {
        return request.getHeader("X-Idempotency-Key");
    }
    
    /**
     * Check if request with given idempotency key is a duplicate.
     * 
     * @param idempotencyKey Idempotency key from request
     * @param userId ID of user making request
     * @return True if request is duplicate
     */
    public boolean isDuplicateRequest(String idempotencyKey, UUID userId) {
        try {
            if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
                return false;
            }
                
            // Create unique cache key for user+idempotency combination
            String cacheKey = IDEMPOTENCY_KEY_PREFIX + userId + ":" + idempotencyKey;
            
            // Try to set the key with expiration
            // If key already exists, request is duplicate
            Boolean isNewKey = redisTemplate.opsForValue().setIfAbsent(
                cacheKey, 
                "1", 
                Duration.ofHours(IDEMPOTENCY_TIMEOUT_HOURS)
            );
            
            return !Boolean.TRUE.equals(isNewKey);
            
        } catch (Exception e) {
            logger.error("Error checking idempotency: {}", e.getMessage());
            return false; // Allow request to proceed on error
        }
    }
    
    /**
     * Clear idempotency key from cache.
     * 
     * @param idempotencyKey Idempotency key to clear
     * @param userId ID of user who made request
     */
    public void clearIdempotencyKey(String idempotencyKey, UUID userId) {
        try {
            if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
                String cacheKey = IDEMPOTENCY_KEY_PREFIX + userId + ":" + idempotencyKey;
                redisTemplate.delete(cacheKey);
                logger.debug("Cleared idempotency key for user {}: {}", userId, idempotencyKey);
            }
        } catch (Exception e) {
            logger.error("Error clearing idempotency key: {}", e.getMessage());
        }
    }
    
    /**
     * Store idempotency key with custom timeout.
     * 
     * @param idempotencyKey Idempotency key to store
     * @param userId ID of user making request
     * @param timeoutHours Timeout in hours
     * @return True if key was stored successfully
     */
    public boolean storeIdempotencyKey(String idempotencyKey, UUID userId, long timeoutHours) {
        try {
            if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
                return false;
            }
            
            String cacheKey = IDEMPOTENCY_KEY_PREFIX + userId + ":" + idempotencyKey;
            
            Boolean isNewKey = redisTemplate.opsForValue().setIfAbsent(
                cacheKey, 
                "1", 
                Duration.ofHours(timeoutHours)
            );
            
            return Boolean.TRUE.equals(isNewKey);
            
        } catch (Exception e) {
            logger.error("Error storing idempotency key: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if idempotency key exists without creating it.
     * 
     * @param idempotencyKey Idempotency key to check
     * @param userId ID of user making request
     * @return True if key exists
     */
    public boolean hasIdempotencyKey(String idempotencyKey, UUID userId) {
        try {
            if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
                return false;
            }
            
            String cacheKey = IDEMPOTENCY_KEY_PREFIX + userId + ":" + idempotencyKey;
            return Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey));
            
        } catch (Exception e) {
            logger.error("Error checking idempotency key existence: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Get remaining TTL for an idempotency key.
     * 
     * @param idempotencyKey Idempotency key to check
     * @param userId ID of user making request
     * @return TTL in seconds, or -1 if key doesn't exist
     */
    public long getIdempotencyKeyTtl(String idempotencyKey, UUID userId) {
        try {
            if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
                return -1;
            }
            
            String cacheKey = IDEMPOTENCY_KEY_PREFIX + userId + ":" + idempotencyKey;
            Long ttl = redisTemplate.getExpire(cacheKey, TimeUnit.SECONDS);
            return ttl != null ? ttl : -1;
            
        } catch (Exception e) {
            logger.error("Error getting idempotency key TTL: {}", e.getMessage());
            return -1;
        }
    }
    
    /**
     * Extend idempotency key expiration.
     * 
     * @param idempotencyKey Idempotency key to extend
     * @param userId ID of user making request
     * @param additionalHours Additional hours to add
     * @return True if key was extended successfully
     */
    public boolean extendIdempotencyKey(String idempotencyKey, UUID userId, long additionalHours) {
        try {
            if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
                return false;
            }
            
            String cacheKey = IDEMPOTENCY_KEY_PREFIX + userId + ":" + idempotencyKey;
            
            // Check if key exists
            if (!Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey))) {
                return false;
            }
            
            // Extend expiration
            Boolean extended = redisTemplate.expire(
                cacheKey, 
                Duration.ofHours(additionalHours)
            );
            
            return Boolean.TRUE.equals(extended);
            
        } catch (Exception e) {
            logger.error("Error extending idempotency key: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Clean up expired idempotency keys (this is usually handled by Redis TTL).
     * This method is provided for manual cleanup if needed.
     * 
     * @return Number of keys cleaned up
     */
    public long cleanupExpiredKeys() {
        try {
            // Redis automatically handles TTL cleanup, but this method can be used
            // for additional cleanup logic if needed
            logger.debug("Idempotency key cleanup completed (handled by Redis TTL)");
            return 0;
        } catch (Exception e) {
            logger.error("Error during idempotency key cleanup: {}", e.getMessage());
            return 0;
        }
    }
}
