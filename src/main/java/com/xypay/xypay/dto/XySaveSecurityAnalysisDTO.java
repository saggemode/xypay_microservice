package com.xypay.xypay.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class XySaveSecurityAnalysisDTO {
    
    private List<TransactionSecurityAnalysis> securityAnalysis;
    private int totalTransactionsAnalyzed;
    private LocalDateTime analysisTimestamp;
    
    @Data
    public static class TransactionSecurityAnalysis {
        private UUID transactionId;
        private String reference;
        private BigDecimal amount;
        private String riskLevel;
        private List<String> riskFactors;
        private LocalDateTime createdAt;
    }
}
