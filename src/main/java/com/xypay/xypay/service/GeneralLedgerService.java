package com.xypay.xypay.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;

@Service
public class GeneralLedgerService {
    public static class LedgerEntry {
        public Long debitAccountId;
        public Long creditAccountId;
        public BigDecimal amount;
        public String description;
        public Date timestamp;
    }
    private final List<LedgerEntry> entries = new ArrayList<>();

    public void postEntry(Long debitAccountId, Long creditAccountId, BigDecimal amount, String description) {
        LedgerEntry entry = new LedgerEntry();
        entry.debitAccountId = debitAccountId;
        entry.creditAccountId = creditAccountId;
        entry.amount = amount;
        entry.description = description;
        entry.timestamp = new Date();
        entries.add(entry);
    }
    public List<LedgerEntry> getLedgerEntries() {
        return entries;
    }
}
