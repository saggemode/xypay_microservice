package com.xypay.xypay.controller;

import com.xypay.xypay.domain.FixedSavingsAccount;
import com.xypay.xypay.domain.FixedSavingsTransaction;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.dto.ApiResponse;
import com.xypay.xypay.dto.FixedSavingsTransactionDTO;
import com.xypay.xypay.repository.FixedSavingsAccountRepository;
import com.xypay.xypay.repository.FixedSavingsTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fixed-savings/transactions")
@PreAuthorize("hasRole('USER')")
public class FixedSavingsTransactionController {
    
    @Autowired
    private FixedSavingsTransactionRepository fixedSavingsTransactionRepository;
    
    @Autowired
    private FixedSavingsAccountRepository fixedSavingsAccountRepository;
    
    /**
     * Get all transactions for user's fixed savings accounts
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<FixedSavingsTransactionDTO>>> getUserFixedSavingsTransactions(
            Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            List<FixedSavingsTransaction> transactions = fixedSavingsTransactionRepository
                .findByFixedSavingsAccountUser(currentUser);
            
            List<FixedSavingsTransactionDTO> transactionDTOs = transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(transactionDTOs));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve fixed savings transactions"));
        }
    }
    
    /**
     * Get transactions by fixed savings account
     */
    @GetMapping("/by-account")
    public ResponseEntity<ApiResponse<List<FixedSavingsTransactionDTO>>> getTransactionsByAccount(
            @RequestParam UUID accountId, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            
            // Verify the account belongs to the user
            FixedSavingsAccount account = fixedSavingsAccountRepository.findByIdAndUser(accountId, currentUser);
            if (account == null) {
                return ResponseEntity.notFound().build();
            }
            
            List<FixedSavingsTransaction> transactions = fixedSavingsTransactionRepository
                .findByFixedSavingsAccount(account);
            
            List<FixedSavingsTransactionDTO> transactionDTOs = transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(transactionDTOs));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve transactions for account"));
        }
    }
    
    /**
     * Get transactions by type
     */
    @GetMapping("/by-type")
    public ResponseEntity<ApiResponse<List<FixedSavingsTransactionDTO>>> getTransactionsByType(
            @RequestParam String type, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            FixedSavingsTransaction.TransactionType transactionType = 
                FixedSavingsTransaction.TransactionType.valueOf(type.toUpperCase());
            
            List<FixedSavingsTransaction> transactions = fixedSavingsTransactionRepository
                .findByFixedSavingsAccountUserAndTransactionType(currentUser, transactionType);
            
            List<FixedSavingsTransactionDTO> transactionDTOs = transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(transactionDTOs));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve transactions by type"));
        }
    }
    
    /**
     * Get recent transactions
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<FixedSavingsTransactionDTO>>> getRecentTransactions(
            @RequestParam(defaultValue = "10") int limit, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            List<FixedSavingsTransaction> transactions = fixedSavingsTransactionRepository
                .findByFixedSavingsAccountUserOrderByCreatedAtDesc(currentUser)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
            
            List<FixedSavingsTransactionDTO> transactionDTOs = transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(transactionDTOs));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve recent transactions"));
        }
    }
    
    /**
     * Get a specific transaction detail
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FixedSavingsTransactionDTO>> getTransactionDetail(
            @PathVariable UUID id, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            FixedSavingsTransaction transaction = fixedSavingsTransactionRepository.findById(id).orElse(null);
            
            if (transaction == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Verify the transaction belongs to the user
            if (!transaction.getFixedSavingsAccount().getUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied"));
            }
            
            FixedSavingsTransactionDTO transactionDTO = convertToDTO(transaction);
            return ResponseEntity.ok(ApiResponse.success(transactionDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve transaction detail"));
        }
    }
    
    private User getCurrentUser(Authentication authentication) {
        // Implementation to get current user from authentication
        // This would depend on your security configuration
        return new User(); // Placeholder
    }
    
    private FixedSavingsTransactionDTO convertToDTO(FixedSavingsTransaction transaction) {
        FixedSavingsTransactionDTO dto = new FixedSavingsTransactionDTO();
        dto.setId(transaction.getId());
        dto.setFixedSavingsAccountId(transaction.getFixedSavingsAccount().getId());
        dto.setTransactionType(transaction.getTransactionType().name());
        dto.setAmount("₦" + transaction.getAmount().toPlainString());
        dto.setBalanceBefore("₦" + transaction.getBalanceBefore().toPlainString());
        dto.setBalanceAfter("₦" + transaction.getBalanceAfter().toPlainString());
        dto.setReference(transaction.getReference());
        dto.setDescription(transaction.getDescription());
        dto.setInterestEarned("₦" + transaction.getInterestEarned().toPlainString());
        dto.setInterestRateApplied(transaction.getInterestRateApplied());
        dto.setSourceAccount(transaction.getSourceAccount() != null ? transaction.getSourceAccount().name() : null);
        dto.setSourceTransactionId(transaction.getSourceTransactionId());
        dto.setMetadata(transaction.getMetadata());
        dto.setCreatedAt(transaction.getCreatedAt());
        return dto;
    }
}