package com.xypay.xypay.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "transaction-service")
public interface TransactionServiceClient {
    
    @GetMapping("/api/transactions")
    List<Map<String, Object>> getAllTransactions();
    
    @GetMapping("/api/transactions/{id}")
    Map<String, Object> getTransactionById(@PathVariable("id") Long id);
    
    @PostMapping("/api/transactions")
    Map<String, Object> createTransaction(@RequestBody Map<String, Object> transaction);
    
    @PutMapping("/api/transactions/{id}/status")
    Map<String, Object> updateTransactionStatus(@PathVariable("id") Long id, @RequestParam("status") String status);
    
    @GetMapping("/api/transactions/user/{userId}")
    List<Map<String, Object>> getTransactionsByUserId(@PathVariable("userId") Long userId);
    
    @GetMapping("/api/transactions/account/{accountId}")
    List<Map<String, Object>> getTransactionsByAccountId(@PathVariable("accountId") Long accountId);
}
