package com.xypay.xypay.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class FixedSavingsAccountDetailDTO {
    private Long id;
    private String user;
    private Long userId;
    private String accountNumber;
    private String amount;
    private String source;
    private String sourceDisplay;
    private String purpose;
    private String purposeDisplay;
    private String purposeDescription;
    private LocalDate startDate;
    private LocalDate paybackDate;
    private Boolean autoRenewalEnabled;
    private Boolean isActive;
    private Boolean isMatured;
    private Boolean isPaidOut;
    private BigDecimal interestRate;
    private String totalInterestEarned;
    private String maturityAmount;
    private Integer durationDays;
    private Integer daysRemaining;
    private Boolean isMature;
    private Boolean canBePaidOut;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime maturedAt;
    private LocalDateTime paidOutAt;
    private List<FixedSavingsTransactionDTO> transactions;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceDisplay() {
        return sourceDisplay;
    }

    public void setSourceDisplay(String sourceDisplay) {
        this.sourceDisplay = sourceDisplay;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getPurposeDisplay() {
        return purposeDisplay;
    }

    public void setPurposeDisplay(String purposeDisplay) {
        this.purposeDisplay = purposeDisplay;
    }

    public String getPurposeDescription() {
        return purposeDescription;
    }

    public void setPurposeDescription(String purposeDescription) {
        this.purposeDescription = purposeDescription;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getPaybackDate() {
        return paybackDate;
    }

    public void setPaybackDate(LocalDate paybackDate) {
        this.paybackDate = paybackDate;
    }

    public Boolean getAutoRenewalEnabled() {
        return autoRenewalEnabled;
    }

    public void setAutoRenewalEnabled(Boolean autoRenewalEnabled) {
        this.autoRenewalEnabled = autoRenewalEnabled;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsMatured() {
        return isMatured;
    }

    public void setIsMatured(Boolean isMatured) {
        this.isMatured = isMatured;
    }

    public Boolean getIsPaidOut() {
        return isPaidOut;
    }

    public void setIsPaidOut(Boolean isPaidOut) {
        this.isPaidOut = isPaidOut;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public String getTotalInterestEarned() {
        return totalInterestEarned;
    }

    public void setTotalInterestEarned(String totalInterestEarned) {
        this.totalInterestEarned = totalInterestEarned;
    }

    public String getMaturityAmount() {
        return maturityAmount;
    }

    public void setMaturityAmount(String maturityAmount) {
        this.maturityAmount = maturityAmount;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public Integer getDaysRemaining() {
        return daysRemaining;
    }

    public void setDaysRemaining(Integer daysRemaining) {
        this.daysRemaining = daysRemaining;
    }

    public Boolean getIsMature() {
        return isMature;
    }

    public void setIsMature(Boolean isMature) {
        this.isMature = isMature;
    }

    public Boolean getCanBePaidOut() {
        return canBePaidOut;
    }

    public void setCanBePaidOut(Boolean canBePaidOut) {
        this.canBePaidOut = canBePaidOut;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getMaturedAt() {
        return maturedAt;
    }

    public void setMaturedAt(LocalDateTime maturedAt) {
        this.maturedAt = maturedAt;
    }

    public LocalDateTime getPaidOutAt() {
        return paidOutAt;
    }

    public void setPaidOutAt(LocalDateTime paidOutAt) {
        this.paidOutAt = paidOutAt;
    }

    public List<FixedSavingsTransactionDTO> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<FixedSavingsTransactionDTO> transactions) {
        this.transactions = transactions;
    }
}