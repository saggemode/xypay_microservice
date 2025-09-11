package com.xypay.xypay.dto;

import java.math.BigDecimal;

public class FixedSavingsSummaryDTO {
    private Integer totalActiveFixedSavings;
    private String totalActiveAmount;
    private String totalMaturityAmount;
    private String totalInterestEarned;
    private Integer maturedUnpaidCount;
    private BigDecimal maturedUnpaidAmount;

    // Getters and setters
    public Integer getTotalActiveFixedSavings() {
        return totalActiveFixedSavings;
    }

    public void setTotalActiveFixedSavings(Integer totalActiveFixedSavings) {
        this.totalActiveFixedSavings = totalActiveFixedSavings;
    }

    public String getTotalActiveAmount() {
        return totalActiveAmount;
    }

    public void setTotalActiveAmount(String totalActiveAmount) {
        this.totalActiveAmount = totalActiveAmount;
    }

    public String getTotalMaturityAmount() {
        return totalMaturityAmount;
    }

    public void setTotalMaturityAmount(String totalMaturityAmount) {
        this.totalMaturityAmount = totalMaturityAmount;
    }

    public String getTotalInterestEarned() {
        return totalInterestEarned;
    }

    public void setTotalInterestEarned(String totalInterestEarned) {
        this.totalInterestEarned = totalInterestEarned;
    }

    public Integer getMaturedUnpaidCount() {
        return maturedUnpaidCount;
    }

    public void setMaturedUnpaidCount(Integer maturedUnpaidCount) {
        this.maturedUnpaidCount = maturedUnpaidCount;
    }

    public BigDecimal getMaturedUnpaidAmount() {
        return maturedUnpaidAmount;
    }

    public void setMaturedUnpaidAmount(BigDecimal maturedUnpaidAmount) {
        this.maturedUnpaidAmount = maturedUnpaidAmount;
    }
}