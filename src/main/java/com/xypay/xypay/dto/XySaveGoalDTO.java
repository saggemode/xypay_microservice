package com.xypay.xypay.dto;

import com.xypay.xypay.domain.XySaveGoal;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class XySaveGoalDTO {
    
    private UUID id;
    private String name;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate targetDate;
    private Boolean isActive;
    private BigDecimal progressPercentage;
    private Boolean isCompleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public XySaveGoalDTO() {}
    
    public XySaveGoalDTO(XySaveGoal goal) {
        this.id = goal.getId();
        this.name = goal.getName();
        this.targetAmount = goal.getTargetAmount();
        this.currentAmount = goal.getCurrentAmount();
        this.targetDate = goal.getTargetDate();
        this.isActive = goal.getIsActive();
        this.progressPercentage = goal.getProgressPercentage();
        this.isCompleted = goal.isCompleted();
        this.createdAt = goal.getCreatedAt();
        this.updatedAt = goal.getUpdatedAt();
    }
}
