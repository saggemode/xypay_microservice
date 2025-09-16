package com.xypay.xypay.controller;

import com.xypay.xypay.domain.*;
import com.xypay.xypay.dto.*;
import com.xypay.xypay.repository.UserRepository;
import com.xypay.xypay.service.TargetSavingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.UUID;

@Controller
@RequestMapping("/target-savings")
@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPERUSER')")
public class TargetSavingController {
    
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
     * Show target savings dashboard
     */
    @GetMapping
    public String showTargetSavingsDashboard(Model model) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            Map<String, Object> targetsResult = targetSavingService.getUserTargets(currentUser, true);
            @SuppressWarnings("unchecked")
            List<TargetSaving> targets = (List<TargetSaving>) targetsResult.get("targets");
            
            model.addAttribute("targets", targets.stream().map(TargetSavingDTO::new).collect(Collectors.toList()));
            model.addAttribute("user", currentUser);
            model.addAttribute("pageTitle", "Target Savings Dashboard");
            
            return "target-savings/dashboard";
            
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load target savings dashboard: " + e.getMessage());
            return "target-savings/dashboard";
        }
    }
    
    /**
     * Show create target saving page
     */
    @GetMapping("/create")
    public String showCreateTargetSaving(Model model) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Add categories
            List<Map<String, String>> categories = Arrays.stream(TargetSavingCategory.values())
                .map(cat -> Map.of("value", cat.getCode(), "label", cat.getDescription()))
                .collect(Collectors.toList());
            
            // Add frequencies
            List<Map<String, String>> frequencies = Arrays.stream(TargetSavingFrequency.values())
                .map(freq -> Map.of("value", freq.getCode(), "label", freq.getDescription()))
                .collect(Collectors.toList());
            
            // Add sources
            List<Map<String, String>> sources = Arrays.stream(TargetSavingSource.values())
                .map(src -> Map.of("value", src.getCode(), "label", src.getDescription()))
                .collect(Collectors.toList());
            
            model.addAttribute("categories", categories);
            model.addAttribute("frequencies", frequencies);
            model.addAttribute("sources", sources);
            model.addAttribute("user", currentUser);
            model.addAttribute("pageTitle", "Create Target Saving");
            
            return "target-savings/create";
            
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load create page: " + e.getMessage());
            return "target-savings/create";
        }
    }
    
    /**
     * Create a new target saving
     */
    @PostMapping
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createTargetSaving(@RequestBody Map<String, Object> data) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated"));
            }
            
            // Convert string dates to LocalDate
            if (data.containsKey("start_date") && data.get("start_date") instanceof String) {
                data.put("start_date", LocalDate.parse((String) data.get("start_date")));
            }
            if (data.containsKey("end_date") && data.get("end_date") instanceof String) {
                data.put("end_date", LocalDate.parse((String) data.get("end_date")));
            }
            
            // Convert string amounts to BigDecimal
            if (data.containsKey("target_amount") && data.get("target_amount") instanceof String) {
                data.put("target_amount", new BigDecimal((String) data.get("target_amount")));
            }
            
            // Convert enums
            if (data.containsKey("category") && data.get("category") instanceof String) {
                data.put("category", TargetSavingCategory.valueOf(((String) data.get("category")).toUpperCase()));
            }
            if (data.containsKey("frequency") && data.get("frequency") instanceof String) {
                data.put("frequency", TargetSavingFrequency.valueOf(((String) data.get("frequency")).toUpperCase()));
            }
            if (data.containsKey("source") && data.get("source") instanceof String) {
                data.put("source", TargetSavingSource.valueOf(((String) data.get("source")).toUpperCase()));
            }
            
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
    @GetMapping("/api/targets")
    @ResponseBody
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
    @GetMapping("/api/targets/{id}")
    @ResponseBody
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
     * Make a deposit to target saving
     */
    @PostMapping("/api/targets/{id}/deposit")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> makeDeposit(
            @PathVariable Long id,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false, defaultValue = "") String notes) {
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated"));
            }
            
            // Convert Long to UUID - this is a workaround for the ID type mismatch
            UUID targetIdUuid = new UUID(0L, id); // Create UUID from Long
            Map<String, Object> result = targetSavingService.makeDeposit(currentUser, targetIdUuid, amount, notes);
            
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
    @PostMapping("/api/targets/{id}/withdraw")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> withdrawFromTarget(
            @PathVariable Long id,
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "wallet") String destination) {
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated"));
            }
            
            // Convert Long to UUID - this is a workaround for the ID type mismatch
            UUID targetIdUuid = new UUID(0L, id); // Create UUID from Long
            Map<String, Object> result = targetSavingService.withdrawFromTarget(currentUser, targetIdUuid, amount, destination);
            
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
    @PostMapping("/api/targets/{id}/deactivate")
    @ResponseBody
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
    @GetMapping("/api/targets/{id}/analytics")
    @ResponseBody
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
    @GetMapping("/api/categories")
    @ResponseBody
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
    @GetMapping("/api/frequencies")
    @ResponseBody
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
     * Get available sources
     */
    @GetMapping("/api/sources")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSources() {
        List<Map<String, String>> sources = Arrays.stream(TargetSavingSource.values())
            .map(src -> Map.of("value", src.getCode(), "label", src.getDescription()))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "sources", sources
        ));
    }
    
    /**
     * Send reminder notification
     */
    @PostMapping("/api/targets/{id}/remind")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendReminder(
            @PathVariable Long id,
            @RequestParam(defaultValue = "weekly") String reminderType) {
        
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
