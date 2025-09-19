package com.xypay.xypay.controller.admin;

import com.xypay.xypay.domain.SmartEarnAccount;
import com.xypay.xypay.domain.SmartEarnTransaction;
import com.xypay.xypay.domain.SmartEarnInterestHistory;
import com.xypay.xypay.repository.SmartEarnAccountRepository;
import com.xypay.xypay.repository.SmartEarnTransactionRepository;
import com.xypay.xypay.repository.SmartEarnInterestHistoryRepository;
import com.xypay.xypay.scheduler.SmartEarnInterestScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/smartearn")
@CrossOrigin(origins = "*")
public class SmartEarnAdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(SmartEarnAdminController.class);
    
    @Autowired
    private SmartEarnAccountRepository smartEarnAccountRepository;
    
    @Autowired
    private SmartEarnTransactionRepository smartEarnTransactionRepository;
    
    @Autowired
    private SmartEarnInterestHistoryRepository smartEarnInterestHistoryRepository;
    
    @Autowired
    private SmartEarnInterestScheduler smartEarnInterestScheduler;
    
    /**
     * Get all SmartEarn accounts with pagination
     */
    @GetMapping("/accounts")
    public ResponseEntity<Map<String, Object>> getAllAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<SmartEarnAccount> accounts = smartEarnAccountRepository.findAll(pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("accounts", accounts.getContent());
            response.put("totalElements", accounts.getTotalElements());
            response.put("totalPages", accounts.getTotalPages());
            response.put("currentPage", page);
            response.put("size", size);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting SmartEarn accounts: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get accounts: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get SmartEarn account by ID
     */
    @GetMapping("/accounts/{accountId}")
    public ResponseEntity<Map<String, Object>> getAccountById(@PathVariable UUID accountId) {
        try {
            SmartEarnAccount account = smartEarnAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("account", account);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting SmartEarn account: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get account: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get all transactions with pagination
     */
    @GetMapping("/transactions")
    public ResponseEntity<Map<String, Object>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "transactionTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<SmartEarnTransaction> transactions;
            
            if (status != null && type != null) {
                transactions = smartEarnTransactionRepository.findByStatusAndTransactionType(
                    SmartEarnTransaction.TransactionStatus.valueOf(status.toUpperCase()),
                    SmartEarnTransaction.TransactionType.valueOf(type.toUpperCase()),
                    pageable);
            } else if (status != null) {
                transactions = smartEarnTransactionRepository.findByStatus(
                    SmartEarnTransaction.TransactionStatus.valueOf(status.toUpperCase()),
                    pageable);
            } else if (type != null) {
                transactions = smartEarnTransactionRepository.findByTransactionType(
                    SmartEarnTransaction.TransactionType.valueOf(type.toUpperCase()),
                    pageable);
            } else {
                transactions = smartEarnTransactionRepository.findAll(pageable);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("transactions", transactions.getContent());
            response.put("totalElements", transactions.getTotalElements());
            response.put("totalPages", transactions.getTotalPages());
            response.put("currentPage", page);
            response.put("size", size);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting SmartEarn transactions: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get transactions: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get interest history with pagination
     */
    @GetMapping("/interest-history")
    public ResponseEntity<Map<String, Object>> getInterestHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "interestDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<SmartEarnInterestHistory> interestHistory;
            
            if (startDate != null && endDate != null) {
                interestHistory = smartEarnInterestHistoryRepository.findByInterestDateBetween(
                    startDate, endDate, pageable);
            } else {
                interestHistory = smartEarnInterestHistoryRepository.findAll(pageable);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("interestHistory", interestHistory.getContent());
            response.put("totalElements", interestHistory.getTotalElements());
            response.put("totalPages", interestHistory.getTotalPages());
            response.put("currentPage", page);
            response.put("size", size);
            
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
     * Get SmartEarn dashboard statistics
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        try {
            // Total accounts
            long totalAccounts = smartEarnAccountRepository.countByIsActiveTrue();
            
            // Total balance
            BigDecimal totalBalance = smartEarnAccountRepository.getTotalBalance();
            
            // Total interest earned
            BigDecimal totalInterestEarned = smartEarnAccountRepository.getTotalInterestEarned();
            
            // Total transactions today
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
            long transactionsToday = smartEarnTransactionRepository.countByTransactionTimeBetween(
                startOfDay, endOfDay);
            
            // Pending transactions
            long pendingTransactions = smartEarnTransactionRepository.countByStatus(
                SmartEarnTransaction.TransactionStatus.PENDING);
            
            // Total processing fees collected
            BigDecimal totalProcessingFees = smartEarnTransactionRepository.getTotalProcessingFees(null);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalAccounts", totalAccounts);
            stats.put("totalBalance", totalBalance);
            stats.put("totalInterestEarned", totalInterestEarned);
            stats.put("transactionsToday", transactionsToday);
            stats.put("pendingTransactions", pendingTransactions);
            stats.put("totalProcessingFees", totalProcessingFees);
            stats.put("averageBalance", totalAccounts > 0 ? totalBalance.divide(new BigDecimal(totalAccounts), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting SmartEarn dashboard stats: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get dashboard stats: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Manually trigger interest calculation
     */
    @PostMapping("/calculate-interest")
    public ResponseEntity<Map<String, Object>> calculateInterest() {
        try {
            smartEarnInterestScheduler.manualInterestCalculation();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Interest calculation triggered successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error triggering interest calculation: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to trigger interest calculation: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Manually trigger interest crediting
     */
    @PostMapping("/credit-interest")
    public ResponseEntity<Map<String, Object>> creditInterest() {
        try {
            smartEarnInterestScheduler.manualInterestCrediting();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Interest crediting triggered successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error triggering interest crediting: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to trigger interest crediting: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Deactivate SmartEarn account
     */
    @PostMapping("/accounts/{accountId}/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateAccount(@PathVariable UUID accountId) {
        try {
            SmartEarnAccount account = smartEarnAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
            
            account.setIsActive(false);
            smartEarnAccountRepository.save(account);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Account deactivated successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error deactivating SmartEarn account: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to deactivate account: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Activate SmartEarn account
     */
    @PostMapping("/accounts/{accountId}/activate")
    public ResponseEntity<Map<String, Object>> activateAccount(@PathVariable UUID accountId) {
        try {
            SmartEarnAccount account = smartEarnAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
            
            account.setIsActive(true);
            smartEarnAccountRepository.save(account);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Account activated successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error activating SmartEarn account: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to activate account: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
}
