package com.xypay.xypay.dto;

import com.xypay.xypay.domain.TargetSaving;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class TargetSavingDTO {
    private UUID id;
    private String name;
    private String category;
    private String categoryDisplay;
    private BigDecimal targetAmount;
    private String frequency;
    private String frequencyDisplay;
    private String preferredDepositDay;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal currentAmount;
    private Boolean isActive;
    private Boolean isCompleted;
    private String accountNumber;
    private String source;
    private Boolean strictMode;
    private BigDecimal progressPercentage;
    private BigDecimal remainingAmount;
    private Long daysRemaining;
    private Boolean isOverdue;
    private BigDecimal dailyTarget;
    private BigDecimal weeklyTarget;
    private BigDecimal monthlyTarget;
    private List<TargetSavingDepositDTO> recentDeposits;
    private Integer totalDeposits;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public TargetSavingDTO(TargetSaving targetSaving) {
        this.id = targetSaving.getId();
        this.name = targetSaving.getName();
        this.category = targetSaving.getCategory().getCode();
        this.categoryDisplay = targetSaving.getCategory().getDescription();
        this.targetAmount = targetSaving.getTargetAmount();
        this.frequency = targetSaving.getFrequency().getCode();
        this.frequencyDisplay = targetSaving.getFrequency().getDescription();
        this.preferredDepositDay = targetSaving.getPreferredDepositDay();
        this.startDate = targetSaving.getStartDate();
        this.endDate = targetSaving.getEndDate();
        this.currentAmount = targetSaving.getCurrentAmount();
        this.isActive = targetSaving.getIsActive();
        this.isCompleted = targetSaving.getIsCompleted();
        this.accountNumber = targetSaving.getAccountNumber();
        this.source = targetSaving.getSource().getCode();
        this.strictMode = targetSaving.getStrictMode();
        this.progressPercentage = targetSaving.getProgressPercentage();
        this.remainingAmount = targetSaving.getRemainingAmount();
        this.daysRemaining = (long) targetSaving.getDaysRemaining();
        this.isOverdue = targetSaving.isOverdue();
        this.dailyTarget = targetSaving.getDailyTarget();
        this.weeklyTarget = targetSaving.getWeeklyTarget();
        this.monthlyTarget = targetSaving.getMonthlyTarget();
        this.totalDeposits = targetSaving.getDeposits().size();
        this.createdAt = targetSaving.getCreatedAt();
        this.updatedAt = targetSaving.getUpdatedAt();
        
        // Convert recent deposits (limit to 5)
        this.recentDeposits = targetSaving.getDeposits().stream()
            .limit(5)
            .map(TargetSavingDepositDTO::new)
            .toList();
    }
}
