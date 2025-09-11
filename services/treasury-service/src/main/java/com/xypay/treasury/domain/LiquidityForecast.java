package com.xypay.treasury.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "liquidity_forecasts")
public class LiquidityForecast extends BaseEntity {
    
    @Column(name = "currency_code", length = 3, nullable = false)
    private String currencyCode;
    
    @Column(name = "forecast_date", nullable = false)
    private LocalDate forecastDate;
    
    @Column(name = "forecast_days", nullable = false)
    private Integer forecastDays;
    
    @Column(name = "current_liquidity", precision = 19, scale = 2, nullable = false)
    private BigDecimal currentLiquidity;
    
    @Column(name = "minimum_required_liquidity", precision = 19, scale = 2)
    private BigDecimal minimumRequiredLiquidity;
    
    @Column(name = "risk_tolerance", precision = 5, scale = 2)
    private BigDecimal riskTolerance = new BigDecimal("0.10"); // 10% default
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @OneToMany(mappedBy = "liquidityForecast", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LiquidityProjection> projections = new ArrayList<>();
    
    @OneToOne(mappedBy = "liquidityForecast", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private RiskMetrics riskMetrics;
    
    public void addProjection(LiquidityProjection projection) {
        projection.setLiquidityForecast(this);
        this.projections.add(projection);
    }
}
