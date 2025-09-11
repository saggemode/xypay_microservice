package com.xypay.xypay.controller;

import com.xypay.xypay.domain.Transaction;
import com.xypay.xypay.service.AMLService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/aml")
public class AMLController {
    @Autowired
    private AMLService amlService;

    @PostMapping("/check")
    public ResponseEntity<Void> checkTransaction(@RequestBody Transaction tx) {
        amlService.checkTransaction(tx);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<String>> getAlerts() {
        return ResponseEntity.ok(amlService.getAlerts());
    }
}
