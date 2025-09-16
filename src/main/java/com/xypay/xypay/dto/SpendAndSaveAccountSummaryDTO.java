package com.xypay.xypay.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SpendAndSaveAccountSummaryDTO {
    private SpendAndSaveAccountDTO account;
    private SpendAndSaveSettingsDTO settings;
    private Map<String, Object> interestBreakdown;
    private List<SpendAndSaveTransactionDTO> recentTransactions;
}
