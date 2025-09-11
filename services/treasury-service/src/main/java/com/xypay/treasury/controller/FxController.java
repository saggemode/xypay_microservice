package com.xypay.treasury.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/treasury/fx")
@RequiredArgsConstructor
public class FxController {

    @GetMapping("/quote")
    public ResponseEntity<Map<String, Object>> quote(@RequestParam String base,
                                                     @RequestParam String quote,
                                                     @RequestParam BigDecimal notional) {
        BigDecimal rate = new BigDecimal("1500.0000");
        return ResponseEntity.ok(Map.of(
                "base", base,
                "quote", quote,
                "notional", notional,
                "rate", rate,
                "timestamp", java.time.LocalDateTime.now()
        ));
    }

    @PostMapping("/deal")
    public ResponseEntity<Map<String, Object>> deal(@RequestParam String base,
                                                    @RequestParam String quote,
                                                    @RequestParam BigDecimal notional,
                                                    @RequestParam BigDecimal rate,
                                                    @RequestParam(defaultValue = "SPOT") String type,
                                                    @RequestParam(required = false) LocalDate valueDate) {
        return ResponseEntity.ok(Map.of(
                "dealId", java.util.UUID.randomUUID().toString(),
                "status", "BOOKED"
        ));
    }
}


