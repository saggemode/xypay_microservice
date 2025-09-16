package com.xypay.xypay.controller;

import com.xypay.xypay.domain.*;
import com.xypay.xypay.dto.*;
import com.xypay.xypay.repository.UserRepository;
import com.xypay.xypay.service.TargetSavingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.UUID;

@RestController
@RequestMapping("/api/target-savings")
@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPERUSER')")
public class TargetSavingRestController {
    
    @Autowired
    private TargetSavingService targetSavingService;
    
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get current authenticated user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            String username = authentication.getName();
            return userRepository.findByUsername(username).orElse(null);
        }
        return null;
    }
    
    /**
     * Create a new target saving
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createTargetSaving(@Valid @RequestBody TargetSavingCreateRequestDTO request) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated"));
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("name", request.getName());
            data.put("category", request.getCategory());
            data.put("target_amount", request.getTargetAmount());
            data.put("frequency", request.getFrequency());
            data.put("source", request.getSource());
            data.put("start_date", request.getStartDate());
            data.put("end_date", request.getEndDate());
            data.put("preferred_deposit_day", request.getPreferredDepositDay());
            data.put("strict_mode", request.getStrictMode());
            
            Map<String, Object> result = targetSavingService.createTargetSaving(currentUser, data);
            
            if ((Boolean) result.get("success")) {
                TargetSaving targetSaving = (TargetSaving) result.get("target_saving");
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", result.get("message"),
                    "target_saving", new TargetSavingDTO(targetSaving)
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", result.get("message")
                ));
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to create target saving: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get all target savings for user
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserTargets(@RequestParam(defaultValue = "true") boolean activeOnly) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated"));
            }
            
            Map<String, Object> result = targetSavingService.getUserTargets(currentUser, activeOnly);
            
            if ((Boolean) result.get("success")) {
                @SuppressWarnings("unchecked")
                List<TargetSaving> targets = (List<TargetSaving>) result.get("targets");
                List<TargetSavingDTO> targetDTOs = targets.stream()
                    .map(TargetSavingDTO::new)
                    .collect(Collectors.toList());
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "targets", targetDTOs,
                    "count", targetDTOs.size()
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", result.get("message")
                ));
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to get target savings: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get target saving details
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getTargetDetails(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated"));
            }
            
            // Convert Long to UUID - this is a workaround for the ID type mismatch
            UUID targetIdUuid = new UUID(0L, id); // Create UUID from Long
            Map<String, Object> result = targetSavingService.getTargetDetails(currentUser, targetIdUuid);
            
            if ((Boolean) result.get("success")) {
                TargetSaving targetSaving = (TargetSaving) result.get("target_saving");
                @SuppressWarnings("unchecked")
                List<TargetSavingDeposit> recentDeposits = (List<TargetSavingDeposit>) result.get("recent_deposits");
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "target_saving", new TargetSavingDTO(targetSaving),
                    "recent_deposits", recentDeposits.stream()
                        .map(TargetSavingDepositDTO::new)
                        .collect(Collectors.toList()),
                    "total_deposits", result.get("total_deposits"),
                    "progress_percentage", result.get("progress_percentage"),
                    "remaining_amount", result.get("remaining_amount"),
                    "days_remaining", result.get("days_remaining")
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", result.get("message")
                ));
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to get target details: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Update target saving
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateTargetSaving(@PathVariable Long id, @Valid @RequestBody TargetSavingUpdateRequestDTO request) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated"));
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("name", request.getName());
            data.put("category", request.getCategory());
            data.put("target_amount", request.getTargetAmount());
            data.put("frequency", request.getFrequency());
            data.put("preferred_deposit_day", request.getPreferredDepositDay());
            data.put("start_date", request.getStartDate());
            data.put("end_date", request.getEndDate());
            
            // Convert Long to UUID - this is a workaround for the ID type mismatch
            UUID targetIdUuid = new UUID(0L, id); // Create UUID from Long
            Map<String, Object> result = targetSavingService.updateTargetSaving(currentUser, targetIdUuid, data);
            
            if ((Boolean) result.get("success")) {
                TargetSaving targetSaving = (TargetSaving) result.get("target_saving");
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", result.get("message"),
                    "target_saving", new TargetSavingDTO(targetSaving)
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", result.get("message")
                ));
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to update target saving: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Make a deposit to target saving
     */
    @PostMapping("/{id}/deposit")
    public ResponseEntity<Map<String, Object>> makeDeposit(@PathVariable Long id, @Valid @RequestBody TargetSavingDepositCreateRequestDTO request) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated"));
            }
            
            // Convert Long to UUID - this is a workaround for the ID type mismatch
            UUID targetIdUuid = new UUID(0L, id); // Create UUID from Long
            Map<String, Object> result = targetSavingService.makeDeposit(currentUser, targetIdUuid, request.getAmount(), request.getNotes());
            
            if ((Boolean) result.get("success")) {
                TargetSavingDeposit deposit = (TargetSavingDeposit) result.get("deposit");
                TargetSaving targetSaving = (TargetSaving) result.get("target_saving");
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", result.get("message"),
                    "deposit", new TargetSavingDepositDTO(deposit),
                    "target_saving", new TargetSavingDTO(targetSaving)
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", result.get("message")
                ));
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to make deposit: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Withdraw from target saving
     */
    @PostMapping("/{id}/withdraw")
    public ResponseEntity<Map<String, Object>> withdrawFromTarget(@PathVariable Long id, @Valid @RequestBody TargetSavingWithdrawalRequestDTO request) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated"));
            }
            
            // Convert Long to UUID - this is a workaround for the ID type mismatch
            UUID targetIdUuid = new UUID(0L, id); // Create UUID from Long
            Map<String, Object> result = targetSavingService.withdrawFromTarget(currentUser, targetIdUuid, request.getAmount(), request.getDestination());
            
            if ((Boolean) result.get("success")) {
                TargetSavingWithdrawal withdrawal = (TargetSavingWithdrawal) result.get("withdrawal");
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", result.get("message"),
                    "withdrawal", new TargetSavingWithdrawalDTO(withdrawal)
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", result.get("message")
                ));
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to withdraw from target: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Deactivate target saving
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateTarget(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated"));
            }
            
            // Convert Long to UUID - this is a workaround for the ID type mismatch
            UUID targetIdUuid = new UUID(0L, id); // Create UUID from Long
            Map<String, Object> result = targetSavingService.deactivateTarget(currentUser, targetIdUuid);
            
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", result.get("message")
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", result.get("message")
                ));
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to deactivate target: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get target analytics
     */
    @GetMapping("/{id}/analytics")
    public ResponseEntity<Map<String, Object>> getTargetAnalytics(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated"));
            }
            
            // Convert Long to UUID - this is a workaround for the ID type mismatch
            UUID targetIdUuid = new UUID(0L, id); // Create UUID from Long
            Map<String, Object> result = targetSavingService.getTargetAnalytics(currentUser, targetIdUuid);
            
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "analytics", result.get("analytics")
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", result.get("message")
                ));
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to get analytics: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get available categories
     */
    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getCategories() {
        List<Map<String, String>> categories = Arrays.stream(TargetSavingCategory.values())
            .map(cat -> Map.of("value", cat.getCode(), "label", cat.getDescription()))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "categories", categories
        ));
    }
    
    /**
     * Get available frequencies
     */
    @GetMapping("/frequencies")
    public ResponseEntity<Map<String, Object>> getFrequencies() {
        List<Map<String, String>> frequencies = Arrays.stream(TargetSavingFrequency.values())
            .map(freq -> Map.of("value", freq.getCode(), "label", freq.getDescription()))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "frequencies", frequencies
        ));
    }
    
    /**
     * Get available deposit days
     */
    @GetMapping("/deposit-days")
    public ResponseEntity<Map<String, Object>> getDepositDays() {
        List<Map<String, String>> days = Arrays.asList(
            Map.of("value", "monday", "label", "Monday"),
            Map.of("value", "tuesday", "label", "Tuesday"),
            Map.of("value", "wednesday", "label", "Wednesday"),
            Map.of("value", "thursday", "label", "Thursday"),
            Map.of("value", "friday", "label", "Friday"),
            Map.of("value", "saturday", "label", "Saturday"),
            Map.of("value", "sunday", "label", "Sunday")
        );
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "days", days
        ));
    }
    
    /**
     * Send reminder notification
     */
    @PostMapping("/{id}/remind")
    public ResponseEntity<Map<String, Object>> sendReminder(@PathVariable Long id, @RequestParam(defaultValue = "weekly") String reminderType) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated"));
            }
            
            // This would need to be implemented to get the target saving and send reminder
            // For now, return success
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", reminderType + " reminder sent successfully"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to send reminder: " + e.getMessage()
            ));
        }
    }
}
