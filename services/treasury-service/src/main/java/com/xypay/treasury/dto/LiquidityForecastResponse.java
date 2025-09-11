package com.xypay.treasury.dto;

import com.xypay.treasury.domain.LiquidityForecast;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class LiquidityForecastResponse {
    
    private Long id;
    private String currencyCode;
    private LocalDate forecastDate;
    private Integer forecastDays;
    private BigDecimal currentLiquidity;
    private BigDecimal minimumRequiredLiquidity;
    private BigDecimal riskTolerance;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<LiquidityProjectionResponse> projections;
    private RiskMetricsResponse riskMetrics;
    
    public static LiquidityForecastResponse fromLiquidityForecast(LiquidityForecast forecast) {
        LiquidityForecastResponse response = new LiquidityForecastResponse();
        response.setId(forecast.getId());
        response.setCurrencyCode(forecast.getCurrencyCode());
        response.setForecastDate(forecast.getForecastDate());
        response.setForecastDays(forecast.getForecastDays());
        response.setCurrentLiquidity(forecast.getCurrentLiquidity());
        response.setMinimumRequiredLiquidity(forecast.getMinimumRequiredLiquidity());
        response.setRiskTolerance(forecast.getRiskTolerance());
        response.setIsActive(forecast.getIsActive());
        response.setCreatedAt(forecast.getCreatedAt());
        response.setUpdatedAt(forecast.getUpdatedAt());
        
        if (forecast.getProjections() != null) {
            response.setProjections(forecast.getProjections().stream()
                .map(LiquidityProjectionResponse::fromLiquidityProjection)
                .collect(Collectors.toList()));
        }
        
        if (forecast.getRiskMetrics() != null) {
            response.setRiskMetrics(RiskMetricsResponse.fromRiskMetrics(forecast.getRiskMetrics()));
        }
        
        return response;
    }
}
