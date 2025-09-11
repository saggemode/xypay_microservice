package com.xypay.xypay.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FixedSavingsAccountCreateDTO {
    private BigDecimal amount;
    private String source;
    private String purpose;
    private String purposeDescription;
    private LocalDate startDate;
    private LocalDate paybackDate;
    private Boolean autoRenewalEnabled = false;

    // Getters and setters
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
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
}