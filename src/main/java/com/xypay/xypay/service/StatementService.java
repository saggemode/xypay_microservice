package com.xypay.xypay.service;

import com.xypay.xypay.scheduler.WeeklyStatementScheduler.StatementData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
// import org.xhtmlrenderer.pdf.ITextRenderer; // Commented out - using alternative approach

import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for generating PDF statements
 * Equivalent to Django's PDF generation functionality
 */
@Service
@Slf4j
public class StatementService {
    
    @Autowired
    private TemplateEngine templateEngine;
    
    /**
     * Generate PDF statement from statement data
     * Equivalent to Django's render_to_string + PDF conversion
     */
    public byte[] generateStatementPdf(StatementData statementData) {
        try {
            // Prepare template context
            Context context = new Context();
            context.setVariable("user", statementData.getUser());
            context.setVariable("wallet", statementData.getWallet());
            context.setVariable("transactions", statementData.getTransactions());
            context.setVariable("dateFrom", statementData.getDateFrom());
            context.setVariable("dateTo", statementData.getDateTo());
            context.setVariable("openingBalance", statementData.getOpeningBalance());
            context.setVariable("closingBalance", statementData.getClosingBalance());
            context.setVariable("totalCredits", statementData.getTotalCredits());
            context.setVariable("totalDebits", statementData.getTotalDebits());
            context.setVariable("now", statementData.getGeneratedAt());
            
            // Add additional formatting helpers
            context.setVariable("dateFormatter", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            context.setVariable("datetimeFormatter", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            context.setVariable("currencyFormatter", new CurrencyFormatter());
            
            // Generate HTML from template
            String htmlContent = templateEngine.process("bank/statement_pdf", context);
            
            // Convert HTML to PDF
            return convertHtmlToPdf(htmlContent);
            
        } catch (Exception e) {
            log.error("Error generating statement PDF: {}", e.getMessage());
            throw new RuntimeException("Failed to generate statement PDF", e);
        }
    }
    
    /**
     * Convert HTML content to PDF using alternative approach
     * For now, return HTML content as bytes (can be enhanced with proper PDF generation later)
     */
    private byte[] convertHtmlToPdf(String htmlContent) throws IOException {
        // TODO: Implement proper PDF generation using iText or similar library
        // For now, return HTML content as bytes
        log.warn("PDF generation not fully implemented - returning HTML content as bytes");
        return htmlContent.getBytes("UTF-8");
    }
    
    /**
     * Generate statement summary data
     */
    public Map<String, Object> generateStatementSummary(StatementData statementData) {
        Map<String, Object> summary = new HashMap<>();
        
        summary.put("account_number", statementData.getWallet().getAccountNumber());
        summary.put("account_holder", statementData.getUser().getFirstName() + " " + statementData.getUser().getLastName());
        summary.put("statement_period", statementData.getDateFrom() + " to " + statementData.getDateTo());
        summary.put("opening_balance", statementData.getOpeningBalance());
        summary.put("closing_balance", statementData.getClosingBalance());
        summary.put("total_credits", statementData.getTotalCredits());
        summary.put("total_debits", statementData.getTotalDebits());
        summary.put("net_movement", statementData.getTotalCredits().subtract(statementData.getTotalDebits()));
        summary.put("transaction_count", statementData.getTransactions().size());
        summary.put("generated_at", statementData.getGeneratedAt());
        
        return summary;
    }
    
    /**
     * Currency formatter helper class
     */
    public static class CurrencyFormatter {
        public String format(BigDecimal amount) {
            if (amount == null) {
                return "0.00";
            }
            return String.format("%,.2f", amount);
        }
        
        public String formatCurrency(BigDecimal amount, String currency) {
            return currency + " " + format(amount);
        }
    }
}
