package com.xypay.xypay.controller;

import com.xypay.xypay.domain.Transaction;
import com.xypay.xypay.service.ReconciliationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/reconciliation")
public class ReconciliationController {
    @Autowired
    private ReconciliationService reconciliationService;

    @PostMapping("/match")
    public ResponseEntity<Void> matchTransactions(@RequestBody List<Transaction> bankTx, @RequestBody List<Transaction> ledgerTx) {
        reconciliationService.matchTransactions(bankTx, ledgerTx);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unmatched")
    public ResponseEntity<Set<Transaction>> getUnmatched() {
        return ResponseEntity.ok(reconciliationService.getUnmatched());
    }
}