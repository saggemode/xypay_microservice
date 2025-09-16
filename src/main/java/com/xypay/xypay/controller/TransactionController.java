package com.xypay.xypay.controller;

import com.xypay.xypay.domain.Transaction;
import com.xypay.xypay.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;

    @PostMapping("/deposit")
    public ResponseEntity<Transaction> deposit(@RequestParam Long accountId, @RequestParam BigDecimal amount, @RequestParam String currency, @RequestParam String reference) {
        // Convert Long to UUID - this is a workaround for the ID type mismatch
        UUID accountIdUuid = new UUID(0L, accountId); // Create UUID from Long
        return ResponseEntity.ok(transactionService.deposit(accountIdUuid, amount, currency, reference));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Transaction> withdraw(@RequestParam Long accountId, @RequestParam BigDecimal amount, @RequestParam String currency, @RequestParam String reference) {
        // Convert Long to UUID - this is a workaround for the ID type mismatch
        UUID accountIdUuid = new UUID(0L, accountId); // Create UUID from Long
        return ResponseEntity.ok(transactionService.withdraw(accountIdUuid, amount, currency, reference));
    }

    @PostMapping("/transfer")
    public ResponseEntity<Transaction> transfer(@RequestParam Long fromAccountId, @RequestParam Long toAccountId, @RequestParam BigDecimal amount, @RequestParam String currency, @RequestParam String reference) {
        // Convert Long to UUID - this is a workaround for the ID type mismatch
        UUID fromAccountIdUuid = new UUID(0L, fromAccountId); // Create UUID from Long
        UUID toAccountIdUuid = new UUID(0L, toAccountId); // Create UUID from Long
        return ResponseEntity.ok(transactionService.transfer(fromAccountIdUuid, toAccountIdUuid, amount, currency, reference));
    }
}