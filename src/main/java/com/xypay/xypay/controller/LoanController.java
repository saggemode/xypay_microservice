package com.xypay.xypay.controller;

import com.xypay.xypay.domain.Loan;
import com.xypay.xypay.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/loans")
public class LoanController {
    @Autowired
    private LoanService loanService;

    @PostMapping("/originate")
    public ResponseEntity<Loan> originateLoan(@RequestParam Long customerId, @RequestParam Long productId, @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(loanService.originateLoan(customerId, productId, amount));
    }

    @GetMapping("/{loanId}")
    public ResponseEntity<Loan> getLoan(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.getLoan(loanId));
    }
}