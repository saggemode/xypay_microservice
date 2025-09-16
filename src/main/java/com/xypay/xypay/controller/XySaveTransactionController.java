package com.xypay.xypay.controller;

import com.xypay.xypay.domain.User;
import com.xypay.xypay.domain.XySaveTransaction;
import com.xypay.xypay.dto.XySaveTransactionDTO;
import com.xypay.xypay.repository.XySaveTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/xysave/transactions")
public class XySaveTransactionController {
    
    private static final Logger logger = LoggerFactory.getLogger(XySaveTransactionController.class);
    
    @Autowired
    private XySaveTransactionRepository xySaveTransactionRepository;
    
    /**
     * Get XySave transactions for current user
     */
    @GetMapping
    public ResponseEntity<Page<XySaveTransactionDTO>> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            Pageable pageable = PageRequest.of(page, size);
            
            Page<XySaveTransaction> transactions = xySaveTransactionRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
            
            Page<XySaveTransactionDTO> transactionDTOs = transactions.map(XySaveTransactionDTO::new);
            return ResponseEntity.ok(transactionDTOs);
        } catch (Exception e) {
            logger.error("Error getting transactions: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Get transactions filtered by type
     */
    @GetMapping("/by-type")
    public ResponseEntity<Page<XySaveTransactionDTO>> getTransactionsByType(
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            Pageable pageable = PageRequest.of(page, size);
            
            Page<XySaveTransaction> transactions;
            if (type != null && !type.isEmpty()) {
                transactions = xySaveTransactionRepository
                    .findByUserIdAndTransactionTypeOrderByCreatedAtDesc(user.getId(), type, pageable);
            } else {
                transactions = xySaveTransactionRepository
                    .findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
            }
            
            Page<XySaveTransactionDTO> transactionDTOs = transactions.map(XySaveTransactionDTO::new);
            return ResponseEntity.ok(transactionDTOs);
        } catch (Exception e) {
            logger.error("Error getting transactions by type: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Get transaction by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<XySaveTransactionDTO> getTransaction(@PathVariable String id, 
                                                              Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            
            // Note: You might want to add a method to verify ownership
            var transaction = xySaveTransactionRepository.findById(java.util.UUID.fromString(id));
            
            if (transaction.isPresent()) {
                return ResponseEntity.ok(new XySaveTransactionDTO(transaction.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error getting transaction by ID: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Get recent transactions (last 10)
     */
    @GetMapping("/recent")
    public ResponseEntity<List<XySaveTransactionDTO>> getRecentTransactions(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            
            List<XySaveTransaction> transactions = xySaveTransactionRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .limit(10)
                .toList();
            
            List<XySaveTransactionDTO> transactionDTOs = transactions.stream()
                .map(XySaveTransactionDTO::new)
                .toList();
            
            return ResponseEntity.ok(transactionDTOs);
        } catch (Exception e) {
            logger.error("Error getting recent transactions: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Get transaction statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getTransactionStatistics(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            
            List<XySaveTransaction> allTransactions = xySaveTransactionRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId());
            
            // Calculate statistics
            long totalTransactions = allTransactions.size();
            long depositCount = allTransactions.stream()
                .filter(t -> t.getTransactionType() == XySaveTransaction.TransactionType.DEPOSIT)
                .count();
            long withdrawalCount = allTransactions.stream()
                .filter(t -> t.getTransactionType() == XySaveTransaction.TransactionType.WITHDRAWAL)
                .count();
            long interestCount = allTransactions.stream()
                .filter(t -> t.getTransactionType() == XySaveTransaction.TransactionType.INTEREST_CREDIT)
                .count();
            
            java.math.BigDecimal totalDeposits = allTransactions.stream()
                .filter(t -> t.getTransactionType() == XySaveTransaction.TransactionType.DEPOSIT)
                .map(XySaveTransaction::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            
            java.math.BigDecimal totalWithdrawals = allTransactions.stream()
                .filter(t -> t.getTransactionType() == XySaveTransaction.TransactionType.WITHDRAWAL)
                .map(XySaveTransaction::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            
            java.math.BigDecimal totalInterest = allTransactions.stream()
                .filter(t -> t.getTransactionType() == XySaveTransaction.TransactionType.INTEREST_CREDIT)
                .map(XySaveTransaction::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            
            Map<String, Object> statistics = Map.of(
                "total_transactions", totalTransactions,
                "deposit_count", depositCount,
                "withdrawal_count", withdrawalCount,
                "interest_count", interestCount,
                "total_deposits", totalDeposits,
                "total_withdrawals", totalWithdrawals,
                "total_interest", totalInterest,
                "net_balance", totalDeposits.add(totalInterest).subtract(totalWithdrawals)
            );
            
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Error getting transaction statistics: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}
