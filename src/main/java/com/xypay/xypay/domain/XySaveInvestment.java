package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "xy_save_investments")
public class XySaveInvestment extends BaseEntity {
    
    public enum InvestmentType {
        TREASURY_BILLS, MUTUAL_FUNDS, SHORT_TERM_PLACEMENTS, GOVERNMENT_BONDS
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "xysave_account_id")
    private XySaveAccount xySaveAccount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "investment_type", length = 25)
    private InvestmentType investmentType;
    
    @Column(name = "amount_invested", precision = 19, scale = 4)
    private BigDecimal amountInvested;
    
    @Column(name = "current_value", precision = 19, scale = 4)
    private BigDecimal currentValue;
    
    @Column(name = "expected_return_rate", precision = 5, scale = 2)
    private BigDecimal expectedReturnRate;
    
    @Column(name = "maturity_date")
    private LocalDate maturityDate;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public XySaveInvestment() {}
    
    // Getters and Setters
    public XySaveAccount getXySaveAccount() {
        return xySaveAccount;
    }
    
    public void setXySaveAccount(XySaveAccount xySaveAccount) {
        this.xySaveAccount = xySaveAccount;
    }
    
    public InvestmentType getInvestmentType() {
        return investmentType;
    }
    
    public void setInvestmentType(InvestmentType investmentType) {
        this.investmentType = investmentType;
    }
    
    public BigDecimal getAmountInvested() {
        return amountInvested;
    }
    
    public void setAmountInvested(BigDecimal amountInvested) {
        this.amountInvested = amountInvested;
    }
    
    public BigDecimal getCurrentValue() {
        return currentValue;
    }
    
    public void setCurrentValue(BigDecimal currentValue) {
        this.currentValue = currentValue;
    }
    
    public BigDecimal getExpectedReturnRate() {
        return expectedReturnRate;
    }
    
    public void setExpectedReturnRate(BigDecimal expectedReturnRate) {
        this.expectedReturnRate = expectedReturnRate;
    }
    
    public LocalDate getMaturityDate() {
        return maturityDate;
    }
    
    public void setMaturityDate(LocalDate maturityDate) {
        this.maturityDate = maturityDate;
    }
    
    public Boolean getActive() {
        return isActive;
    }
    
    public void setActive(Boolean active) {
        isActive = active;
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
    
    // Utility methods
    public BigDecimal getReturnPercentage() {
        if (amountInvested == null || amountInvested.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return currentValue.subtract(amountInvested)
                .multiply(new BigDecimal("100"))
                .divide(amountInvested, 2, java.math.RoundingMode.HALF_UP);
    }
}