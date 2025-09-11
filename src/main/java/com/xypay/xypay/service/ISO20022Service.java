package com.xypay.xypay.service;

import com.xypay.xypay.domain.Transaction;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ISO20022Service {
    public String exportTransactionsAsISO20022(List<Transaction> txs) {
        StringBuilder sb = new StringBuilder();
        sb.append("<ISO20022Report>\n");
        for (Transaction tx : txs) {
            sb.append("  <Transaction>\n");
            sb.append("    <Id>" + tx.getId() + "</Id>\n");
            sb.append("    <Amount>" + tx.getAmount() + "</Amount>\n");
            sb.append("    <Currency>" + tx.getCurrency() + "</Currency>\n");
            sb.append("    <Reference>" + tx.getReference() + "</Reference>\n");
            sb.append("  </Transaction>\n");
        }
        sb.append("</ISO20022Report>");
        return sb.toString();
    }
}
