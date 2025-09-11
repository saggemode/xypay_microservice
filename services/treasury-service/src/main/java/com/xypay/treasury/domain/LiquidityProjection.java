package com.xypay.treasury.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "liquidity_projections")
public class LiquidityProjection extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "liquidity_forecast_id", nullable = false)
    private LiquidityForecast liquidityForecast;
    
    @Column(name = "forecast_date", nullable = false)
    private LocalDate forecastDate;
    
    @Column(name = "projected_liquidity", precision = 19, scale = 2, nullable = false)
    private BigDecimal projectedLiquidity;
    
    @Column(name = "confidence_level", precision = 5, scale = 4)
    private BigDecimal confidenceLevel;
    
    @Column(name = "variance", precision = 19, scale = 2)
    private BigDecimal variance;
    
    @Column(name = "scenario", length = 50)
    private String scenario = "BASE"; // BASE, STRESS, OPTIMISTIC
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
