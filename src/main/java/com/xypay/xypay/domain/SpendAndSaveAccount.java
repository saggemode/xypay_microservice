package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "spend_and_save_accounts")
public class SpendAndSaveAccount {
    
    public enum WithdrawalDestination {
        WALLET("wallet", "Wallet"),
        XYSAVE("xysave", "XySave Account");
        
        private final String code;
        private final String description;
        
        WithdrawalDestination(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // Tier thresholds
    public static final int TIER_1_THRESHOLD = 10000; // First 10,000 at 20% p.a
    public static final int TIER_2_THRESHOLD = 100000; // 10,001 - 100,000 at 16% p.a
    public static final BigDecimal TIER_1_RATE = new BigDecimal("0.20"); // 20% p.a
    public static final BigDecimal TIER_2_RATE = new BigDecimal("0.16"); // 16% p.a
    public static final BigDecimal TIER_3_RATE = new BigDecimal("0.08"); // 8% p.a
    
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
    
    @Column(name = "total_saved_from_spending", precision = 19, scale = 4, nullable = false)
    private BigDecimal totalSavedFromSpending = BigDecimal.ZERO;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;
    
    @Column(name = "savings_percentage", precision = 5, scale = 2, nullable = false)
    private BigDecimal savingsPercentage = new BigDecimal("5.00");
    
    @Column(name = "min_transaction_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal minTransactionAmount = new BigDecimal("100.00");
    
    @Column(name = "daily_tier_1_rate", precision = 8, scale = 6, nullable = false)
    private BigDecimal dailyTier1Rate = new BigDecimal("0.000548"); // 20% / 365
    
    @Column(name = "daily_tier_2_rate", precision = 8, scale = 6, nullable = false)
    private BigDecimal dailyTier2Rate = new BigDecimal("0.000438"); // 16% / 365
    
    @Column(name = "daily_tier_3_rate", precision = 8, scale = 6, nullable = false)
    private BigDecimal dailyTier3Rate = new BigDecimal("0.000219"); // 8% / 365
    
    @Column(name = "last_interest_calculation", nullable = false)
    private LocalDateTime lastInterestCalculation;
    
    @Column(name = "total_transactions_processed", nullable = false)
    private Integer totalTransactionsProcessed = 0;
    
    @Column(name = "last_auto_save_date")
    private LocalDate lastAutoSaveDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "default_withdrawal_destination", length = 20, nullable = false)
    private WithdrawalDestination defaultWithdrawalDestination = WithdrawalDestination.WALLET;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "spendAndSaveAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SpendAndSaveTransaction> transactions;
    
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
        return "SS" + userId + uuid;
    }
    
    public BigDecimal calculateTieredInterest() {
        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal remainingBalance = balance;
        BigDecimal totalInterest = BigDecimal.ZERO;
        
        // Tier 1: First 10,000 at 20% p.a
        if (remainingBalance.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tier1Amount = remainingBalance.min(new BigDecimal(TIER_1_THRESHOLD));
            BigDecimal tier1Interest = tier1Amount.multiply(dailyTier1Rate);
            totalInterest = totalInterest.add(tier1Interest);
            remainingBalance = remainingBalance.subtract(tier1Amount);
        }
        
        // Tier 2: 10,001 - 100,000 at 16% p.a
        if (remainingBalance.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tier2Amount = remainingBalance.min(new BigDecimal(TIER_2_THRESHOLD - TIER_1_THRESHOLD));
            BigDecimal tier2Interest = tier2Amount.multiply(dailyTier2Rate);
            totalInterest = totalInterest.add(tier2Interest);
            remainingBalance = remainingBalance.subtract(tier2Amount);
        }
        
        // Tier 3: Above 100,000 at 8% p.a
        if (remainingBalance.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tier3Interest = remainingBalance.multiply(dailyTier3Rate);
            totalInterest = totalInterest.add(tier3Interest);
        }
        
        return totalInterest;
    }
    
    public String getInterestBreakdown() {
        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            return "{\"tier_1\":{\"amount\":0,\"rate\":20,\"interest\":0},\"tier_2\":{\"amount\":0,\"rate\":16,\"interest\":0},\"tier_3\":{\"amount\":0,\"rate\":8,\"interest\":0},\"total_interest\":0}";
        }
        
        BigDecimal remainingBalance = balance;
        BigDecimal tier1Amount = remainingBalance.min(new BigDecimal(TIER_1_THRESHOLD));
        BigDecimal tier1Interest = tier1Amount.multiply(dailyTier1Rate);
        remainingBalance = remainingBalance.subtract(tier1Amount);
        
        BigDecimal tier2Amount = remainingBalance.min(new BigDecimal(TIER_2_THRESHOLD - TIER_1_THRESHOLD));
        BigDecimal tier2Interest = tier2Amount.multiply(dailyTier2Rate);
        remainingBalance = remainingBalance.subtract(tier2Amount);
        
        BigDecimal tier3Amount = remainingBalance;
        BigDecimal tier3Interest = tier3Amount.multiply(dailyTier3Rate);
        
        BigDecimal totalInterest = tier1Interest.add(tier2Interest).add(tier3Interest);
        
        return String.format(
            "{\"tier_1\":{\"amount\":%.2f,\"rate\":20,\"interest\":%.6f},\"tier_2\":{\"amount\":%.2f,\"rate\":16,\"interest\":%.6f},\"tier_3\":{\"amount\":%.2f,\"rate\":8,\"interest\":%.6f},\"total_interest\":%.6f}",
            tier1Amount, tier1Interest,
            tier2Amount, tier2Interest,
            tier3Amount, tier3Interest,
            totalInterest
        );
    }
    
    public boolean canWithdraw(BigDecimal amount) {
        return balance.compareTo(amount) >= 0 && isActive;
    }
    
    public void activate(BigDecimal savingsPercentage) {
        this.isActive = true;
        this.savingsPercentage = savingsPercentage;
    }
    
    public void deactivate() {
        this.isActive = false;
    }
    
    public BigDecimal processSpendingTransaction(BigDecimal transactionAmount) {
        if (!isActive || transactionAmount.compareTo(minTransactionAmount) < 0) {
            return BigDecimal.ZERO;
        }
        
        // Calculate auto-save based on the transfer amount
        BigDecimal autoSaveAmount = transactionAmount.multiply(savingsPercentage.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        return autoSaveAmount;
    }
}