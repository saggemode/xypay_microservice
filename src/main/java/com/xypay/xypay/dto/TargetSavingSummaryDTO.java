package com.xypay.xypay.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TargetSavingSummaryDTO {
    
    private Integer totalTargets;
    private Integer activeTargets;
    private Integer completedTargets;
    private Integer overdueTargets;
    private BigDecimal totalTargetAmount;
    private BigDecimal totalCurrentAmount;
    private BigDecimal totalProgressPercentage;
    private List<CategoryBreakdownDTO> categoryBreakdown;
    
    @Data
    public static class CategoryBreakdownDTO {
        private String category;
        private Integer count;
        private BigDecimal totalTarget;
        private BigDecimal totalCurrent;
    }
}
