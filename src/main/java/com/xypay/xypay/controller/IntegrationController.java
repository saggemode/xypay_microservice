package com.xypay.xypay.controller;

import com.xypay.xypay.domain.Transaction;
import com.xypay.xypay.service.IntegrationService;
import com.xypay.xypay.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/integration")
public class IntegrationController {
    
    @Autowired
    private IntegrationService integrationService;
    
    @Autowired
    private TransactionService transactionService;
    
    @GetMapping("/transactions/{transactionId}/swift")
    public ResponseEntity<String> getTransactionAsSWIFT(@PathVariable Long transactionId) {
        Transaction transaction = transactionService.getTransaction(transactionId);
        String swiftMessage = integrationService.transformToSWIFT(transaction);
        return ResponseEntity.ok(swiftMessage);
    }
    
    @GetMapping("/transactions/{transactionId}/iso20022")
    public ResponseEntity<String> getTransactionAsISO20022(@PathVariable Long transactionId) {
        Transaction transaction = transactionService.getTransaction(transactionId);
        String iso20022Message = integrationService.transformToISO20022(transaction);
        return ResponseEntity.ok(iso20022Message);
    }
}