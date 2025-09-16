package com.xypay.xypay.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class FixedSavingsSettingsDTO {
    
    private UUID id;
    private String user;
    private Boolean maturityNotifications = true;
    private Boolean interestNotifications = true;
    private Boolean autoRenewalNotifications = true;
    private Boolean defaultAutoRenewal = false;
    
    @Min(value = 7, message = "Default renewal duration must be at least 7 days")
    @Max(value = 1000, message = "Default renewal duration cannot exceed 1000 days")
    private Integer defaultRenewalDuration = 30;
    
    private String defaultSource = "WALLET";
    private String defaultSourceDisplay;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}