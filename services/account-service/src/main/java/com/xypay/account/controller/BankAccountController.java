package com.xypay.account.controller;

import com.xypay.account.service.BankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bank-accounts")
@CrossOrigin(origins = "*")
public class BankAccountController {
    
    @Autowired
    private BankAccountService bankAccountService;

    @GetMapping("/search")
    public ResponseEntity<?> searchBanksByAccountNumber(@RequestParam String accountNumber) {
        try {
            List<Map<String, Object>> banks = bankAccountService.searchBanksByAccountNumber(accountNumber);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "banks", banks,
                "count", banks.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyAccountWithNibss(
            @RequestParam String bankCode, 
            @RequestParam String accountNumber) {
        try {
            Map<String, Object> result = bankAccountService.verifyAccountWithNibss(bankCode, accountNumber);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/banks")
    public ResponseEntity<?> getAllBanks() {
        try {
            List<Map<String, Object>> banks = bankAccountService.getAllBanks();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "banks", banks,
                "count", banks.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}
