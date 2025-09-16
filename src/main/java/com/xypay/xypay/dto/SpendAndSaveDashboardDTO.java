package com.xypay.xypay.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SpendAndSaveDashboardDTO {
    private SpendAndSaveAccountSummaryDTO accountSummary;
    private InterestForecastDTO interestForecast;
    private Map<String, Object> tieredRatesInfo;
    private List<SpendAndSaveTransactionDTO> recentActivity;
    private Map<String, Object> savingsProgress;
}
