package com.xypay.xypay.controller;

import com.xypay.xypay.domain.*;
import com.xypay.xypay.dto.*;
import com.xypay.xypay.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/target-savings/deposits")
@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPERUSER')")
public class TargetSavingDepositRestController {
    
    @Autowired
    private TargetSavingDepositRepository depositRepository;
    
    @Autowired
    private TargetSavingRepository targetSavingRepository;
    
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
     * Get all deposits for user's target savings
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserDeposits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated"));
            }
            
            Pageable pageable = PageRequest.of(page, size);
            Page<TargetSavingDeposit> deposits = depositRepository.findByTargetSavingUserOrderByDepositDateDesc(currentUser, pageable);
            
            List<TargetSavingDepositDTO> depositDTOs = deposits.getContent().stream()
                .map(TargetSavingDepositDTO::new)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("deposits", depositDTOs);
            response.put("count", deposits.getTotalElements());
            response.put("page", page);
            response.put("size", size);
            response.put("totalPages", deposits.getTotalPages());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to get deposits: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get deposits for a specific target saving
     */
    @GetMapping("/target/{targetId}")
    public ResponseEntity<Map<String, Object>> getTargetDeposits(
            @PathVariable UUID targetId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated"));
            }
            
            Optional<TargetSaving> targetOpt = targetSavingRepository.findByIdAndUser(targetId, currentUser);
            if (targetOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Target saving not found"));
            }
            
            TargetSaving targetSaving = targetOpt.get();
            Pageable pageable = PageRequest.of(page, size);
            Page<TargetSavingDeposit> deposits = depositRepository.findByTargetSavingOrderByDepositDateDesc(targetSaving, pageable);
            
            List<TargetSavingDepositDTO> depositDTOs = deposits.getContent().stream()
                .map(TargetSavingDepositDTO::new)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("deposits", depositDTOs);
            response.put("count", deposits.getTotalElements());
            response.put("page", page);
            response.put("size", size);
            response.put("totalPages", deposits.getTotalPages());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to get target deposits: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get deposit analytics across all targets
     */
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getDepositAnalytics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated"));
            }
            
            List<TargetSavingDeposit> deposits = depositRepository.findByTargetSavingUserOrderByDepositDateDesc(currentUser);
            
            // Apply date filters if provided
            if (startDate != null) {
                LocalDate start = LocalDate.parse(startDate);
                deposits = deposits.stream()
                    .filter(deposit -> deposit.getDepositDate().toLocalDate().isAfter(start) || 
                                     deposit.getDepositDate().toLocalDate().isEqual(start))
                    .collect(Collectors.toList());
            }
            
            if (endDate != null) {
                LocalDate end = LocalDate.parse(endDate);
                deposits = deposits.stream()
                    .filter(deposit -> deposit.getDepositDate().toLocalDate().isBefore(end) || 
                                     deposit.getDepositDate().toLocalDate().isEqual(end))
                    .collect(Collectors.toList());
            }
            
            // Calculate analytics
            int totalDeposits = deposits.size();
            BigDecimal totalAmount = deposits.stream()
                .map(TargetSavingDeposit::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal averageDeposit = totalDeposits > 0 ? 
                totalAmount.divide(new BigDecimal(totalDeposits), 2, java.math.RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
            
            // Monthly breakdown
            Map<String, Map<String, Object>> monthlyBreakdown = deposits.stream()
                .collect(Collectors.groupingBy(
                    deposit -> deposit.getDepositDate().getMonth().toString(),
                    Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> {
                            int count = list.size();
                            BigDecimal total = list.stream()
                                .map(TargetSavingDeposit::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                            Map<String, Object> result = new HashMap<>();
                            result.put("count", count);
                            result.put("total", total);
                            return result;
                        }
                    )
                ));
            
            // Target breakdown
            Map<String, Map<String, Object>> targetBreakdown = deposits.stream()
                .collect(Collectors.groupingBy(
                    deposit -> deposit.getTargetSaving().getName(),
                    Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> {
                            int count = list.size();
                            BigDecimal total = list.stream()
                                .map(TargetSavingDeposit::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                            Map<String, Object> result = new HashMap<>();
                            result.put("count", count);
                            result.put("total", total);
                            result.put("category", list.get(0).getTargetSaving().getCategory().getDescription());
                            return result;
                        }
                    )
                ));
            
            Map<String, Object> analytics = new HashMap<>();
            analytics.put("total_deposits", totalDeposits);
            analytics.put("total_amount", totalAmount);
            analytics.put("average_deposit", averageDeposit);
            analytics.put("monthly_breakdown", monthlyBreakdown);
            analytics.put("target_breakdown", targetBreakdown);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "analytics", analytics
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to get deposit analytics: " + e.getMessage()
            ));
        }
    }
}
