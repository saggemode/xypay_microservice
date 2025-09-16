package com.xypay.xypay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Service for generating and validating device fingerprints.
 * Device fingerprinting helps identify and track devices for security purposes.
 */
@Service
public class DeviceFingerprintService {
    
    private static final Logger logger = LoggerFactory.getLogger(DeviceFingerprintService.class);
    
    /**
     * Generate a device fingerprint from request data.
     * 
     * @param request HTTP request object
     * @return Device fingerprint hash
     */
    public String generateDeviceFingerprint(HttpServletRequest request) {
        try {
            // Collect device information from request headers
            String userAgent = request.getHeader("User-Agent");
            String acceptLanguage = request.getHeader("Accept-Language");
            String acceptEncoding = request.getHeader("Accept-Encoding");
            
            // Get IP address
            String ip = getClientIp(request);
            
            // Combine device data
            Map<String, String> deviceData = new HashMap<>();
            deviceData.put("user_agent", userAgent != null ? userAgent : "");
            deviceData.put("accept_language", acceptLanguage != null ? acceptLanguage : "");
            deviceData.put("accept_encoding", acceptEncoding != null ? acceptEncoding : "");
            deviceData.put("ip_address", ip);
            // Add more device characteristics as needed
            
            // Generate hash
            String dataString = deviceData.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((a, b) -> a + "&" + b)
                .orElse("");
            
            return generateHash(dataString);
            
        } catch (Exception e) {
            logger.error("Error generating device fingerprint: {}", e.getMessage());
            return generateFallbackFingerprint(request);
        }
    }
    
    /**
     * Get client IP address from request.
     * 
     * @param request HTTP request object
     * @return Client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        try {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                // Take first IP if multiple are present
                return xForwardedFor.split(",")[0].trim();
            } else {
                String xRealIp = request.getHeader("X-Real-IP");
                return xRealIp != null ? xRealIp : request.getRemoteAddr();
            }
        } catch (Exception e) {
            logger.error("Error getting client IP: {}", e.getMessage());
            return "";
        }
    }
    
    /**
     * Generate a basic fallback fingerprint when full fingerprinting fails.
     * 
     * @param request HTTP request object
     * @return Basic device fingerprint
     */
    private String generateFallbackFingerprint(HttpServletRequest request) {
        try {
            // Use basic data that should always be available
            String basicData = String.format(
                "ip=%s&user_agent=%s&timestamp=%d",
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                System.currentTimeMillis()
            );
            
            return generateHash(basicData);
            
        } catch (Exception e) {
            logger.error("Error generating fallback fingerprint: {}", e.getMessage());
            return generateHash(String.valueOf(System.currentTimeMillis()));
        }
    }
    
    /**
     * Generate SHA-256 hash of the input string.
     * 
     * @param input Input string to hash
     * @return SHA-256 hash as hex string
     */
    private String generateHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("SHA-256 algorithm not available: {}", e.getMessage());
            return Integer.toHexString(input.hashCode());
        }
    }
    
    /**
     * Compare two fingerprints and return similarity score.
     * 
     * @param fingerprint1 First fingerprint
     * @param fingerprint2 Second fingerprint
     * @return Similarity score (0-1)
     */
    public double compareFingerprints(String fingerprint1, String fingerprint2) {
        try {
            if (fingerprint1 == null || fingerprint2 == null) {
                return 0.0;
            }
                
            // Convert to sets of characters for comparison
            Set<Character> set1 = fingerprint1.chars()
                .mapToObj(c -> (char) c)
                .collect(java.util.stream.Collectors.toSet());
            Set<Character> set2 = fingerprint2.chars()
                .mapToObj(c -> (char) c)
                .collect(java.util.stream.Collectors.toSet());
            
            // Calculate Jaccard similarity
            Set<Character> intersection = new java.util.HashSet<>(set1);
            intersection.retainAll(set2);
            
            Set<Character> union = new java.util.HashSet<>(set1);
            union.addAll(set2);
            
            return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
            
        } catch (Exception e) {
            logger.error("Error comparing fingerprints: {}", e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Check if two fingerprints are similar enough to be considered the same device.
     * 
     * @param fingerprint1 First fingerprint
     * @param fingerprint2 Second fingerprint
     * @param threshold Similarity threshold (0-1)
     * @return True if fingerprints are similar enough
     */
    public boolean areFingerprintsSimilar(String fingerprint1, String fingerprint2, double threshold) {
        return compareFingerprints(fingerprint1, fingerprint2) >= threshold;
    }
    
    /**
     * Check if two fingerprints are similar enough to be considered the same device (default threshold).
     * 
     * @param fingerprint1 First fingerprint
     * @param fingerprint2 Second fingerprint
     * @return True if fingerprints are similar enough
     */
    public boolean areFingerprintsSimilar(String fingerprint1, String fingerprint2) {
        return areFingerprintsSimilar(fingerprint1, fingerprint2, 0.8); // 80% similarity threshold
    }
    
    /**
     * Extract device characteristics from request for analysis.
     * 
     * @param request HTTP request object
     * @return Map of device characteristics
     */
    public Map<String, String> extractDeviceCharacteristics(HttpServletRequest request) {
        Map<String, String> characteristics = new HashMap<>();
        
        try {
            characteristics.put("user_agent", request.getHeader("User-Agent"));
            characteristics.put("accept_language", request.getHeader("Accept-Language"));
            characteristics.put("accept_encoding", request.getHeader("Accept-Encoding"));
            characteristics.put("accept", request.getHeader("Accept"));
            characteristics.put("connection", request.getHeader("Connection"));
            characteristics.put("upgrade_insecure_requests", request.getHeader("Upgrade-Insecure-Requests"));
            characteristics.put("sec_fetch_dest", request.getHeader("Sec-Fetch-Dest"));
            characteristics.put("sec_fetch_mode", request.getHeader("Sec-Fetch-Mode"));
            characteristics.put("sec_fetch_site", request.getHeader("Sec-Fetch-Site"));
            characteristics.put("sec_fetch_user", request.getHeader("Sec-Fetch-User"));
            characteristics.put("cache_control", request.getHeader("Cache-Control"));
            characteristics.put("pragma", request.getHeader("Pragma"));
            characteristics.put("ip_address", getClientIp(request));
            
            // Remove null values
            characteristics.entrySet().removeIf(entry -> entry.getValue() == null);
            
        } catch (Exception e) {
            logger.error("Error extracting device characteristics: {}", e.getMessage());
        }
        
        return characteristics;
    }
}
