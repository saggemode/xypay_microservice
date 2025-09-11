package com.xypay.xypay.controller;

import com.xypay.xypay.dto.ApiResponse;
import com.xypay.xypay.service.NIBSSClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/nibss")
public class NIBSSController {
    
    @Autowired
    private NIBSSClient nibssClient;
    
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
     * Send interbank transfer endpoint
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