package com.xypay.transaction.service;

import com.xypay.transaction.client.CustomerServiceClient;
import com.xypay.transaction.dto.TransactionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComplianceService {
    
    private final CustomerServiceClient customerServiceClient;
    
    public ComplianceResult validateKYC(String customerId, String accountNumber) {
        log.info("Validating KYC for customer: {} and account: {}", customerId, accountNumber);
        
        try {
            // Check customer KYC status
            Map<String, Object> customerKYC = customerServiceClient.getCustomerKYCStatus(customerId);
            
            if (customerKYC == null) {
                return ComplianceResult.failed("Customer KYC not found");
            }
            
            String kycStatus = (String) customerKYC.get("status");
            String kycLevel = (String) customerKYC.get("level");
            
            if (!"VERIFIED".equals(kycStatus)) {
                return ComplianceResult.failed("Customer KYC not verified. Status: " + kycStatus);
            }
            
            // Check KYC level requirements
            if ("BASIC".equals(kycLevel)) {
                return ComplianceResult.warning("Customer has basic KYC level. Some transactions may be restricted.");
            }
            
            return ComplianceResult.passed("KYC validation successful");
            
        } catch (Exception e) {
            log.error("KYC validation failed: {}", e.getMessage(), e);
            return ComplianceResult.failed("KYC validation failed: " + e.getMessage());
        }
    }
    
    public ComplianceResult validateAML(TransactionRequest request) {
        log.info("Validating AML for transaction: {} amount: {}", request.getReference(), request.getAmount());
        
        try {
            // Check transaction amount thresholds
            if (request.getAmount().compareTo(new BigDecimal("500000")) > 0) {
                return ComplianceResult.warning("High-value transaction requires additional AML screening");
            }
            
            // Check for suspicious patterns
            if (isSuspiciousTransaction(request)) {
                return ComplianceResult.failed("Transaction flagged for suspicious activity");
            }
            
            // Check customer AML status
            Map<String, Object> customerAML = customerServiceClient.getCustomerAMLStatus(request.getCustomerId());
            
            if (customerAML != null) {
                String amlStatus = (String) customerAML.get("status");
                if ("BLOCKED".equals(amlStatus)) {
                    return ComplianceResult.failed("Customer is on AML watchlist");
                }
            }
            
            return ComplianceResult.passed("AML validation successful");
            
        } catch (Exception e) {
            log.error("AML validation failed: {}", e.getMessage(), e);
            return ComplianceResult.failed("AML validation failed: " + e.getMessage());
        }
    }
    
    private boolean isSuspiciousTransaction(TransactionRequest request) {
        // Check for suspicious patterns
        if (request.getAmount().compareTo(new BigDecimal("1000000")) > 0) {
            return true; // Very high amount
        }
        
        if (request.getChannel() != null && request.getChannel().isDigital()) {
            // Check for rapid successive transactions
            // This would require additional logic to check transaction history
            return false;
        }
        
        return false;
    }
    
    public ComplianceResult validateRegulatoryCompliance(TransactionRequest request) {
        log.info("Validating regulatory compliance for transaction: {}", request.getReference());
        
        try {
            // Check CBN regulations
            if (request.getAmount().compareTo(new BigDecimal("10000000")) > 0) {
                return ComplianceResult.warning("Transaction exceeds CBN reporting threshold");
            }
            
            // Check currency regulations
            if (!"NGN".equals(request.getCurrency())) {
                return ComplianceResult.warning("Foreign currency transaction requires additional compliance checks");
            }
            
            return ComplianceResult.passed("Regulatory compliance validation successful");
            
        } catch (Exception e) {
            log.error("Regulatory compliance validation failed: {}", e.getMessage(), e);
            return ComplianceResult.failed("Regulatory compliance validation failed: " + e.getMessage());
        }
    }
    
    public static class ComplianceResult {
        private final boolean passed;
        private final boolean warning;
        private final String message;
        private final String code;
        
        private ComplianceResult(boolean passed, boolean warning, String message, String code) {
            this.passed = passed;
            this.warning = warning;
            this.message = message;
            this.code = code;
        }
        
        public static ComplianceResult passed(String message) {
            return new ComplianceResult(true, false, message, "PASSED");
        }
        
        public static ComplianceResult warning(String message) {
            return new ComplianceResult(true, true, message, "WARNING");
        }
        
        public static ComplianceResult failed(String message) {
            return new ComplianceResult(false, false, message, "FAILED");
        }
        
        public boolean isPassed() { return passed; }
        public boolean isWarning() { return warning; }
        public boolean isFailed() { return !passed; }
        public String getMessage() { return message; }
        public String getCode() { return code; }
    }
}
