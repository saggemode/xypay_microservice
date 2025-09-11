package com.xypay.xypay.controller;

import com.xypay.xypay.domain.Transaction;
import com.xypay.xypay.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;

    @PostMapping("/deposit")
    public ResponseEntity<Transaction> deposit(@RequestParam Long accountId, @RequestParam BigDecimal amount, @RequestParam String currency, @RequestParam String reference) {
        return ResponseEntity.ok(transactionService.deposit(accountId, amount, currency, reference));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Transaction> withdraw(@RequestParam Long accountId, @RequestParam BigDecimal amount, @RequestParam String currency, @RequestParam String reference) {
        return ResponseEntity.ok(transactionService.withdraw(accountId, amount, currency, reference));
    }

    @PostMapping("/transfer")
    public ResponseEntity<Transaction> transfer(@RequestParam Long fromAccountId, @RequestParam Long toAccountId, @RequestParam BigDecimal amount, @RequestParam String currency, @RequestParam String reference) {
        return ResponseEntity.ok(transactionService.transfer(fromAccountId, toAccountId, amount, currency, reference));
    }
}