package com.xypay.xypay.controller;

import com.xypay.xypay.service.AuditTrailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditTrailController {
    @Autowired
    private AuditTrailService auditTrailService;

    @PostMapping("/log")
    public ResponseEntity<Void> logEvent(@RequestParam String event, @RequestParam String details) {
        auditTrailService.logEvent(event, details);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/logs")
    public ResponseEntity<List<AuditTrailService.AuditLog>> getLogs() {
        return ResponseEntity.ok(auditTrailService.getLogs());
    }
}
