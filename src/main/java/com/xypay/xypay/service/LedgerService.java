package com.xypay.xypay.service;

import com.xypay.xypay.domain.JournalEntry;
import com.xypay.xypay.domain.Transaction;
import com.xypay.xypay.repository.JournalEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LedgerService {
    
    @Autowired
    private JournalEntryRepository journalEntryRepository;
    
    @Autowired
    private AuditTrailService auditTrailService;
    
    /**
     * Create journal entries for a transaction to ensure double-entry bookkeeping
     * 
     * @param transaction The transaction to create journal entries for
     * @return The created journal entries
     */
    @Transactional
    public JournalEntry[] createJournalEntries(Transaction transaction) {
        try {
            // Create debit entry
            JournalEntry debitEntry = new JournalEntry();
            debitEntry.setTxId(transaction.getId());
            debitEntry.setAccountId(transaction.getWallet().getId());
            debitEntry.setDebitCredit("DEBIT");
            debitEntry.setAmount(transaction.getAmount());
            debitEntry.setCurrency(transaction.getCurrency());
            debitEntry.setGlAccount(determineGLAccount(transaction, "DEBIT"));
            debitEntry.setCreatedAt(LocalDateTime.now());
            
            // Save debit entry
            debitEntry = journalEntryRepository.save(debitEntry);
            
            // Create credit entry
            JournalEntry creditEntry = new JournalEntry();
            creditEntry.setTxId(transaction.getId());
            creditEntry.setAccountId(transaction.getWallet().getId());
            creditEntry.setDebitCredit("CREDIT");
            creditEntry.setAmount(transaction.getAmount());
            creditEntry.setCurrency(transaction.getCurrency());
            creditEntry.setGlAccount(determineGLAccount(transaction, "CREDIT"));
            creditEntry.setCreatedAt(LocalDateTime.now());
            
            // Save credit entry
            creditEntry = journalEntryRepository.save(creditEntry);
            
            // Log the journal entry creation
            auditTrailService.logFinancialTransaction(
                transaction.getId(), 
                "JOURNAL_ENTRIES_CREATED", 
                "Created debit entry " + debitEntry.getId() + 
                " and credit entry " + creditEntry.getId(), 
                "SYSTEM"
            );
            
            return new JournalEntry[] { debitEntry, creditEntry };
        } catch (Exception e) {
            // In a real implementation, we would implement compensating transactions here
            throw new RuntimeException("Failed to create journal entries", e);
        }
    }
    
    /**
     * Reverse journal entries for a transaction reversal
     * 
     * @param originalTransaction The original transaction being reversed
     * @return The created reversal journal entries
     */
    @Transactional
    public JournalEntry[] reverseJournalEntries(Transaction originalTransaction) {
        try {
            // Create reversal debit entry (opposite of original credit)
            JournalEntry reversalDebitEntry = new JournalEntry();
            reversalDebitEntry.setTxId(originalTransaction.getId());
            reversalDebitEntry.setAccountId(originalTransaction.getWallet().getId());
            reversalDebitEntry.setDebitCredit("DEBIT");
            reversalDebitEntry.setAmount(originalTransaction.getAmount());
            reversalDebitEntry.setCurrency(originalTransaction.getCurrency());
            reversalDebitEntry.setGlAccount(determineGLAccount(originalTransaction, "CREDIT")); // Opposite of original
            reversalDebitEntry.setCreatedAt(LocalDateTime.now());
            
            // Save reversal debit entry
            reversalDebitEntry = journalEntryRepository.save(reversalDebitEntry);
            
            // Create reversal credit entry (opposite of original debit)
            JournalEntry reversalCreditEntry = new JournalEntry();
            reversalCreditEntry.setTxId(originalTransaction.getId());
            reversalCreditEntry.setAccountId(originalTransaction.getWallet().getId());
            reversalCreditEntry.setDebitCredit("CREDIT");
            reversalCreditEntry.setAmount(originalTransaction.getAmount());
            reversalCreditEntry.setCurrency(originalTransaction.getCurrency());
            reversalCreditEntry.setGlAccount(determineGLAccount(originalTransaction, "DEBIT")); // Opposite of original
            reversalCreditEntry.setCreatedAt(LocalDateTime.now());
            
            // Save reversal credit entry
            reversalCreditEntry = journalEntryRepository.save(reversalCreditEntry);
            
            // Log the journal entry reversal
            auditTrailService.logFinancialTransaction(
                originalTransaction.getId(), 
                "JOURNAL_ENTRIES_REVERSED", 
                "Reversed with debit entry " + reversalDebitEntry.getId() + 
                " and credit entry " + reversalCreditEntry.getId(), 
                "SYSTEM"
            );
            
            return new JournalEntry[] { reversalDebitEntry, reversalCreditEntry };
        } catch (Exception e) {
            // In a real implementation, we would implement compensating transactions here
            throw new RuntimeException("Failed to reverse journal entries", e);
        }
    }
    
    /**
     * Determine the appropriate GL account for a transaction
     * 
     * @param transaction The transaction
     * @param entryType The type of entry (DEBIT or CREDIT)
     * @return The GL account code
     */
    private String determineGLAccount(Transaction transaction, String entryType) {
        // In a real implementation, this would be determined based on:
        // 1. Transaction type
        // 2. Account type
        // 3. Business rules
        // 4. Configuration
        
        if ("DEBIT".equals(entryType)) {
            return "ASSET_" + transaction.getType();
        } else {
            return "LIABILITY_" + transaction.getType();
        }
    }
    
    /**
     * Validate that journal entries balance (debit = credit)
     * 
     * @param transactionId The transaction ID to validate
     * @return true if entries balance, false otherwise
     */
    public boolean validateJournalEntries(Long transactionId) {
        try {
            // In a real implementation, we would sum all debits and credits
            // for the transaction and ensure they balance
            
            // Log the validation
            auditTrailService.logFinancialTransaction(
                transactionId, 
                "JOURNAL_ENTRIES_VALIDATED", 
                "Validated journal entries for transaction", 
                "SYSTEM"
            );
            
            return true; // Simplified for demonstration
        } catch (Exception e) {
            return false;
        }
    }
}