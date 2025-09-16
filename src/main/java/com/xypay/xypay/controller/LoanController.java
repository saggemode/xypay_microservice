package com.xypay.xypay.controller;

import com.xypay.xypay.domain.Loan;
import com.xypay.xypay.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/loans")
public class LoanController {
    @Autowired
    private LoanService loanService;

    @PostMapping("/originate")
    public ResponseEntity<Loan> originateLoan(@RequestParam Long customerId, @RequestParam Long productId, @RequestParam BigDecimal amount) {
        // Convert Long to UUID - this is a workaround for the ID type mismatch
        UUID customerIdUuid = new UUID(0L, customerId); // Create UUID from Long
        UUID productIdUuid = new UUID(0L, productId); // Create UUID from Long
        return ResponseEntity.ok(loanService.originateLoan(customerIdUuid, productIdUuid, amount));
    }

    @GetMapping("/{loanId}")
    public ResponseEntity<Loan> getLoan(@PathVariable Long loanId) {
        // Convert Long to UUID - this is a workaround for the ID type mismatch
        UUID loanIdUuid = new UUID(0L, loanId); // Create UUID from Long
        return ResponseEntity.ok(loanService.getLoan(loanIdUuid));
    }
}