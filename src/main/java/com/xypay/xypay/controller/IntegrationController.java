package com.xypay.xypay.controller;

import com.xypay.xypay.domain.Transaction;
import com.xypay.xypay.service.IntegrationService;
import com.xypay.xypay.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;



@RestController
@RequestMapping("/api/integration")
public class IntegrationController {
    
    @Autowired
    private IntegrationService integrationService;
    
    @Autowired
    private TransactionService transactionService;
    
    @GetMapping("/transactions/{transactionId}/swift")
    public ResponseEntity<String> getTransactionAsSWIFT(@PathVariable Long transactionId) {
        // Convert Long to UUID - this is a workaround for the ID type mismatch
        UUID transactionIdUuid = new UUID(0L, transactionId); // Create UUID from Long
        Transaction transaction = transactionService.getTransaction(transactionIdUuid);
        String swiftMessage = integrationService.transformToSWIFT(transaction);
        return ResponseEntity.ok(swiftMessage);
    }
    
    @GetMapping("/transactions/{transactionId}/iso20022")
    public ResponseEntity<String> getTransactionAsISO20022(@PathVariable Long transactionId) {
        // Convert Long to UUID - this is a workaround for the ID type mismatch
        UUID transactionIdUuid = new UUID(0L, transactionId); // Create UUID from Long
        Transaction transaction = transactionService.getTransaction(transactionIdUuid);
        String iso20022Message = integrationService.transformToISO20022(transaction);
        return ResponseEntity.ok(iso20022Message);
    }
}