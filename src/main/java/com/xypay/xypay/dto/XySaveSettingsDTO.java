package com.xypay.xypay.dto;

import com.xypay.xypay.domain.XySaveSettings;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class XySaveSettingsDTO {
    
    private UUID id;
    private Boolean dailyInterestNotifications;
    private Boolean goalReminders;
    private Boolean autoSaveNotifications;
    private Boolean investmentUpdates;
    private String preferredInterestPayout;
    private String preferredInterestPayoutDisplay;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public XySaveSettingsDTO() {}
    
    public XySaveSettingsDTO(XySaveSettings settings) {
        this.id = settings.getId();
        this.dailyInterestNotifications = settings.getDailyInterestNotifications();
        this.goalReminders = settings.getGoalReminders();
        this.autoSaveNotifications = settings.getAutoSaveNotifications();
        this.investmentUpdates = settings.getInvestmentUpdates();
        this.preferredInterestPayout = settings.getPreferredInterestPayout().getCode();
        this.preferredInterestPayoutDisplay = settings.getPreferredInterestPayout().getDescription();
        this.createdAt = settings.getCreatedAt();
        this.updatedAt = settings.getUpdatedAt();
    }
}
