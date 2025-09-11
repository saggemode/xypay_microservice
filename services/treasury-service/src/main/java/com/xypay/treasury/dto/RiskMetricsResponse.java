package com.xypay.treasury.dto;

import com.xypay.treasury.domain.RiskMetrics;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RiskMetricsResponse {
    
    private Long id;
    private BigDecimal liquidityRisk;
    private BigDecimal concentrationRisk;
    private BigDecimal marketRisk;
    private BigDecimal operationalRisk;
    private BigDecimal totalRisk;
    private BigDecimal var95;
    private BigDecimal var99;
    private BigDecimal expectedShortfall;
    private Boolean riskToleranceBreach;
    private String riskRating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static RiskMetricsResponse fromRiskMetrics(RiskMetrics riskMetrics) {
        RiskMetricsResponse response = new RiskMetricsResponse();
        response.setId(riskMetrics.getId());
        response.setLiquidityRisk(riskMetrics.getLiquidityRisk());
        response.setConcentrationRisk(riskMetrics.getConcentrationRisk());
        response.setMarketRisk(riskMetrics.getMarketRisk());
        response.setOperationalRisk(riskMetrics.getOperationalRisk());
        response.setTotalRisk(riskMetrics.getTotalRisk());
        response.setVar95(riskMetrics.getVar95());
        response.setVar99(riskMetrics.getVar99());
        response.setExpectedShortfall(riskMetrics.getExpectedShortfall());
        response.setRiskToleranceBreach(riskMetrics.getRiskToleranceBreach());
        response.setRiskRating(riskMetrics.getRiskRating());
        response.setCreatedAt(riskMetrics.getCreatedAt());
        response.setUpdatedAt(riskMetrics.getUpdatedAt());
        return response;
    }
}
