package com.xypay.xypay.dto;

import com.xypay.xypay.domain.SpendAndSaveSettings;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SpendAndSaveSettingsDTO {
    private UUID id;
    private Boolean autoSaveNotifications;
    private Boolean interestNotifications;
    private Boolean withdrawalNotifications;
    private BigDecimal preferredSavingsPercentage;
    private BigDecimal minTransactionThreshold;
    private String defaultWithdrawalDestination;
    private String interestPayoutFrequency;
    private String fundingPreference;
    private Boolean autoWithdrawalEnabled;
    private BigDecimal autoWithdrawalThreshold;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public SpendAndSaveSettingsDTO(SpendAndSaveSettings settings) {
        this.id = settings.getId();
        this.autoSaveNotifications = settings.getAutoSaveNotifications();
        this.interestNotifications = settings.getInterestNotifications();
        this.withdrawalNotifications = settings.getWithdrawalNotifications();
        this.preferredSavingsPercentage = settings.getPreferredSavingsPercentage();
        this.minTransactionThreshold = settings.getMinTransactionThreshold();
        this.defaultWithdrawalDestination = settings.getDefaultWithdrawalDestination() != null ? settings.getDefaultWithdrawalDestination().toString() : null;
        this.interestPayoutFrequency = settings.getInterestPayoutFrequency() != null ? settings.getInterestPayoutFrequency().toString() : null;
        this.fundingPreference = settings.getFundingPreference() != null ? settings.getFundingPreference().toString() : null;
        this.autoWithdrawalEnabled = settings.getAutoWithdrawalEnabled();
        this.autoWithdrawalThreshold = settings.getAutoWithdrawalThreshold();
        this.createdAt = settings.getCreatedAt();
        this.updatedAt = settings.getUpdatedAt();
    }
}