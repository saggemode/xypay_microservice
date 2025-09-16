package com.xypay.xypay.dto;

import com.xypay.xypay.domain.XySaveAccount;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class XySaveAccountDTO {
    
    private UUID id;
    private String accountNumber;
    private String accountNumberDisplay;
    private BigDecimal balance;
    private BigDecimal totalInterestEarned;
    private BigDecimal dailyInterestRate;
    private BigDecimal annualInterestRate;
    private BigDecimal dailyInterest;
    private Boolean isActive;
    private Boolean autoSaveEnabled;
    private BigDecimal autoSavePercentage;
    private BigDecimal autoSaveMinAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public XySaveAccountDTO() {}
    
    public XySaveAccountDTO(XySaveAccount account) {
        this.id = account.getId();
        this.accountNumber = account.getAccountNumber();
        this.accountNumberDisplay = formatAccountNumber(account.getAccountNumber());
        this.balance = account.getBalance();
        this.totalInterestEarned = account.getTotalInterestEarned();
        this.dailyInterestRate = account.getDailyInterestRate();
        this.annualInterestRate = account.getAnnualInterestRate();
        this.dailyInterest = account.calculateDailyInterest();
        this.isActive = account.getIsActive();
        this.autoSaveEnabled = account.getAutoSaveEnabled();
        this.autoSavePercentage = account.getAutoSavePercentage();
        this.autoSaveMinAmount = account.getAutoSaveMinAmount();
        this.createdAt = account.getCreatedAt();
        this.updatedAt = account.getUpdatedAt();
    }
    
    private String formatAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 12) {
            return accountNumber;
        }
        return "XS-" + accountNumber.substring(2, 6) + "-" + 
               accountNumber.substring(6, 10) + "-" + accountNumber.substring(10);
    }
}
