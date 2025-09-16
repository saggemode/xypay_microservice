package com.xypay.xypay.dto;

import com.xypay.xypay.domain.XySaveInvestment;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class XySaveInvestmentDTO {
    
    private UUID id;
    private String investmentType;
    private String investmentTypeDisplay;
    private BigDecimal amountInvested;
    private BigDecimal currentValue;
    private BigDecimal expectedReturnRate;
    private BigDecimal returnPercentage;
    private LocalDate maturityDate;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public XySaveInvestmentDTO() {}
    
    public XySaveInvestmentDTO(XySaveInvestment investment) {
        this.id = investment.getId();
        this.investmentType = investment.getInvestmentType().getCode();
        this.investmentTypeDisplay = investment.getInvestmentType().getDescription();
        this.amountInvested = investment.getAmountInvested();
        this.currentValue = investment.getCurrentValue();
        this.expectedReturnRate = investment.getExpectedReturnRate();
        this.returnPercentage = investment.getReturnPercentage();
        this.maturityDate = investment.getMaturityDate();
        this.isActive = investment.getIsActive();
        this.createdAt = investment.getCreatedAt();
        this.updatedAt = investment.getUpdatedAt();
    }
}
