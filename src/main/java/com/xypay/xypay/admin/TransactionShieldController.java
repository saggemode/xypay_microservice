package com.xypay.xypay.admin;

import com.xypay.xypay.domain.LargeTransactionShieldSettings;
import com.xypay.xypay.service.TransactionShieldService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class TransactionShieldController {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionShieldController.class);
    
    @Autowired
    private TransactionShieldService transactionShieldService;
    
    /**
     * Main transaction shield page
     */
    @GetMapping("/transaction-shield")
    public String transactionShield(Model model) {
        try {
            // Get statistics
            Map<String, Object> stats = transactionShieldService.getShieldStatistics();
            model.addAttribute("stats", stats);
            
            // Get recent activity
            List<Map<String, Object>> recentActivity = transactionShieldService.getRecentShieldActivity(10);
            model.addAttribute("recentActivity", recentActivity);
            
            // Get shield settings with pagination
            Pageable pageable = PageRequest.of(0, 20);
            Page<LargeTransactionShieldSettings> shieldSettings = transactionShieldService.getAllShieldSettings(pageable);
            model.addAttribute("shieldSettings", shieldSettings.getContent());
            
            logger.info("Transaction shield page loaded with {} active shields", stats.get("activeShields"));
            
        } catch (Exception e) {
            logger.error("Error loading transaction shield page: {}", e.getMessage());
            model.addAttribute("error", "Failed to load transaction shield data");
        }
        
        return "admin/transaction-shield";
    }
    
    /**
     * Get shield statistics API
     */
    @GetMapping("/transaction-shield/api/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStats() {
        try {
            Map<String, Object> stats = transactionShieldService.getShieldStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error getting shield statistics: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get statistics");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Get recent activity API
     */
    @GetMapping("/transaction-shield/api/activity")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getRecentActivity(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> activity = transactionShieldService.getRecentShieldActivity(limit);
            return ResponseEntity.ok(activity);
        } catch (Exception e) {
            logger.error("Error getting recent activity: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(List.of());
        }
    }
    
    /**
     * Get all shield settings API
     */
    @GetMapping("/transaction-shield/api/settings")
    @ResponseBody
    public ResponseEntity<Page<LargeTransactionShieldSettings>> getShieldSettings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<LargeTransactionShieldSettings> settings = transactionShieldService.getAllShieldSettings(pageable);
            return ResponseEntity.ok(settings);
        } catch (Exception e) {
            logger.error("Error getting shield settings: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Page.empty());
        }
    }
    
    /**
     * Create or update shield settings API
     */
    @PostMapping("/transaction-shield/api/settings")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveShieldSettings(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            BigDecimal perTransactionLimit = new BigDecimal(request.get("perTransactionLimit").toString());
            BigDecimal dailyLimit = new BigDecimal(request.get("dailyLimit").toString());
            BigDecimal monthlyLimit = new BigDecimal(request.get("monthlyLimit").toString());
            boolean enableFaceRecognition = Boolean.parseBoolean(request.get("enableFaceRecognition").toString());
            String faceTemplateHash = request.get("faceTemplateHash") != null ? 
                request.get("faceTemplateHash").toString() : null;
            
            // Convert Long to UUID - this is a workaround for the ID type mismatch
            UUID userIdUuid = new UUID(0L, userId); // Create UUID from Long
            LargeTransactionShieldSettings settings = transactionShieldService.saveShieldSettings(
                userIdUuid, perTransactionLimit, dailyLimit, monthlyLimit, enableFaceRecognition, faceTemplateHash);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Shield settings saved successfully");
            response.put("settings", settings);
            
            logger.info("Shield settings saved for user {}", userId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error saving shield settings: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Failed to save shield settings: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Update shield limits API
     */
    @PutMapping("/transaction-shield/api/limits")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateShieldLimits(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            BigDecimal perTransactionLimit = new BigDecimal(request.get("perTransactionLimit").toString());
            BigDecimal dailyLimit = new BigDecimal(request.get("dailyLimit").toString());
            BigDecimal monthlyLimit = new BigDecimal(request.get("monthlyLimit").toString());
            
            // Convert Long to UUID - this is a workaround for the ID type mismatch
            UUID userIdUuid = new UUID(0L, userId); // Create UUID from Long
            LargeTransactionShieldSettings settings = transactionShieldService.updateShieldLimits(
                userIdUuid, perTransactionLimit, dailyLimit, monthlyLimit);
            
            Map<String, Object> response = new HashMap<>();
            if (settings != null) {
                response.put("success", true);
                response.put("message", "Shield limits updated successfully");
                response.put("settings", settings);
            } else {
                response.put("success", false);
                response.put("error", "Shield settings not found for user");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error updating shield limits: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Failed to update shield limits: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Update biometric settings API
     */
    @PutMapping("/transaction-shield/api/biometric")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateBiometricSettings(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            boolean enableFingerprint = Boolean.parseBoolean(request.get("enableFingerprint").toString());
            boolean enableFaceRecognition = Boolean.parseBoolean(request.get("enableFaceRecognition").toString());
            boolean enableVoiceRecognition = Boolean.parseBoolean(request.get("enableVoiceRecognition").toString());
            boolean enablePinVerification = Boolean.parseBoolean(request.get("enablePinVerification").toString());
            BigDecimal biometricThreshold = new BigDecimal(request.get("biometricThreshold").toString());
            
            // Convert Long to UUID - this is a workaround for the ID type mismatch
            UUID userIdUuid = new UUID(0L, userId); // Create UUID from Long
            LargeTransactionShieldSettings settings = transactionShieldService.updateBiometricSettings(
                userIdUuid, enableFingerprint, enableFaceRecognition, enableVoiceRecognition, 
                enablePinVerification, biometricThreshold);
            
            Map<String, Object> response = new HashMap<>();
            if (settings != null) {
                response.put("success", true);
                response.put("message", "Biometric settings updated successfully");
                response.put("settings", settings);
            } else {
                response.put("success", false);
                response.put("error", "Shield settings not found for user");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error updating biometric settings: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Failed to update biometric settings: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Enable shield for user API
     */
    @PostMapping("/transaction-shield/api/enable")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> enableShield(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            BigDecimal perTransactionLimit = new BigDecimal(request.get("perTransactionLimit").toString());
            BigDecimal dailyLimit = new BigDecimal(request.get("dailyLimit").toString());
            BigDecimal monthlyLimit = new BigDecimal(request.get("monthlyLimit").toString());
            
            // Convert Long to UUID - this is a workaround for the ID type mismatch
            UUID userIdUuid = new UUID(0L, userId); // Create UUID from Long
            LargeTransactionShieldSettings settings = transactionShieldService.enableShield(
                userIdUuid, perTransactionLimit, dailyLimit, monthlyLimit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Shield enabled successfully");
            response.put("settings", settings);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error enabling shield: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Failed to enable shield: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Disable shield for user API
     */
    @PostMapping("/transaction-shield/api/disable")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> disableShield(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            
            // Convert Long to UUID - this is a workaround for the ID type mismatch
            UUID userIdUuid = new UUID(0L, userId); // Create UUID from Long
            LargeTransactionShieldSettings settings = transactionShieldService.disableShield(userIdUuid);
            
            Map<String, Object> response = new HashMap<>();
            if (settings != null) {
                response.put("success", true);
                response.put("message", "Shield disabled successfully");
                response.put("settings", settings);
            } else {
                response.put("success", false);
                response.put("error", "Shield settings not found for user");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error disabling shield: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Failed to disable shield: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Delete shield settings API
     */
    @DeleteMapping("/transaction-shield/api/settings/{userId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteShieldSettings(@PathVariable Long userId) {
        try {
            // Convert Long to UUID - this is a workaround for the ID type mismatch
            UUID userIdUuid = new UUID(0L, userId); // Create UUID from Long
            transactionShieldService.deleteShieldSettings(userIdUuid);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Shield settings deleted successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error deleting shield settings: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Failed to delete shield settings: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Get shield settings for user API
     */
    @GetMapping("/transaction-shield/api/settings/{userId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getShieldSettingsForUser(@PathVariable Long userId) {
        try {
            // Convert Long to UUID - this is a workaround for the ID type mismatch
            UUID userIdUuid = new UUID(0L, userId); // Create UUID from Long
            Optional<LargeTransactionShieldSettings> settings = transactionShieldService.getShieldSettings(userIdUuid);
            
            Map<String, Object> response = new HashMap<>();
            if (settings.isPresent()) {
                response.put("success", true);
                response.put("settings", settings.get());
            } else {
                response.put("success", false);
                response.put("error", "Shield settings not found for user");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting shield settings for user {}: {}", userId, e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Failed to get shield settings: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
