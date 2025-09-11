package com.xypay.account.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@FeignClient(name = "transaction-service", url = "http://localhost:8084")
public interface TransactionServiceClient {
    
    // Account balance operations for Transaction Service to use
    @PostMapping("/api/transactions/accounts/{accountNumber}/debit")
    ResponseEntity<Map<String, Object>> debitAccount(
        @PathVariable String accountNumber, 
        @RequestBody Map<String, Object> request
    );
    
    @PostMapping("/api/transactions/accounts/{accountNumber}/credit")
    ResponseEntity<Map<String, Object>> creditAccount(
        @PathVariable String accountNumber, 
        @RequestBody Map<String, Object> request
    );
    
    // Account validation for transactions
    @GetMapping("/api/transactions/accounts/{accountNumber}/validate")
    ResponseEntity<Map<String, Object>> validateAccountForTransaction(@PathVariable String accountNumber);
    
    // Account balance inquiry
    @GetMapping("/api/transactions/accounts/{accountNumber}/balance")
    ResponseEntity<Map<String, Object>> getAccountBalance(@PathVariable String accountNumber);
    
    // Account limits check
    @PostMapping("/api/transactions/accounts/{accountNumber}/check-limits")
    ResponseEntity<Map<String, Object>> checkTransactionLimits(
        @PathVariable String accountNumber,
        @RequestBody Map<String, Object> request
    );
}
