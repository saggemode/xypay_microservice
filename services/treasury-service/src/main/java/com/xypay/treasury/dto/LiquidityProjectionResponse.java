package com.xypay.treasury.dto;

import com.xypay.treasury.domain.LiquidityProjection;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LiquidityProjectionResponse {
    
    private Long id;
    private LocalDate forecastDate;
    private BigDecimal projectedLiquidity;
    private BigDecimal confidenceLevel;
    private BigDecimal variance;
    private String scenario;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static LiquidityProjectionResponse fromLiquidityProjection(LiquidityProjection projection) {
        LiquidityProjectionResponse response = new LiquidityProjectionResponse();
        response.setId(projection.getId());
        response.setForecastDate(projection.getForecastDate());
        response.setProjectedLiquidity(projection.getProjectedLiquidity());
        response.setConfidenceLevel(projection.getConfidenceLevel());
        response.setVariance(projection.getVariance());
        response.setScenario(projection.getScenario());
        response.setNotes(projection.getNotes());
        response.setCreatedAt(projection.getCreatedAt());
        response.setUpdatedAt(projection.getUpdatedAt());
        return response;
    }
}
