package com.xypay.treasury.dto;

import com.xypay.treasury.domain.TreasuryPosition;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TreasuryPositionResponse {
    
    private Long id;
    private String currencyCode;
    private BigDecimal positionAmount;
    private BigDecimal availableAmount;
    private BigDecimal reservedAmount;
    private LocalDate valueDate;
    private LocalDate maturityDate;
    private String positionType;
    private String liquidityBucket;
    private BigDecimal interestRate;
    private String costCenter;
    private String profitCenter;
    private BigDecimal riskWeight;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static TreasuryPositionResponse fromTreasuryPosition(TreasuryPosition position) {
        TreasuryPositionResponse response = new TreasuryPositionResponse();
        response.setId(position.getId());
        response.setCurrencyCode(position.getCurrencyCode());
        response.setPositionAmount(position.getPositionAmount());
        response.setAvailableAmount(position.getAvailableAmount());
        response.setReservedAmount(position.getReservedAmount());
        response.setValueDate(position.getValueDate());
        response.setMaturityDate(position.getMaturityDate());
        response.setPositionType(position.getPositionType() != null ? position.getPositionType().name() : null);
        response.setLiquidityBucket(position.getLiquidityBucket() != null ? position.getLiquidityBucket().name() : null);
        response.setInterestRate(position.getInterestRate());
        response.setCostCenter(position.getCostCenter());
        response.setProfitCenter(position.getProfitCenter());
        response.setRiskWeight(position.getRiskWeight());
        response.setIsActive(position.getIsActive());
        response.setCreatedAt(position.getCreatedAt());
        response.setUpdatedAt(position.getUpdatedAt());
        return response;
    }
}
