package com.xypay.xypay.service;

import com.xypay.xypay.domain.Transaction;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;

@Service
public class AMLService {
    private final List<String> alerts = new ArrayList<>();
    private static final BigDecimal THRESHOLD = new BigDecimal("10000.00");

    public void checkTransaction(Transaction tx) {
        if (tx.getAmount() != null && tx.getAmount().compareTo(THRESHOLD) > 0) {
            alerts.add("Suspicious transaction: " + tx.getId() + " Amount: " + tx.getAmount());
        }
    }
    public List<String> getAlerts() {
        return alerts;
    }
}