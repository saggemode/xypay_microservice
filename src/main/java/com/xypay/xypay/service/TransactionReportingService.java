package com.xypay.xypay.service;

import com.xypay.xypay.domain.Transaction;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TransactionReportingService {
    
    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter ISO_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    
    /**
     * Generate ISO 20022 formatted transaction report
     * 
     * @param transactions List of transactions to include in the report
     * @return ISO 20022 formatted XML string
     */
    public String generateISO20022Report(List<Transaction> transactions) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.001.001.03\">\n");
        xml.append("  <CstmrCdtTrfInitn>\n");
        xml.append("    <GrpHdr>\n");
        xml.append("      <MsgId>REPORT-").append(System.currentTimeMillis()).append("</MsgId>\n");
        xml.append("      <CreDtTm>").append(java.time.LocalDateTime.now().format(ISO_DATETIME_FORMATTER)).append("</CreDtTm>\n");
        xml.append("      <NbOfTxs>").append(transactions.size()).append("</NbOfTxs>\n");
        xml.append("      <CtrlSum>").append(calculateControlSum(transactions)).append("</CtrlSum>\n");
        xml.append("    </GrpHdr>\n");
        
        for (Transaction transaction : transactions) {
            xml.append("    <PmtInf>\n");
            xml.append("      <PmtInfId>").append(transaction.getId()).append("</PmtInfId>\n");
            xml.append("      <PmtMtd>TRF</PmtMtd>\n");
            xml.append("      <NbOfTxs>1</NbOfTxs>\n");
            xml.append("      <CtrlSum>").append(transaction.getAmount()).append("</CtrlSum>\n");
            xml.append("      <PmtTpInf>\n");
            xml.append("        <SvcLvl>\n");
            xml.append("          <Cd>SEPA</Cd>\n");
            xml.append("        </SvcLvl>\n");
            xml.append("      </PmtTpInf>\n");
            xml.append("      <ReqdExctnDt>").append(transaction.getCreatedAt().format(ISO_DATE_FORMATTER)).append("</ReqdExctnDt>\n");
            xml.append("      <Dbtr>\n");
            xml.append("        <Nm>Debtor Name</Nm>\n");
            xml.append("      </Dbtr>\n");
            xml.append("      <DbtrAcct>\n");
            xml.append("        <Id>\n");
            xml.append("          <IBAN>DEBTOR-ACCOUNT</IBAN>\n");
            xml.append("        </Id>\n");
            xml.append("      </DbtrAcct>\n");
            xml.append("      <Cdtr>\n");
            xml.append("        <Nm>Creditor Name</Nm>\n");
            xml.append("      </Cdtr>\n");
            xml.append("      <CdtrAcct>\n");
            xml.append("        <Id>\n");
            xml.append("          <IBAN>CREDITOR-ACCOUNT</IBAN>\n");
            xml.append("        </Id>\n");
            xml.append("      </CdtrAcct>\n");
            xml.append("      <Amt>\n");
            xml.append("        <InstdAmt Ccy=\"").append(transaction.getCurrency()).append("\">")
               .append(transaction.getAmount()).append("</InstdAmt>\n");
            xml.append("      </Amt>\n");
            xml.append("    </PmtInf>\n");
        }
        
        xml.append("  </CstmrCdtTrfInitn>\n");
        xml.append("</Document>");
        
        return xml.toString();
    }
    
    private String calculateControlSum(List<Transaction> transactions) {
        return transactions.stream()
                .map(Transaction::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
                .toString();
    }
}