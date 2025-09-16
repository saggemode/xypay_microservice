package com.xypay.xypay.controller;

import com.xypay.xypay.domain.SpendAndSaveAccount;
import com.xypay.xypay.domain.SpendAndSaveTransaction;
import com.xypay.xypay.domain.Transaction;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.dto.ApiResponse;
import com.xypay.xypay.dto.SpendAndSaveTransactionDTO;
import com.xypay.xypay.service.SpendAndSaveService;
import com.xypay.xypay.repository.SpendAndSaveAccountRepository;
import com.xypay.xypay.repository.SpendAndSaveTransactionRepository;
import com.xypay.xypay.repository.TransactionRepository;
import com.xypay.xypay.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for viewing Spend and Save transactions
 */
@RestController
@RequestMapping("/api/spend-and-save/transactions")
@Slf4j
public class SpendAndSaveTransactionController {
    
    @Autowired
    private SpendAndSaveService spendAndSaveService;
    
    @Autowired
    private SpendAndSaveAccountRepository spendAndSaveAccountRepository;
    
    @Autowired
    private SpendAndSaveTransactionRepository spendAndSaveTransactionRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get all transactions for the current user's Spend and Save account
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SpendAndSaveTransactionDTO>>> getTransactions(Authentication authentication) {
        try {
            User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Optional<SpendAndSaveAccount> accountOpt = spendAndSaveAccountRepository.findByUser(user);
            if (accountOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            List<SpendAndSaveTransaction> transactions = spendAndSaveTransactionRepository
                .findBySpendAndSaveAccount(accountOpt.get());
            
            List<SpendAndSaveTransactionDTO> transactionDTOs = transactions.stream()
                .map(SpendAndSaveTransactionDTO::new)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(transactionDTOs));
            
        } catch (Exception e) {
            log.error("Error getting transactions: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error getting transactions: " + e.getMessage()));
        }
    }
    
    /**
     * Get a specific transaction by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SpendAndSaveTransactionDTO>> getTransaction(
            @PathVariable UUID id,
            Authentication authentication) {
        try {
            User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Optional<SpendAndSaveTransaction> transactionOpt = spendAndSaveTransactionRepository.findById(id);
            if (transactionOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            SpendAndSaveTransaction transaction = transactionOpt.get();
            
            // Verify the transaction belongs to the user
            if (!transaction.getSpendAndSaveAccount().getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).build();
            }
            
            SpendAndSaveTransactionDTO transactionDTO = new SpendAndSaveTransactionDTO(transaction);
            return ResponseEntity.ok(ApiResponse.success(transactionDTO));
            
        } catch (Exception e) {
            log.error("Error getting transaction: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error getting transaction: " + e.getMessage()));
        }
    }
    
    /**
     * Process a spending transaction for auto-save
     */
    @PostMapping("/process-spending")
    public ResponseEntity<ApiResponse<SpendAndSaveTransactionDTO>> processSpending(
            @Valid @RequestBody ProcessSpendingTransactionRequestDTO request,
            Authentication authentication) {
        try {
            User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Optional<Transaction> transactionOpt = transactionRepository.findById(request.getTransactionId());
            if (transactionOpt.isEmpty()) {
                return ResponseEntity.status(404)
                    .body(ApiResponse.error("Transaction not found"));
            }
            
            Transaction transaction = transactionOpt.get();
            
            // Verify the transaction belongs to the user
            if (!transaction.getWallet().getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("Transaction does not belong to user"));
            }
            
            // Verify it's a debit transaction
            if (!"debit".equals(transaction.getType())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Only debit transactions can be processed for auto-save"));
            }
            
            SpendAndSaveTransaction autoSaveTx = spendAndSaveService.processSpendingTransaction(transaction);
            
            if (autoSaveTx == null) {
                return ResponseEntity.ok(ApiResponse.success(null, 
                    "No auto-save processed (account inactive or amount below threshold)"));
            }
            
            SpendAndSaveTransactionDTO transactionDTO = new SpendAndSaveTransactionDTO(autoSaveTx);
            return ResponseEntity.ok(ApiResponse.success(transactionDTO, "Auto-save processed successfully"));
            
        } catch (Exception e) {
            log.error("Error processing spending transaction: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error processing spending transaction: " + e.getMessage()));
        }
    }
    
    /**
     * Request DTO for processing spending transactions
     */
    @Data
    public static class ProcessSpendingTransactionRequestDTO {
        @NotNull(message = "Transaction ID is required")
        private UUID transactionId;
    }
}
