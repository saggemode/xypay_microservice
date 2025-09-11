package com.xypay.xypay.service;

import com.xypay.xypay.domain.Transaction;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ReconciliationService {
    private final Set<Transaction> unmatched = new HashSet<>();

    public void matchTransactions(List<Transaction> bankTx, List<Transaction> ledgerTx) {
        Set<Transaction> bankSet = new HashSet<>(bankTx);
        Set<Transaction> ledgerSet = new HashSet<>(ledgerTx);
        unmatched.clear();
        for (Transaction t : bankSet) {
            if (!ledgerSet.contains(t)) unmatched.add(t);
        }
        for (Transaction t : ledgerSet) {
            if (!bankSet.contains(t)) unmatched.add(t);
        }
    }
    public Set<Transaction> getUnmatched() {
        return unmatched;
    }
}