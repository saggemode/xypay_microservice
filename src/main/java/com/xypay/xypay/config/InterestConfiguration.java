package com.xypay.xypay.config;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "interest_configurations")
public class InterestConfiguration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "configuration_name")
    private String configurationName;
    
    @Column(name = "product_type")
    private String productType; // SAVINGS, LOAN
    
    @Column(name = "calculation_method")
    private String calculationMethod; // SIMPLE, COMPOUND
    
    @Column(name = "compounding_frequency")
    private String compoundingFrequency; // DAILY, MONTHLY, QUARTERLY, ANNUALLY
    
    @Column(name = "interest_rate")
    private BigDecimal interestRate;
    
    @Column(name = "preferential_rate")
    private BigDecimal preferentialRate;
    
    @Column(name = "standard_rate")
    private BigDecimal standardRate;
    
    @Column(name = "minimum_balance")
    private BigDecimal minimumBalance;
    
    @Column(name = "tiered_rates")
    private String tieredRates; // JSON format for tiered interest rates
    
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
    
    // Getters and setters for new fields
    public BigDecimal getPreferentialRate() {
        return preferentialRate;
    }
    
    public void setPreferentialRate(BigDecimal preferentialRate) {
        this.preferentialRate = preferentialRate;
    }
    
    public BigDecimal getStandardRate() {
        return standardRate;
    }
    
    public void setStandardRate(BigDecimal standardRate) {
        this.standardRate = standardRate;
    }
    
    // Getter and setter for configurationName
    public String getConfigurationName() {
        return configurationName;
    }
    
    public void setConfigurationName(String configurationName) {
        this.configurationName = configurationName;
    }
}