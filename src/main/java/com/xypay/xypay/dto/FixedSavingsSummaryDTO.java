package com.xypay.xypay.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FixedSavingsSummaryDTO {
    
    private Integer totalActiveFixedSavings;
    private String totalActiveAmount;
    private String totalMaturityAmount;
    private String totalInterestEarned;
    private Integer maturedUnpaidCount;
    private BigDecimal maturedUnpaidAmount;
}