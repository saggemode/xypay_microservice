package com.xypay.treasury.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "risk_metrics")
public class RiskMetrics extends BaseEntity {
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "liquidity_forecast_id")
    private LiquidityForecast liquidityForecast;
    
    @Column(name = "liquidity_risk", precision = 19, scale = 2)
    private BigDecimal liquidityRisk;
    
    @Column(name = "concentration_risk", precision = 19, scale = 2)
    private BigDecimal concentrationRisk;
    
    @Column(name = "market_risk", precision = 19, scale = 2)
    private BigDecimal marketRisk;
    
    @Column(name = "operational_risk", precision = 19, scale = 2)
    private BigDecimal operationalRisk;
    
    @Column(name = "total_risk", precision = 19, scale = 2)
    private BigDecimal totalRisk;
    
    @Column(name = "var_95", precision = 19, scale = 2)
    private BigDecimal var95; // Value at Risk 95%
    
    @Column(name = "var_99", precision = 19, scale = 2)
    private BigDecimal var99; // Value at Risk 99%
    
    @Column(name = "expected_shortfall", precision = 19, scale = 2)
    private BigDecimal expectedShortfall;
    
    @Column(name = "risk_tolerance_breach")
    private Boolean riskToleranceBreach = false;
    
    @Column(name = "risk_rating", length = 10)
    private String riskRating; // LOW, MEDIUM, HIGH, CRITICAL
}
