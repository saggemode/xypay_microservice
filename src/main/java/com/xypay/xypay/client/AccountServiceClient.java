package com.xypay.xypay.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@FeignClient(name = "account-service")
public interface AccountServiceClient {
    
    @GetMapping("/api/accounts")
    List<Map<String, Object>> getAllAccounts();
    
    @GetMapping("/api/accounts/{accountNumber}")
    Map<String, Object> getAccountByNumber(@PathVariable("accountNumber") String accountNumber);
    
    @PostMapping("/api/accounts")
    Map<String, Object> createAccount(@RequestBody Map<String, Object> account);
    
    @PostMapping("/api/accounts/{id}/debit")
    Map<String, Object> debitAccount(@PathVariable("id") Long id, @RequestParam("amount") BigDecimal amount);
    
    @PostMapping("/api/accounts/{id}/credit")
    Map<String, Object> creditAccount(@PathVariable("id") Long id, @RequestParam("amount") BigDecimal amount);
    
    @GetMapping("/api/accounts/user/{userId}")
    List<Map<String, Object>> getAccountsByUserId(@PathVariable("userId") Long userId);
}
