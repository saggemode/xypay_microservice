package com.xypay.xypay.controller;

import com.xypay.xypay.service.GeneralLedgerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/ledger")
public class GeneralLedgerController {
    @Autowired
    private GeneralLedgerService ledgerService;

    @PostMapping("/post")
    public ResponseEntity<Void> postEntry(@RequestParam Long debitAccountId, @RequestParam Long creditAccountId, @RequestParam BigDecimal amount, @RequestParam String description) {
        ledgerService.postEntry(debitAccountId, creditAccountId, amount, description);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<GeneralLedgerService.LedgerEntry>> getLedgerEntries() {
        return ResponseEntity.ok(ledgerService.getLedgerEntries());
    }
}
