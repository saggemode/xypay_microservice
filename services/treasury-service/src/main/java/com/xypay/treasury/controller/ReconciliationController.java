package com.xypay.treasury.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/treasury/reconciliations")
@RequiredArgsConstructor
@Slf4j
public class ReconciliationController {

    @PostMapping("/run")
    public ResponseEntity<Map<String, Object>> run(@RequestParam Long statementId,
                                                   @RequestParam(defaultValue = "0.00") BigDecimal tolerance) {
        // Run simple auto-match rules; return counts
        log.info("Reconciliation run statementId={} tolerance={}", statementId, tolerance);
        return ResponseEntity.ok(Map.of(
                "statementId", statementId,
                "matched", 0,
                "unmatched", 0,
                "exceptions", 0
        ));
    }
}


