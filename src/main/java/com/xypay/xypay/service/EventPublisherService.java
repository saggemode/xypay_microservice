package com.xypay.xypay.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.xypay.xypay.domain.Transaction;
import com.xypay.xypay.domain.JournalEntry;
import com.xypay.xypay.domain.AuditLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventPublisherService {
    
    @Autowired
    private ObjectMapper objectMapper;
    
    public void publishTransactionEvent(Transaction transaction) {
        // Kafka implementation removed for now
        // Will be added back later
        System.out.println("Transaction event published: " + transaction.getId());
    }
    
    public void publishJournalEvent(JournalEntry journalEntry) {
        // Kafka implementation removed for now
        // Will be added back later
        System.out.println("Journal event published: " + journalEntry.getId());
    }
    
    public void publishAuditEvent(AuditLog auditLog) {
        // Kafka implementation removed for now
        // Will be added back later
        System.out.println("Audit event published: " + auditLog.getId());
    }
}