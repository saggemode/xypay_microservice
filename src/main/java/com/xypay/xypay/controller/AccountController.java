package com.xypay.xypay.controller;

import com.xypay.xypay.domain.Account;
import com.xypay.xypay.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    @Autowired
    private AccountService accountService;

    @PostMapping("/open")
    public ResponseEntity<Account> openAccount(@RequestParam Long customerId, @RequestParam String currency, @RequestParam String accountType) {
        return ResponseEntity.ok(accountService.openAccount(customerId, currency, accountType));
    }

    @PostMapping("/{accountId}/close")
    public ResponseEntity<String> closeAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(accountService.closeAccount(accountId));
    }

    @GetMapping("/{accountId}/status")
    public ResponseEntity<String> getAccountStatus(@PathVariable Long accountId) {
        return ResponseEntity.ok(accountService.getAccountStatus(accountId));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<Account> getAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(accountService.getAccount(accountId));
    }
}