package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "rate_configurations")
public class RateConfiguration extends BaseEntity {
    
    @Column(name = "rate_code", unique = true, nullable = false)
    private String rateCode;
    
    @Column(name = "rate_name", nullable = false)
    private String rateName;
    
    @Column(name = "rate_type", nullable = false)
    private String rateType; // INTEREST, FEE, PENALTY, EXCHANGE, DISCOUNT
    
    @Column(name = "product_type")
    private String productType;
    
    @Column(name = "customer_segment")
    private String customerSegment; // RETAIL, CORPORATE, VIP, STAFF
    
    @Column(name = "base_rate", precision = 19, scale = 6)
    private BigDecimal baseRate;
    
    @Column(name = "spread", precision = 19, scale = 6)
    private BigDecimal spread;
    
    @Column(name = "minimum_rate", precision = 19, scale = 6)
    private BigDecimal minimumRate;
    
    @Column(name = "maximum_rate", precision = 19, scale = 6)
    private BigDecimal maximumRate;
    
    @Column(name = "calculation_method")
    private String calculationMethod; // SIMPLE, COMPOUND, FLAT, REDUCING
    
    @Column(name = "compounding_frequency")
    private String compoundingFrequency; // DAILY, MONTHLY, QUARTERLY, ANNUALLY
    
    @Column(name = "rate_basis")
    private String rateBasis; // 360, 365, ACTUAL
    
    @Column(name = "tier_structure", columnDefinition = "TEXT")
    private String tierStructure; // JSON for tiered rates
    
    @Column(name = "pricing_formula", columnDefinition = "TEXT")
    private String pricingFormula; // Custom formula for rate calculation
    
    @Column(name = "benchmark_rate")
    private String benchmarkRate; // Reference rate (LIBOR, PRIME, etc.)
    
    @Column(name = "review_frequency")
    private String reviewFrequency; // DAILY, WEEKLY, MONTHLY, QUARTERLY
    
    @Column(name = "auto_adjustment")
    private Boolean autoAdjustment = false;
    
    @Column(name = "adjustment_threshold", precision = 19, scale = 6)
    private BigDecimal adjustmentThreshold;
    
    @Column(name = "effective_from", nullable = false)
    private java.time.LocalDateTime effectiveFrom;
    
    @Column(name = "effective_to")
    private java.time.LocalDateTime effectiveTo;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "approval_status")
    private String approvalStatus = "DRAFT"; // DRAFT, PENDING, APPROVED, REJECTED
}
