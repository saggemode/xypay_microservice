package com.xypay.xypay.admin;

import com.xypay.xypay.service.XySaveService;
import com.xypay.xypay.domain.XySaveAccount;
import com.xypay.xypay.domain.XySaveTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERUSER')")
public class XySaveAdminController {
    
    @Autowired
    private XySaveService xySaveService;
    
    /**
     * Main XySave management page
     */
    @GetMapping("/xysave")
    public String xySaveManagement(Model model) {
        // Get statistics
        Map<String, Object> stats = xySaveService.getXySaveStatistics();
        model.addAttribute("stats", stats);
        
        // Get recent transactions
        List<Map<String, Object>> recentTransactions = xySaveService.getRecentTransactions(10);
        model.addAttribute("recentTransactions", recentTransactions);
        
        // Get users for dropdown
        List<Map<String, Object>> users = xySaveService.getAllUsers();
        model.addAttribute("users", users);
        
        model.addAttribute("pageTitle", "XySave Account Management");
        return "admin/xysave";
    }
    
    /**
     * API: Get XySave statistics
     */
    @GetMapping("/xysave/api/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = xySaveService.getXySaveStatistics();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * API: Get recent transactions
     */
    @GetMapping("/xysave/api/transactions")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getRecentTransactions(
            @RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> transactions = xySaveService.getRecentTransactions(limit);
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * API: Get all users
     */
    @GetMapping("/xysave/api/users")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getUsers() {
        List<Map<String, Object>> users = xySaveService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    /**
     * API: Get all XySave accounts with pagination
     */
    @GetMapping("/xysave/api/accounts")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<XySaveAccount> accountsPage = xySaveService.getAllXySaveAccounts(pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("accounts", accountsPage.getContent());
        response.put("totalElements", accountsPage.getTotalElements());
        response.put("totalPages", accountsPage.getTotalPages());
        response.put("currentPage", page);
        response.put("size", size);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * API: Create investment for user
     */
    @PostMapping("/xysave/api/invest")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createInvestment(
            @RequestParam UUID userId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description) {
        
        try {
            if (description == null || description.trim().isEmpty()) {
                description = "XySave investment";
            }
            
            XySaveTransaction transaction = xySaveService.createInvestment(userId, amount, description);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Investment created successfully");
            response.put("transactionId", transaction.getId());
            response.put("reference", transaction.getReference());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error creating investment: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * API: Process withdrawal for user
     */
    @PostMapping("/xysave/api/withdraw")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> processWithdrawal(
            @RequestParam UUID userId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description) {
        
        try {
            if (description == null || description.trim().isEmpty()) {
                description = "XySave withdrawal";
            }
            
            XySaveTransaction transaction = xySaveService.processWithdrawal(userId, amount, description);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Withdrawal processed successfully");
            response.put("transactionId", transaction.getId());
            response.put("reference", transaction.getReference());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error processing withdrawal: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * API: Update XySave settings
     */
    @PostMapping("/xysave/api/settings")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateSettings(
            @RequestParam UUID userId,
            @RequestParam(required = false) BigDecimal dailyInterestRate,
            @RequestParam(required = false) Boolean autoSaveEnabled,
            @RequestParam(required = false) BigDecimal autoSavePercentage,
            @RequestParam(required = false) BigDecimal autoSaveMinAmount) {
        
        try {
            XySaveAccount account = xySaveService.updateSettings(
                userId, dailyInterestRate, autoSaveEnabled, 
                autoSavePercentage, autoSaveMinAmount);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Settings updated successfully");
            response.put("accountId", account.getId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error updating settings: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * API: Calculate and credit interest for user
     */
    @PostMapping("/xysave/api/calculate-interest")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> calculateInterest(@RequestParam UUID userId) {
        try {
            xySaveService.calculateAndCreditInterest(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Interest calculated and credited successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error calculating interest: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * API: Get user's XySave account details
     */
    @GetMapping("/xysave/api/account/{userId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAccountDetails(@PathVariable UUID userId) {
        try {
            Optional<XySaveAccount> accountOpt = xySaveService.getXySaveAccount(userId);
            
            if (accountOpt.isPresent()) {
                XySaveAccount account = accountOpt.get();
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("account", account);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "XySave account not found for user");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving account: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
