package com.xypay.account.controller;

import com.xypay.account.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@CrossOrigin(origins = "*")
public class AccountController {
    
    @Autowired
    private AccountService accountService;

    @PostMapping("/open")
    public ResponseEntity<?> openAccount(
            @RequestParam UUID customerId, 
            @RequestParam String currency, 
            @RequestParam String accountType) {
        try {
            var account = accountService.openAccount(customerId, currency, accountType);
            return ResponseEntity.ok(account);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{accountId}/close")
    public ResponseEntity<?> closeAccount(@PathVariable UUID accountId) {
        try {
            String result = accountService.closeAccount(accountId);
            return ResponseEntity.ok(Map.of("message", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{accountId}/status")
    public ResponseEntity<?> getAccountStatus(@PathVariable UUID accountId) {
        try {
            String status = accountService.getAccountStatus(accountId);
            return ResponseEntity.ok(Map.of("status", status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<?> getAccount(@PathVariable UUID accountId) {
        try {
            var account = accountService.getAccount(accountId);
            if (account != null) {
                return ResponseEntity.ok(account);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getAccountsByCustomer(@PathVariable UUID customerId) {
        try {
            var accounts = accountService.getAccountsByCustomer(customerId);
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
            "status", "healthy",
            "service", "Account Service",
            "timestamp", System.currentTimeMillis()
        ));
    }
}
