package com.xypay.xypay.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class XySaveAccountSummaryDTO {
    
    private XySaveAccountDTO account;
    private XySaveSettingsDTO settings;
    private BigDecimal dailyInterest;
    private BigDecimal annualInterestRate;
    private List<XySaveTransactionDTO> recentTransactions;
    private List<XySaveGoalDTO> activeGoals;
    private List<XySaveInvestmentDTO> investments;
    private BigDecimal totalInvested;
    private BigDecimal totalInvestmentValue;
}
