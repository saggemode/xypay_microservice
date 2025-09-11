package com.xypay.xypay.service;

import com.xypay.xypay.domain.Transaction;
import org.springframework.stereotype.Service;

@Service
public class IntegrationService {
    public String transformToSWIFT(Transaction tx) {
        return "SWIFT-MSG:TX-" + tx.getId();
    }
    public String transformToISO20022(Transaction tx) {
        return "<ISO20022><TxId>" + tx.getId() + "</TxId></ISO20022>";
    }
}