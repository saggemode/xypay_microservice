package com.xypay.xypay.controller;

import com.xypay.xypay.domain.Transaction;
import com.xypay.xypay.service.ISO20022Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/iso20022")
public class ISO20022Controller {
    @Autowired
    private ISO20022Service iso20022Service;

    @PostMapping(value = "/export", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> export(@RequestBody List<Transaction> txs) {
        return ResponseEntity.ok(iso20022Service.exportTransactionsAsISO20022(txs));
    }
}
