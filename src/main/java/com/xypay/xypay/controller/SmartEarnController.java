package com.xypay.xypay.controller;

import com.xypay.xypay.domain.SmartEarnAccount;
import com.xypay.xypay.domain.SmartEarnTransaction;
import com.xypay.xypay.domain.SmartEarnInterestHistory;
import com.xypay.xypay.service.SmartEarnService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/smartearn")
@CrossOrigin(origins = "*")
public class SmartEarnController {
    
    private static final Logger logger = LoggerFactory.getLogger(SmartEarnController.class);
    
    @Autowired
    private SmartEarnService smartEarnService;
    
    /**
     * Create a new SmartEarn account
     */
    @PostMapping("/create-account")
    public ResponseEntity<Map<String, Object>> createAccount(@RequestParam UUID userId) {
        try {
            SmartEarnAccount account = smartEarnService.createSmartEarnAccount(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "SmartEarn account created successfully");
            response.put("account", account);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error creating SmartEarn account: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to create SmartEarn account: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get SmartEarn account details
     */
    @GetMapping("/account")
    public ResponseEntity<Map<String, Object>> getAccount(@RequestParam UUID userId) {
        try {
            SmartEarnAccount account = smartEarnService.getAccount(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("account", account);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting SmartEarn account: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get SmartEarn account: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Deposit money into SmartEarn account
     */
    @PostMapping("/deposit")
    public ResponseEntity<Map<String, Object>> deposit(
            @RequestParam UUID userId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description) {
        
        try {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Amount must be greater than zero");
                return ResponseEntity.badRequest().body(response);
            }
            
            SmartEarnTransaction transaction = smartEarnService.deposit(userId, amount, description);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Deposit processed successfully");
            response.put("transaction", transaction);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing SmartEarn deposit: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to process deposit: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Withdraw money from SmartEarn account
     */
    @PostMapping("/withdraw")
    public ResponseEntity<Map<String, Object>> withdraw(
            @RequestParam UUID userId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description) {
        
        try {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Amount must be greater than zero");
                return ResponseEntity.badRequest().body(response);
            }
            
            SmartEarnTransaction transaction = smartEarnService.withdraw(userId, amount, description);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Withdrawal processed successfully");
            response.put("transaction", transaction);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing SmartEarn withdrawal: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to process withdrawal: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get transaction history
     */
    @GetMapping("/transactions")
    public ResponseEntity<Map<String, Object>> getTransactionHistory(
            @RequestParam UUID userId,
            @RequestParam(defaultValue = "50") int limit) {
        
        try {
            List<SmartEarnTransaction> transactions = smartEarnService.getTransactionHistory(userId, limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("transactions", transactions);
            response.put("count", transactions.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting SmartEarn transaction history: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get transaction history: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get interest history
     */
    @GetMapping("/interest-history")
    public ResponseEntity<Map<String, Object>> getInterestHistory(
            @RequestParam UUID userId,
            @RequestParam(defaultValue = "30") int limit) {
        
        try {
            List<SmartEarnInterestHistory> interestHistory = smartEarnService.getInterestHistory(userId, limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("interestHistory", interestHistory);
            response.put("count", interestHistory.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting SmartEarn interest history: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get interest history: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Calculate processing fee for a given amount
     */
    @GetMapping("/calculate-fee")
    public ResponseEntity<Map<String, Object>> calculateProcessingFee(@RequestParam BigDecimal amount) {
        try {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Amount must be greater than zero");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Create a temporary account to calculate fee
            SmartEarnAccount tempAccount = new SmartEarnAccount();
            BigDecimal processingFee = tempAccount.calculateProcessingFee(amount);
            BigDecimal netAmount = amount.subtract(processingFee);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("amount", amount);
            response.put("processingFee", processingFee);
            response.put("netAmount", netAmount);
            response.put("feeRate", "1%");
            response.put("minFee", SmartEarnAccount.MIN_PROCESSING_FEE);
            response.put("maxFee", SmartEarnAccount.MAX_PROCESSING_FEE);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error calculating processing fee: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to calculate processing fee: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get SmartEarn product information
     */
    @GetMapping("/product-info")
    public ResponseEntity<Map<String, Object>> getProductInfo() {
        Map<String, Object> productInfo = new HashMap<>();
        productInfo.put("name", "SmartEarn");
        productInfo.put("description", "High-yield savings with daily interest and flexible withdrawals");
        productInfo.put("annualInterestRate", "21.05%");
        productInfo.put("dailyInterestRate", SmartEarnAccount.DAILY_INTEREST_RATE);
        productInfo.put("processingFeeRate", "1%");
        productInfo.put("minProcessingFee", SmartEarnAccount.MIN_PROCESSING_FEE);
        productInfo.put("maxProcessingFee", SmartEarnAccount.MAX_PROCESSING_FEE);
        productInfo.put("features", List.of(
            "No holding period",
            "No penalty fees",
            "Daily interest calculation",
            "Flexible deposits and withdrawals",
            "Same-day confirmation before 10:30 AM",
            "T+1 confirmation after 10:30 AM"
        ));
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("productInfo", productInfo);
        
        return ResponseEntity.ok(response);
    }
}
