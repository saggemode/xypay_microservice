package com.xypay.xypay.config;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "loan_product_configurations")
public class LoanProductConfiguration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "product_name")
    private String productName;
    
    @Column(name = "minimum_amount")
    private BigDecimal minimumAmount;
    
    @Column(name = "maximum_amount")
    private BigDecimal maximumAmount;
    
    @Column(name = "interest_rate")
    private BigDecimal interestRate;
    
    @Column(name = "minimum_term")
    private Integer minimumTerm;
    
    @Column(name = "maximum_term")
    private Integer maximumTerm;
    
    @Column(name = "term_unit")
    private String termUnit; // DAYS, MONTHS, YEARS
    
    @Column(name = "processing_fee")
    private BigDecimal processingFee;
    
    @Column(name = "insurance_required")
    private Boolean insuranceRequired;
    
    @Column(name = "collateral_required")
    private Boolean collateralRequired;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    // Explicit getter and setter for isActive field
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}