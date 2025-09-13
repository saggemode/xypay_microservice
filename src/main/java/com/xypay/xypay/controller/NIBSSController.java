package com.xypay.xypay.controller;

import com.xypay.xypay.dto.ApiResponse;
import com.xypay.xypay.service.NIBSSClient;
import com.xypay.xypay.service.BankTransferProcessingService;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.domain.BankTransfer;
import com.xypay.xypay.repository.UserRepository;
import com.xypay.xypay.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/nibss")
public class NIBSSController {
    
    private static final Logger logger = LoggerFactory.getLogger(NIBSSController.class);
    
    @Autowired
    private NIBSSClient nibssClient;
    
    @Autowired
    private BankTransferProcessingService bankTransferProcessingService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private WalletRepository walletRepository;
    
    /**
     * Validate account number endpoint
     */
    @GetMapping("/validate-account/{accountNumber}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateAccount(
            @PathVariable String accountNumber) {
        Map<String, Object> result = nibssClient.validateAccountNumber(accountNumber);
        boolean success = (Boolean) result.get("valid");
        String message = success ? "Account validated successfully" : (String) result.get("error");
        return ResponseEntity.ok(new ApiResponse<>(success, message, result));
    }
    
    /**
     * Send interbank transfer endpoint (legacy - for backward compatibility)
     */
    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendTransfer(
            @RequestParam String senderAccount,
            @RequestParam String recipientBankCode,
            @RequestParam String recipientAccount,
            @RequestParam double amount,
            @RequestParam String narration) {
        Map<String, Object> result = nibssClient.sendInterbankTransfer(
                senderAccount, recipientBankCode, recipientAccount, amount, narration);
        boolean success = "success".equals(result.get("status"));
        String message = (String) result.get("message");
        return ResponseEntity.ok(new ApiResponse<>(success, message, result));
    }
    
    /**
     * Send interbank transfer with internal record creation
     */
    @PostMapping("/transfer-with-record")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendTransferWithRecord(
            @RequestParam String senderAccount,
            @RequestParam String recipientBankCode,
            @RequestParam String recipientAccount,
            @RequestParam double amount,
            @RequestParam String narration,
            @RequestParam Long userId) {
        
        logger.info("Processing NIBSS transfer with record: {} -> {} (amount: {})", senderAccount, recipientAccount, amount);
        
        try {
            // First, call NIBSS to process the transfer
            Map<String, Object> nibssResult = nibssClient.sendInterbankTransfer(
                    senderAccount, recipientBankCode, recipientAccount, amount, narration);
            
            boolean nibssSuccess = "success".equals(nibssResult.get("status"));
            
            if (nibssSuccess) {
                // If NIBSS transfer is successful, create internal BankTransfer record
                Optional<User> userOpt = userRepository.findById(userId);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    
                    // Get bank name from bank code
                    String bankName = getBankNameFromCode(recipientBankCode);
                    
                    // Create and process the bank transfer through our internal system
                    BankTransfer bankTransfer = bankTransferProcessingService.createAndProcessTransfer(
                            user,
                            bankName,
                            recipientBankCode,
                            recipientAccount,
                            BigDecimal.valueOf(amount),
                            narration
                    );
                    
                    // Add the bank transfer ID to the result
                    nibssResult.put("bankTransferId", bankTransfer.getId());
                    nibssResult.put("internalReference", bankTransfer.getReference());
                    
                    logger.info("Created internal BankTransfer record: {} for NIBSS transfer", bankTransfer.getId());
                } else {
                    logger.warn("User not found for ID: {} - skipping internal transfer record creation", userId);
                    nibssResult.put("error", "User not found");
                }
            } else {
                logger.error("NIBSS transfer failed: {}", nibssResult.get("message"));
            }
            
            boolean success = nibssSuccess;
            String message = (String) nibssResult.get("message");
            return ResponseEntity.ok(new ApiResponse<>(success, message, nibssResult));
            
        } catch (Exception e) {
            logger.error("Error processing NIBSS transfer: {}", e.getMessage(), e);
            Map<String, Object> errorResult = Map.of(
                "status", "failed",
                "message", "Internal error: " + e.getMessage()
            );
            return ResponseEntity.ok(new ApiResponse<>(false, "Transfer failed", errorResult));
        }
    }
    
    /**
     * Helper method to get bank name from bank code
     * In production, this should be a proper service with a database lookup
     */
    private String getBankNameFromCode(String bankCode) {
        // Simple mapping - in production, use a proper bank service
        Map<String, String> bankMapping = Map.of(
            "044", "Access Bank",
            "058", "GT Bank", 
            "011", "First Bank",
            "057", "Zenith Bank",
            "033", "UBA",
            "050", "Ecobank",
            "032", "Union Bank",
            "070", "Fidelity Bank",
            "232", "Sterling Bank",
            "035", "Wema Bank"
        );
        return bankMapping.getOrDefault(bankCode, "Unknown Bank (" + bankCode + ")");
    }
    
    /**
     * Check transfer status endpoint
     */
    @GetMapping("/transfer-status/{nibssReference}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkTransferStatus(
            @PathVariable String nibssReference) {
        Map<String, Object> result = nibssClient.checkTransferStatus(nibssReference);
        boolean success = "success".equals(result.get("status"));
        String message = (String) result.get("message");
        return ResponseEntity.ok(new ApiResponse<>(success, message, result));
    }
    
    /**
     * Pay bill endpoint
     */
    @PostMapping("/pay-bill")
    public ResponseEntity<ApiResponse<Map<String, Object>>> payBill(
            @RequestParam String customerAccount,
            @RequestParam String billerCode,
            @RequestParam double amount,
            @RequestParam(required = false) String billReference,
            @RequestParam String narration) {
        Map<String, Object> result = nibssClient.payBill(
                customerAccount, billerCode, amount, billReference, narration);
        boolean success = "success".equals(result.get("status"));
        String message = (String) result.get("message");
        return ResponseEntity.ok(new ApiResponse<>(success, message, result));
    }
    
    /**
     * Verify BVN endpoint
     */
    @GetMapping("/verify-bvn/{bvn}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyBVN(
            @PathVariable String bvn) {
        Map<String, Object> result = nibssClient.verifyBVN(bvn);
        boolean success = "success".equals(result.get("status"));
        String message = (String) result.get("message");
        return ResponseEntity.ok(new ApiResponse<>(success, message, result));
    }
    
    /**
     * Setup direct debit endpoint
     */
    @PostMapping("/setup-direct-debit")
    public ResponseEntity<ApiResponse<Map<String, Object>>> setupDirectDebit(
            @RequestParam String customerAccount,
            @RequestParam double amount,
            @RequestParam(required = false) String mandateReference) {
        Map<String, Object> result = nibssClient.setupDirectDebit(
                customerAccount, amount, mandateReference);
        boolean success = "success".equals(result.get("status"));
        String message = (String) result.get("message");
        return ResponseEntity.ok(new ApiResponse<>(success, message, result));
    }
}