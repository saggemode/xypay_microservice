package com.xypay.treasury.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/treasury/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentsController {

    // In a complete impl, inject repositories/services; minimal scaffold for now

    @PostMapping
    @Transactional
    public ResponseEntity<Map<String, Object>> submit(@RequestHeader(value = "Idempotency-Key", required = false) String idemp,
                                                      @RequestBody PaymentRequest req) {
        String key = idemp != null ? idemp : UUID.randomUUID().toString();
        // Persist payment with status PENDING and idempotency key; emit PaymentRequested event
        log.info("PaymentRequested key={} amount={} {} to {}", key, req.amount, req.currency, req.creditorName);
        return ResponseEntity.accepted().body(Map.of(
                "idempotencyKey", key,
                "status", "PENDING",
                "createdAt", LocalDateTime.now()
        ));
    }

    @PostMapping("/{paymentId}/approve")
    public ResponseEntity<Map<String, Object>> approve(@PathVariable Long paymentId,
                                                       @RequestParam String actor) {
        // Record approval (CHECKER), transition status if maker/checker satisfied; emit PaymentSubmitted
        log.info("PaymentApproved id={} by {}", paymentId, actor);
        return ResponseEntity.ok(Map.of("paymentId", paymentId, "status", "APPROVED"));
    }

    @PostMapping("/{paymentId}/reject")
    public ResponseEntity<Map<String, Object>> reject(@PathVariable Long paymentId,
                                                      @RequestParam String actor,
                                                      @RequestParam(required = false) String reason) {
        log.info("PaymentRejected id={} by {} reason={}", paymentId, actor, reason);
        return ResponseEntity.ok(Map.of("paymentId", paymentId, "status", "REJECTED"));
    }

    @Data
    public static class PaymentRequest {
        public Long debtorAccountId;
        public String creditorAccount;
        public String creditorName;
        public BigDecimal amount;
        public String currency;
        public String scheme;
        public String purpose;
        public LocalDateTime scheduledFor;
    }
}


