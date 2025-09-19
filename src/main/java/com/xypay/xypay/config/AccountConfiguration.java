package com.xypay.xypay.config;

import com.xypay.xypay.domain.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "account_configurations")
public class AccountConfiguration extends BaseEntity {
    
    @Column(name = "configuration_name")
    private String configurationName;
    
    @Column(name = "account_type")
    private String accountType; // SAVINGS, CURRENT, FIXED_DEPOSIT, RECURRING_DEPOSIT
    
    @Column(name = "account_number_format")
    private String accountNumberFormat; // Format pattern for account numbers
    
    @Column(name = "account_number_prefix")
    private String accountNumberPrefix; // Prefix for account numbers
    
    @Column(name = "minimum_balance")
    private BigDecimal minimumBalance;
    
    @Column(name = "currency")
    private String currency;
    
    @Column(name = "daily_withdrawal_limit")
    private BigDecimal dailyWithdrawalLimit;
    
    @Column(name = "weekly_withdrawal_limit")
    private BigDecimal weeklyWithdrawalLimit;
    
    @Column(name = "monthly_withdrawal_limit")
    private BigDecimal monthlyWithdrawalLimit;
    
    @Column(name = "mandatory_fields")
    private String mandatoryFields; // JSON format list of mandatory fields
    
    
    @Column(name = "is_active")
    private Boolean isActive = true;

    // Getters and Setters

    public String getConfigurationName() {
        return configurationName;
    }

    public void setConfigurationName(String configurationName) {
        this.configurationName = configurationName;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getAccountNumberFormat() {
        return accountNumberFormat;
    }

    public void setAccountNumberFormat(String accountNumberFormat) {
        this.accountNumberFormat = accountNumberFormat;
    }

    public String getAccountNumberPrefix() {
        return accountNumberPrefix;
    }

    public void setAccountNumberPrefix(String accountNumberPrefix) {
        this.accountNumberPrefix = accountNumberPrefix;
    }

    public BigDecimal getMinimumBalance() {
        return minimumBalance;
    }

    public void setMinimumBalance(BigDecimal minimumBalance) {
        this.minimumBalance = minimumBalance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getDailyWithdrawalLimit() {
        return dailyWithdrawalLimit;
    }

    public void setDailyWithdrawalLimit(BigDecimal dailyWithdrawalLimit) {
        this.dailyWithdrawalLimit = dailyWithdrawalLimit;
    }

    public BigDecimal getWeeklyWithdrawalLimit() {
        return weeklyWithdrawalLimit;
    }

    public void setWeeklyWithdrawalLimit(BigDecimal weeklyWithdrawalLimit) {
        this.weeklyWithdrawalLimit = weeklyWithdrawalLimit;
    }

    public BigDecimal getMonthlyWithdrawalLimit() {
        return monthlyWithdrawalLimit;
    }

    public void setMonthlyWithdrawalLimit(BigDecimal monthlyWithdrawalLimit) {
        this.monthlyWithdrawalLimit = monthlyWithdrawalLimit;
    }

    public String getMandatoryFields() {
        return mandatoryFields;
    }

    public void setMandatoryFields(String mandatoryFields) {
        this.mandatoryFields = mandatoryFields;
    }


    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}