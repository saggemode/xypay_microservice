package com.xypay.xypay.controller;

import com.xypay.xypay.service.TransactionPinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.repository.UserRepository;

@RestController
@RequestMapping("/api/transaction-pin")
@CrossOrigin(origins = "*") // Configure appropriately for production
public class TransactionPinController {
    
    @Autowired
    private TransactionPinService transactionPinService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Set transaction PIN (first time setup)
     * Example request body:
     * {
     *   "pin": "1234"
     * }
     */
    @PostMapping("/set/{userId}")
    public ResponseEntity<Map<String, Object>> setTransactionPin(
            @PathVariable Long userId, 
            @RequestBody Map<String, String> request) {
        
        try {
            // Convert Long to UUID - this is a workaround for the ID type mismatch
            UUID userIdUuid = new UUID(0L, userId); // Create UUID from Long
            User user = userRepository.findById(userIdUuid).orElse(null);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "User not found"));
            }
            
            String pin = request.get("pin");
            Map<String, Object> response = transactionPinService.createTransactionPin(user, pin);
            
            if ((Boolean) response.get("success")) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Failed to set transaction PIN: " + e.getMessage()));
        }
    }
    
    /**
     * Update transaction PIN (requires old PIN)
     * Example request body:
     * {
     *   "old_pin": "1234",
     *   "new_pin": "5678"
     * }
     */
    @PutMapping("/update/{userId}")
    public ResponseEntity<Map<String, Object>> updateTransactionPin(
            @PathVariable Long userId, 
            @RequestBody Map<String, String> request) {
        
        try {
            // Convert Long to UUID - this is a workaround for the ID type mismatch
            UUID userIdUuid = new UUID(0L, userId); // Create UUID from Long
            User user = userRepository.findById(userIdUuid).orElse(null);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "User not found"));
            }
            
            // String oldPin = request.get("old_pin");
            // String newPin = request.get("new_pin");
            
            // TODO: Implement updateTransactionPin method in TransactionPinService
            // Map<String, Object> response = transactionPinService.updateTransactionPin(user, oldPin, newPin);
            Map<String, Object> response = Map.of("success", false, "error", "Update transaction PIN not implemented yet");
            
            if ((Boolean) response.get("success")) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Failed to update transaction PIN: " + e.getMessage()));
        }
    }
    
    /**
     * Verify transaction PIN
     * Example request body:
     * {
     *   "pin": "1234"
     * }
     */
    @PostMapping("/verify/{userId}")
    public ResponseEntity<Map<String, Object>> verifyTransactionPin(
            @PathVariable Long userId, 
            @RequestBody Map<String, String> request) {
        
        try {
            // Convert Long to UUID - this is a workaround for the ID type mismatch
            UUID userIdUuid = new UUID(0L, userId); // Create UUID from Long
            User user = userRepository.findById(userIdUuid).orElse(null);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "User not found"));
            }
            
            String pin = request.get("pin");
            Map<String, Object> response = transactionPinService.verifyTransactionPin(user, pin);
            
            if ((Boolean) response.get("success")) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Failed to verify transaction PIN: " + e.getMessage()));
        }
    }
    
    /**
     * Check if user has transaction PIN set
     */
    @GetMapping("/status/{userId}")
    public ResponseEntity<Map<String, Object>> hasTransactionPin(@PathVariable Long userId) {
        try {
            // Convert Long to UUID - this is a workaround for the ID type mismatch
            UUID userIdUuid = new UUID(0L, userId); // Create UUID from Long
            User user = userRepository.findById(userIdUuid).orElse(null);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "User not found"));
            }
            
            // TODO: Implement hasTransactionPin method in TransactionPinService
            // Map<String, Object> response = transactionPinService.hasTransactionPin(user);
            Map<String, Object> response = Map.of("success", true, "has_pin", false, "message", "Transaction PIN status check not implemented yet");
            
            if ((Boolean) response.get("success")) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Failed to check transaction PIN status: " + e.getMessage()));
        }
    }
    
    /**
     * Reset transaction PIN (Admin only)
     * Example request body:
     * {
     *   "admin_id": 1
     * }
     */
    @PostMapping("/reset/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERUSER')")
    public ResponseEntity<Map<String, Object>> resetTransactionPin(
            @PathVariable Long userId, 
            @RequestBody Map<String, Long> request) {
        
        try {
            // Convert Long to UUID - this is a workaround for the ID type mismatch
            UUID userIdUuid = new UUID(0L, userId); // Create UUID from Long
            User user = userRepository.findById(userIdUuid).orElse(null);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "User not found"));
            }
            
            // Long adminId = request.get("admin_id");
            // TODO: Implement resetTransactionPin method in TransactionPinService
            // Map<String, Object> response = transactionPinService.resetTransactionPin(user, adminId);
            Map<String, Object> response = Map.of("success", false, "error", "Reset transaction PIN not implemented yet");
            
            if ((Boolean) response.get("success")) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Failed to reset transaction PIN: " + e.getMessage()));
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "healthy",
            "service", "Transaction PIN API",
            "timestamp", System.currentTimeMillis()
        ));
    }
}