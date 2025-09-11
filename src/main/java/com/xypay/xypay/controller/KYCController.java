package com.xypay.xypay.controller;

import com.xypay.xypay.service.KYCService;
import com.xypay.xypay.service.KYCValidationService;
import com.xypay.xypay.repository.UserRepository;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/kyc")
@CrossOrigin(origins = "*") // Configure appropriately for production
public class KYCController {
    
    @Autowired
    private KYCService kycService;
    
    @Autowired
    private KYCValidationService kycValidationService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * Create KYC profile (matches Django KYCProfileSerializer.create)
     * Auto-approves and sets to Tier 1 like Django implementation
     * Example request body:
     * {
     *   "bvn": "12345678901",
     *   "nin": "12345678901",
     *   "dateOfBirth": "1990-01-01",
     *   "address": "123 Main Street, Lagos",
     *   "state": "Lagos",
     *   "lga": "Lagos Island",
     *   "gender": "MALE",
     *   "telephoneNumber": "08012345678",
     *   "passportPhoto": "path/to/passport.jpg",
     *   "selfie": "path/to/selfie.jpg",
     *   "idDocument": "path/to/id.jpg",
     *   "govtIdType": "NATIONAL_ID",
     *   "govtIdDocument": "path/to/govt_id.jpg",
     *   "proofOfAddress": "path/to/address_proof.jpg"
     * }
     */
    @PostMapping("/profile/{userId}")
    public ResponseEntity<Map<String, Object>> createKYCProfile(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> kycData) {
        
        Map<String, Object> response = kycService.createKYCProfile(userId, kycData);
        
        if ((Boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Update existing KYC profile
     * Example request body: (same as create)
     */
    @PutMapping("/profile/{userId}")
    public ResponseEntity<Map<String, Object>> updateKYCProfile(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> kycData) {
        
        Map<String, Object> response = kycService.updateKYCProfile(userId, kycData);
        
        if ((Boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get KYC profile for a user
     */
    @GetMapping("/profile/{userId}")
    public ResponseEntity<Map<String, Object>> getKYCProfile(@PathVariable Long userId) {
        Map<String, Object> response = kycService.getKYCProfile(userId);
        
        if ((Boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Request tier upgrade
     * Example request body:
     * {
     *   "target_tier": "TIER_2"
     * }
     */
    @PostMapping("/upgrade/{userId}")
    public ResponseEntity<Map<String, Object>> requestTierUpgrade(
            @PathVariable Long userId, 
            @RequestBody Map<String, String> request) {
        
        String targetTier = request.get("target_tier");
        Map<String, Object> response = kycService.requestTierUpgrade(userId, targetTier);
        
        if ((Boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get pending KYC approvals (Admin only)
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERUSER')")
    public ResponseEntity<List<Map<String, Object>>> getPendingApprovals() {
        List<Map<String, Object>> pendingList = kycService.getPendingApprovals();
        return ResponseEntity.ok(pendingList);
    }
    
    /**
     * Approve KYC profile (Admin only)
     * Example request body:
     * {
     *   "approver_id": 1
     * }
     */
    @PostMapping("/approve/{kycProfileId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERUSER')")
    public ResponseEntity<Map<String, Object>> approveKYC(
            @PathVariable String kycProfileId,
            @RequestBody Map<String, Long> request) {
        
        Long approverId = request.get("approver_id");
        Map<String, Object> response = kycService.approveKYC(kycProfileId, approverId);
        
        if ((Boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Reject KYC profile (Admin only)
     * Example request body:
     * {
     *   "reason": "Incomplete documentation"
     * }
     */
    @PostMapping("/reject/{kycProfileId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERUSER')")
    public ResponseEntity<Map<String, Object>> rejectKYC(
            @PathVariable String kycProfileId,
            @RequestBody Map<String, String> request) {
        
        String reason = request.get("reason");
        Map<String, Object> response = kycService.rejectKYC(kycProfileId, reason);
        
        if ((Boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get KYC tier limits and information
     */
    @GetMapping("/tiers")
    public ResponseEntity<Map<String, Object>> getTierInformation() {
        Map<String, Object> tierInfo = Map.of(
            "TIER_1", Map.of(
                "daily_transaction_limit", 50000.0,
                "max_balance_limit", 300000.0,
                "description", "Basic tier with limited transactions",
                "requirements", List.of(
                    "Phone number verification",
                    "Basic personal information"
                )
            ),
            "TIER_2", Map.of(
                "daily_transaction_limit", 200000.0,
                "max_balance_limit", 500000.0,
                "description", "Enhanced tier with moderate limits",
                "requirements", List.of(
                    "Tier 1 completion",
                    "BVN or NIN verification",
                    "Address verification"
                )
            ),
            "TIER_3", Map.of(
                "daily_transaction_limit", 5000000.0,
                "max_balance_limit", null, // Unlimited
                "description", "Premium tier with high limits",
                "requirements", List.of(
                    "Tier 2 completion",
                    "Both BVN and NIN verification",
                    "Government ID document",
                    "Proof of address",
                    "Additional documentation"
                )
            )
        );
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "tiers", tierInfo
        ));
    }
    
    /**
     * Validate NIN with JWT authentication
     * Example request body:
     * {
     *   "nin": "1002003004"
     * }
     * Requires: Authorization: Bearer <jwt_token>
     */
    @PostMapping("/validate-nin")
    public ResponseEntity<Map<String, Object>> validateNin(
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // Extract JWT token from Authorization header
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Authorization header with Bearer token is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            String token = authHeader.substring(7);
            String nin = request.get("nin");
            
            if (nin == null || nin.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "NIN is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Extract username from JWT token and get user
            String username = jwtUtil.extractUsername(token);
            User user = userRepository.findByUsername(username).orElse(null);
            
            if (user == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "User not found");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Validate NIN
            var validationResult = kycValidationService.validateNin(user, nin);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", validationResult.isSuccess());
            response.put("message", validationResult.getMessage());
            if (validationResult.getData() != null) {
                response.put("data", validationResult.getData());
            }
            response.put("fallback_used", validationResult.isFallbackUsed());
            
            if (validationResult.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "NIN validation failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Validate BVN with JWT authentication
     * Example request body:
     * {
     *   "bvn": "4002003055"
     * }
     * Requires: Authorization: Bearer <jwt_token>
     */
    @PostMapping("/validate-bvn")
    public ResponseEntity<Map<String, Object>> validateBvn(
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // Extract JWT token from Authorization header
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Authorization header with Bearer token is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            String token = authHeader.substring(7);
            String bvn = request.get("bvn");
            
            if (bvn == null || bvn.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "BVN is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Extract username from JWT token and get user
            String username = jwtUtil.extractUsername(token);
            User user = userRepository.findByUsername(username).orElse(null);
            
            if (user == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "User not found");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Validate BVN
            var validationResult = kycValidationService.validateBvn(user, bvn);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", validationResult.isSuccess());
            response.put("message", validationResult.getMessage());
            if (validationResult.getData() != null) {
                response.put("data", validationResult.getData());
            }
            response.put("fallback_used", validationResult.isFallbackUsed());
            
            if (validationResult.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "BVN validation failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "healthy",
            "service", "KYC Management API",
            "timestamp", System.currentTimeMillis()
        ));
    }
}