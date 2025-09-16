package com.xypay.xypay.dto;

import lombok.Data;

import java.util.List;

@Data
public class XySaveDashboardDTO {
    
    private XySaveAccountSummaryDTO accountSummary;
    private XySaveInterestForecastDTO interestForecast;
    private List<XySaveTransactionDTO> recentActivity;
    private List<XySaveGoalDTO> goalsProgress;
    private List<XySaveInvestmentDTO> investmentPortfolio;
}
