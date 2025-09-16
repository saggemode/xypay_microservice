package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "xysave_accounts")
public class XySaveAccount {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(name = "account_number", length = 20, unique = true, nullable = false)
    private String accountNumber;
    
    @Column(name = "balance", precision = 19, scale = 4, nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;
    
    @Column(name = "total_interest_earned", precision = 19, scale = 4, nullable = false)
    private BigDecimal totalInterestEarned = BigDecimal.ZERO;
    
    @Column(name = "last_interest_calculation", nullable = false)
    private LocalDateTime lastInterestCalculation;
    
    @Column(name = "daily_interest_rate", precision = 8, scale = 6, nullable = false)
    private BigDecimal dailyInterestRate = new BigDecimal("0.0004"); // 0.04% daily = ~15% annual
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "auto_save_enabled", nullable = false)
    private Boolean autoSaveEnabled = false;
    
    @Column(name = "auto_save_percentage", precision = 5, scale = 2, nullable = false)
    private BigDecimal autoSavePercentage = new BigDecimal("10.00"); // 10% by default
    
    @Column(name = "auto_save_min_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal autoSaveMinAmount = new BigDecimal("100.00");
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "xysaveAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<XySaveTransaction> transactions;
    
    @OneToMany(mappedBy = "xysaveAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<XySaveGoal> goals;
    
    @OneToMany(mappedBy = "xysaveAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<XySaveInvestment> investments;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (lastInterestCalculation == null) {
            lastInterestCalculation = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
        
        if (accountNumber == null) {
            accountNumber = generateAccountNumber();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    private String generateAccountNumber() {
        String userId = String.format("%08d", user.getId());
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "XS" + userId + uuid;
    }
    
    public BigDecimal calculateDailyInterest() {
        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        // Tiered interest calculation
        BigDecimal totalInterest = BigDecimal.ZERO;
        BigDecimal remainingBalance = balance;
        
        // Tier 1: First 10,000 at 20% p.a
        BigDecimal tier1Threshold = new BigDecimal("10000");
        if (remainingBalance.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tier1Amount = remainingBalance.min(tier1Threshold);
            BigDecimal tier1Rate = new BigDecimal("0.20").divide(new BigDecimal("365"), 10, RoundingMode.HALF_UP);
            totalInterest = totalInterest.add(tier1Amount.multiply(tier1Rate));
            remainingBalance = remainingBalance.subtract(tier1Amount);
        }
        
        // Tier 2: Next 90,000 (10,001 - 100,000) at 16% p.a
        BigDecimal tier2Threshold = new BigDecimal("100000");
        if (remainingBalance.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tier2Amount = remainingBalance.min(tier2Threshold.subtract(tier1Threshold));
            BigDecimal tier2Rate = new BigDecimal("0.16").divide(new BigDecimal("365"), 10, RoundingMode.HALF_UP);
            totalInterest = totalInterest.add(tier2Amount.multiply(tier2Rate));
            remainingBalance = remainingBalance.subtract(tier2Amount);
        }
        
        // Tier 3: Above 100,000 at 8% p.a
        if (remainingBalance.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tier3Rate = new BigDecimal("0.08").divide(new BigDecimal("365"), 10, RoundingMode.HALF_UP);
            totalInterest = totalInterest.add(remainingBalance.multiply(tier3Rate));
        }
        
        return totalInterest;
    }
    
    public BigDecimal getAnnualInterestRate() {
        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal dailyInterest = calculateDailyInterest();
        BigDecimal effectiveDailyRate = dailyInterest.divide(balance, 10, RoundingMode.HALF_UP);
        return effectiveDailyRate.multiply(new BigDecimal("36500")); // 365 * 100
    }
    
    public boolean canWithdraw(BigDecimal amount) {
        return balance.compareTo(amount) >= 0 && isActive;
    }
    
    public String getInterestBreakdown() {
        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            return "{\"tier_1\":{\"amount\":0,\"rate\":20,\"daily_rate\":0,\"interest\":0},\"tier_2\":{\"amount\":0,\"rate\":16,\"daily_rate\":0,\"interest\":0},\"tier_3\":{\"amount\":0,\"rate\":8,\"daily_rate\":0,\"interest\":0},\"total_interest\":0}";
        }
        
        BigDecimal remainingBalance = balance;
        BigDecimal tier1Threshold = new BigDecimal("10000");
        BigDecimal tier2Threshold = new BigDecimal("100000");
        BigDecimal tier1Rate = new BigDecimal("0.20").divide(new BigDecimal("365"), 10, RoundingMode.HALF_UP);
        BigDecimal tier2Rate = new BigDecimal("0.16").divide(new BigDecimal("365"), 10, RoundingMode.HALF_UP);
        BigDecimal tier3Rate = new BigDecimal("0.08").divide(new BigDecimal("365"), 10, RoundingMode.HALF_UP);
        
        BigDecimal tier1Amount = remainingBalance.min(tier1Threshold);
        BigDecimal tier1Interest = tier1Amount.multiply(tier1Rate);
        remainingBalance = remainingBalance.subtract(tier1Amount);
        
        BigDecimal tier2Amount = remainingBalance.min(tier2Threshold.subtract(tier1Threshold));
        BigDecimal tier2Interest = tier2Amount.multiply(tier2Rate);
        remainingBalance = remainingBalance.subtract(tier2Amount);
        
        BigDecimal tier3Amount = remainingBalance;
        BigDecimal tier3Interest = tier3Amount.multiply(tier3Rate);
        
        BigDecimal totalInterest = tier1Interest.add(tier2Interest).add(tier3Interest);
        
        return String.format(
            "{\"tier_1\":{\"amount\":%.2f,\"rate\":20,\"daily_rate\":%.6f,\"interest\":%.6f},\"tier_2\":{\"amount\":%.2f,\"rate\":16,\"daily_rate\":%.6f,\"interest\":%.6f},\"tier_3\":{\"amount\":%.2f,\"rate\":8,\"daily_rate\":%.6f,\"interest\":%.6f},\"total_interest\":%.6f}",
            tier1Amount, tier1Rate, tier1Interest,
            tier2Amount, tier2Rate, tier2Interest,
            tier3Amount, tier3Rate, tier3Interest,
            totalInterest
        );
    }
}