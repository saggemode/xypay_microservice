package com.xypay.transaction.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@FeignClient(name = "account-service", url = "http://localhost:8083")
public interface AccountServiceClient {
    
    @GetMapping("/api/accounts/{accountNumber}/balance")
    ResponseEntity<Map<String, Object>> getAccountBalance(@PathVariable String accountNumber);
    
    @PostMapping("/api/accounts/{accountNumber}/debit")
    ResponseEntity<Map<String, Object>> debitAccount(@PathVariable String accountNumber, @RequestBody DebitRequest request);
    
    @PostMapping("/api/accounts/{accountNumber}/credit")
    ResponseEntity<Map<String, Object>> creditAccount(@PathVariable String accountNumber, @RequestBody CreditRequest request);
    
    @PostMapping("/api/accounts/{accountNumber}/hold")
    ResponseEntity<Map<String, Object>> holdAmount(@PathVariable String accountNumber, @RequestBody HoldRequest request);
    
    @PostMapping("/api/accounts/{accountNumber}/release")
    ResponseEntity<Map<String, Object>> releaseAmount(@PathVariable String accountNumber, @RequestBody ReleaseRequest request);
    
    @GetMapping("/api/accounts/{accountNumber}/limits")
    ResponseEntity<Map<String, Object>> getAccountLimits(@PathVariable String accountNumber);
    
    @PostMapping("/api/accounts/{accountNumber}/validate-transaction")
    ResponseEntity<Map<String, Object>> validateTransaction(@PathVariable String accountNumber, @RequestBody TransactionValidationRequest request);
    
    class DebitRequest {
        public BigDecimal amount;
        public String reference;
        public String description;
        public String transactionType;
        
        public DebitRequest() {}
        
        public DebitRequest(BigDecimal amount, String reference, String description, String transactionType) {
            this.amount = amount;
            this.reference = reference;
            this.description = description;
            this.transactionType = transactionType;
        }
    }
    
    class CreditRequest {
        public BigDecimal amount;
        public String reference;
        public String description;
        public String transactionType;
        
        public CreditRequest() {}
        
        public CreditRequest(BigDecimal amount, String reference, String description, String transactionType) {
            this.amount = amount;
            this.reference = reference;
            this.description = description;
            this.transactionType = transactionType;
        }
    }
    
    class HoldRequest {
        public BigDecimal amount;
        public String reference;
        public String description;
        public String reason;
        
        public HoldRequest() {}
        
        public HoldRequest(BigDecimal amount, String reference, String description, String reason) {
            this.amount = amount;
            this.reference = reference;
            this.description = description;
            this.reason = reason;
        }
    }
    
    class ReleaseRequest {
        public BigDecimal amount;
        public String reference;
        public String description;
        public String reason;
        
        public ReleaseRequest() {}
        
        public ReleaseRequest(BigDecimal amount, String reference, String description, String reason) {
            this.amount = amount;
            this.reference = reference;
            this.description = description;
            this.reason = reason;
        }
    }
    
    class TransactionValidationRequest {
        public BigDecimal amount;
        public String transactionType;
        public String channel;
        public String currency;
        
        public TransactionValidationRequest() {}
        
        public TransactionValidationRequest(BigDecimal amount, String transactionType, String channel, String currency) {
            this.amount = amount;
            this.transactionType = transactionType;
            this.channel = channel;
            this.currency = currency;
        }
    }
}
