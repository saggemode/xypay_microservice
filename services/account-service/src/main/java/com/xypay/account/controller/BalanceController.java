package com.xypay.account.controller;

import com.xypay.account.service.BalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/balance")
@CrossOrigin(origins = "*")
public class BalanceController {
    
    @Autowired
    private BalanceService balanceService;

    @GetMapping("/{accountId}")
    public ResponseEntity<?> getBalance(@PathVariable UUID accountId) {
        try {
            BigDecimal balance = balanceService.getBalance(accountId);
            return ResponseEntity.ok(Map.of(
                "accountId", accountId,
                "balance", balance,
                "currency", "NGN" // Default currency
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{accountId}")
    public ResponseEntity<?> updateBalance(
            @PathVariable UUID accountId, 
            @RequestParam BigDecimal newBalance) {
        try {
            balanceService.updateBalance(accountId, newBalance);
            return ResponseEntity.ok(Map.of("message", "Balance updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{accountId}/history")
    public ResponseEntity<?> getBalanceHistory(@PathVariable UUID accountId) {
        try {
            var history = balanceService.getBalanceHistory(accountId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
