package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "smartearn_interest_history")
public class SmartEarnInterestHistory extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "smartearn_account_id", nullable = false)
    private SmartEarnAccount smartEarnAccount;
    
    @Column(name = "interest_date", nullable = false)
    private LocalDate interestDate;
    
    @Column(name = "balance_at_start", precision = 19, scale = 4, nullable = false)
    private BigDecimal balanceAtStart;
    
    @Column(name = "balance_at_end", precision = 19, scale = 4, nullable = false)
    private BigDecimal balanceAtEnd;
    
    @Column(name = "average_balance", precision = 19, scale = 4, nullable = false)
    private BigDecimal averageBalance;
    
    @Column(name = "interest_rate", precision = 8, scale = 6, nullable = false)
    private BigDecimal interestRate;
    
    @Column(name = "interest_earned", precision = 19, scale = 4, nullable = false)
    private BigDecimal interestEarned;
    
    @Column(name = "is_credited", nullable = false)
    private Boolean isCredited = false;
    
    @Column(name = "credited_at")
    private LocalDateTime creditedAt;
    
    
    /**
     * Mark interest as credited
     */
    public void markAsCredited() {
        this.isCredited = true;
        this.creditedAt = LocalDateTime.now();
    }
    
    /**
     * Calculate average balance for the day
     */
    public BigDecimal calculateAverageBalance() {
        return balanceAtStart.add(balanceAtEnd).divide(new BigDecimal("2"), 4, RoundingMode.HALF_UP);
    }
}
