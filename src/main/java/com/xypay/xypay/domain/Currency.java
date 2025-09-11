package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "currencies")
public class Currency extends BaseEntity {
    
    @Column(name = "code", length = 3, unique = true, nullable = false)
    private String code; // USD, EUR, NGN, GBP, etc.
    
    @Column(name = "name", length = 100, nullable = false)
    private String name;
    
    @Column(name = "symbol", length = 10)
    private String symbol;
    
    @Column(name = "numeric_code", length = 3)
    private String numericCode; // ISO 4217 numeric code
    
    @Column(name = "decimal_places")
    private Integer decimalPlaces = 2;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "country", length = 100)
    private String country;
    
    @Column(name = "is_crypto")
    private Boolean isCrypto = false;
    
    @Column(name = "is_fiat")
    private Boolean isFiat = true;
    
    // Exchange rate information
    @Column(name = "base_exchange_rate", precision = 19, scale = 6)
    private BigDecimal baseExchangeRate = BigDecimal.ONE;
    
    @Column(name = "rate_last_updated")
    private LocalDateTime rateLastUpdated;
    
    // Regulatory compliance
    @Column(name = "aml_risk_level")
    @Enumerated(EnumType.STRING)
    private RiskLevel amlRiskLevel = RiskLevel.LOW;
    
    @Column(name = "sanctions_screening_required")
    private Boolean sanctionsScreeningRequired = true;
    
    @Column(name = "kyc_enhanced_required")
    private Boolean kycEnhancedRequired = false;
    
    public enum RiskLevel {
        LOW, MEDIUM, HIGH, VERY_HIGH
    }
}
