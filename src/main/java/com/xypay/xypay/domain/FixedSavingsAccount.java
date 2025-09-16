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
@Table(name = "fixed_savings_accounts")
public class FixedSavingsAccount {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "account_number", unique = true, length = 20)
    private String accountNumber;
    
    @Column(name = "amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 10, nullable = false)
    private FixedSavingsSource source;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", length = 20, nullable = false)
    private FixedSavingsPurpose purpose;
    
    @Column(name = "purpose_description", columnDefinition = "TEXT")
    private String purposeDescription;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "payback_date", nullable = false)
    private LocalDate paybackDate;
    
    @Column(name = "auto_renewal_enabled", nullable = false)
    private Boolean autoRenewalEnabled = false;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "is_matured", nullable = false)
    private Boolean isMatured = false;
    
    @Column(name = "is_paid_out", nullable = false)
    private Boolean isPaidOut = false;
    
    @Column(name = "interest_rate", precision = 5, scale = 2)
    private BigDecimal interestRate;
    
    @Column(name = "total_interest_earned", precision = 19, scale = 4)
    private BigDecimal totalInterestEarned = BigDecimal.ZERO;
    
    @Column(name = "maturity_amount", precision = 19, scale = 4)
    private BigDecimal maturityAmount;
    
    @Column(name = "matured_at")
    private LocalDateTime maturedAt;
    
    @Column(name = "paid_out_at")
    private LocalDateTime paidOutAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "fixedSavingsAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FixedSavingsTransaction> transactions;
    
    // Tier thresholds in days
    public static final int TIER_1_MIN_DAYS = 7;
    public static final int TIER_1_MAX_DAYS = 29;
    public static final BigDecimal TIER_1_RATE = new BigDecimal("0.10"); // 10% p.a.
    
    public static final int TIER_2_MIN_DAYS = 30;
    public static final int TIER_2_MAX_DAYS = 59;
    public static final BigDecimal TIER_2_RATE = new BigDecimal("0.10"); // 10% p.a.
    
    public static final int TIER_3_MIN_DAYS = 60;
    public static final int TIER_3_MAX_DAYS = 89;
    public static final BigDecimal TIER_3_RATE = new BigDecimal("0.12"); // 12% p.a.
    
    public static final int TIER_4_MIN_DAYS = 90;
    public static final int TIER_4_MAX_DAYS = 179;
    public static final BigDecimal TIER_4_RATE = new BigDecimal("0.15"); // 15% p.a.
    
    public static final int TIER_5_MIN_DAYS = 180;
    public static final int TIER_5_MAX_DAYS = 364;
    public static final BigDecimal TIER_5_RATE = new BigDecimal("0.18"); // 18% p.a.
    
    public static final int TIER_6_MIN_DAYS = 365;
    public static final int TIER_6_MAX_DAYS = 1000;
    public static final BigDecimal TIER_6_RATE = new BigDecimal("0.20"); // 20% p.a.
    
    // Constructors
    public FixedSavingsAccount() {}
    
    /**
     * Calculate interest rate based on duration
     */
    public BigDecimal calculateInterestRate() {
        if (startDate == null || paybackDate == null) {
            return BigDecimal.ZERO;
        }
        
        int durationDays = getDurationDays();
        
        if (TIER_1_MIN_DAYS <= durationDays && durationDays <= TIER_1_MAX_DAYS) {
            return TIER_1_RATE.multiply(new BigDecimal("100"));
        } else if (TIER_2_MIN_DAYS <= durationDays && durationDays <= TIER_2_MAX_DAYS) {
            return TIER_2_RATE.multiply(new BigDecimal("100"));
        } else if (TIER_3_MIN_DAYS <= durationDays && durationDays <= TIER_3_MAX_DAYS) {
            return TIER_3_RATE.multiply(new BigDecimal("100"));
        } else if (TIER_4_MIN_DAYS <= durationDays && durationDays <= TIER_4_MAX_DAYS) {
            return TIER_4_RATE.multiply(new BigDecimal("100"));
        } else if (TIER_5_MIN_DAYS <= durationDays && durationDays <= TIER_5_MAX_DAYS) {
            return TIER_5_RATE.multiply(new BigDecimal("100"));
        } else if (TIER_6_MIN_DAYS <= durationDays && durationDays <= TIER_6_MAX_DAYS) {
            return TIER_6_RATE.multiply(new BigDecimal("100"));
        } else {
            return BigDecimal.ZERO; // Invalid duration
        }
    }
    
    /**
     * Calculate maturity amount (principal + interest)
     */
    public BigDecimal calculateMaturityAmount() {
        if (interestRate == null || interestRate.compareTo(BigDecimal.ZERO) == 0) {
            return amount;
        }
        
        int durationDays = getDurationDays();
        BigDecimal annualRate = interestRate.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        BigDecimal dailyRate = annualRate.divide(new BigDecimal("365"), 10, RoundingMode.HALF_UP);
        
        BigDecimal interestEarned = amount.multiply(dailyRate).multiply(new BigDecimal(durationDays));
        return amount.add(interestEarned);
    }
    
    /**
     * Calculate duration in days
     */
    public int getDurationDays() {
        if (startDate == null || paybackDate == null) {
            return 0;
        }
        return (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, paybackDate);
    }
    
    /**
     * Calculate days remaining until maturity
     */
    public int getDaysRemaining() {
        if (paybackDate == null) {
            return 0;
        }
        LocalDate today = LocalDate.now();
        int remaining = (int) java.time.temporal.ChronoUnit.DAYS.between(today, paybackDate);
        return Math.max(0, remaining);
    }
    
    /**
     * Check if the fixed savings has matured
     */
    public boolean isMature() {
        if (paybackDate == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        return !today.isBefore(paybackDate);
    }
    
    /**
     * Check if the fixed savings can be paid out
     */
    public boolean canBePaidOut() {
        return isMature() && !isPaidOut && isActive;
    }
    
    /**
     * Mark the fixed savings as matured
     */
    public boolean markAsMatured() {
        if (!isMature()) {
            return false;
        }
        
        isMatured = true;
        maturedAt = LocalDateTime.now();
        return true;
    }
    
    /**
     * Pay out the matured amount to user's xysave account
     */
    public boolean payOut() {
        if (!canBePaidOut()) {
            return false;
        }
        
        try {
            // This will be implemented in the service layer
            // as it requires access to repositories
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @PrePersist
    protected void onCreate() {
        if (accountNumber == null) {
            accountNumber = generateAccountNumber();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
        
        // Calculate interest rate and maturity amount
        if (interestRate == null) {
            interestRate = calculateInterestRate();
        }
        if (maturityAmount == null) {
            maturityAmount = calculateMaturityAmount();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    private String generateAccountNumber() {
        String userId = String.format("%08d", user.getId());
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "FS" + userId + uuid;
    }
}