package com.xypay.xypay.controller;

import com.xypay.xypay.service.BalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/balance")
public class BalanceController {
    @Autowired
    private BalanceService balanceService;

    @GetMapping("/{accountId}")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long accountId) {
        return ResponseEntity.ok(balanceService.getBalance(accountId));
    }

    @PostMapping("/{accountId}")
    public ResponseEntity<Void> updateBalance(@PathVariable Long accountId, @RequestParam BigDecimal newBalance) {
        balanceService.updateBalance(accountId, newBalance);
        return ResponseEntity.ok().build();
    }
}