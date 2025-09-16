package com.xypay.xypay.controller;

import com.xypay.xypay.domain.BankTransfer;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.dto.ApiResponse;
import com.xypay.xypay.service.BankTransferProcessingService;
import com.xypay.xypay.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Controller for handling bank transfers with proper transaction and notification recording
 */
@RestController
@RequestMapping("/api/v1/bank-transfers")
public class BankTransferController {
    
    private static final Logger logger = LoggerFactory.getLogger(BankTransferController.class);
    
    @Autowired
    private BankTransferProcessingService bankTransferProcessingService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Create and process a bank transfer
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<BankTransfer>> createBankTransfer(
            @RequestParam Long userId,
            @RequestParam String bankName,
            @RequestParam String bankCode,
            @RequestParam String accountNumber,
            @RequestParam BigDecimal amount,
            @RequestParam String description) {
        
        logger.info("Creating bank transfer for user {}: {} -> {} (amount: {})", 
            userId, bankName, accountNumber, amount);
        
        try {
            // Convert Long to UUID - this is a workaround for the ID type mismatch
            UUID userIdUuid = new UUID(0L, userId); // Create UUID from Long
            // Find the user
            Optional<User> userOpt = userRepository.findById(userIdUuid);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "User not found", null));
            }
            
            User user = userOpt.get();
            
            // Create and process the bank transfer
            BankTransfer bankTransfer = bankTransferProcessingService.createAndProcessTransfer(
                    user,
                    bankName,
                    bankCode,
                    accountNumber,
                    amount,
                    description
            );
            
            logger.info("Successfully created bank transfer: {} for user: {}", 
                bankTransfer.getId(), userId);
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Bank transfer created successfully", bankTransfer));
            
        } catch (Exception e) {
            logger.error("Error creating bank transfer for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(new ApiResponse<>(false, "Failed to create bank transfer: " + e.getMessage(), null));
        }
    }
    
    /**
     * Create a bank transfer with JSON request body
     */
    @PostMapping("/create-json")
    public ResponseEntity<ApiResponse<BankTransfer>> createBankTransferJson(@RequestBody BankTransferRequest request) {
        
        logger.info("Creating bank transfer for user {}: {} -> {} (amount: {})", 
            request.getUserId(), request.getBankName(), request.getAccountNumber(), request.getAmount());
        
        try {
            // Convert Long to UUID - this is a workaround for the ID type mismatch
            UUID userIdUuid = new UUID(0L, request.getUserId()); // Create UUID from Long
            // Find the user
            Optional<User> userOpt = userRepository.findById(userIdUuid);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "User not found", null));
            }
            
            User user = userOpt.get();
            
            // Create and process the bank transfer
            BankTransfer bankTransfer = bankTransferProcessingService.createAndProcessTransfer(
                    user,
                    request.getBankName(),
                    request.getBankCode(),
                    request.getAccountNumber(),
                    request.getAmount(),
                    request.getDescription()
            );
            
            logger.info("Successfully created bank transfer: {} for user: {}", 
                bankTransfer.getId(), request.getUserId());
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Bank transfer created successfully", bankTransfer));
            
        } catch (Exception e) {
            logger.error("Error creating bank transfer for user {}: {}", request.getUserId(), e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(new ApiResponse<>(false, "Failed to create bank transfer: " + e.getMessage(), null));
        }
    }
    
    /**
     * Request DTO for bank transfer creation
     */
    public static class BankTransferRequest {
        private Long userId;
        private String bankName;
        private String bankCode;
        private String accountNumber;
        private BigDecimal amount;
        private String description;
        
        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getBankName() { return bankName; }
        public void setBankName(String bankName) { this.bankName = bankName; }
        
        public String getBankCode() { return bankCode; }
        public void setBankCode(String bankCode) { this.bankCode = bankCode; }
        
        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
