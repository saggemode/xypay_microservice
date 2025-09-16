package com.xypay.xypay.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class TargetSavingReminderRequestDTO {
    
    @NotBlank(message = "Target ID is required")
    private String targetId;
    
    @Pattern(regexp = "^(weekly|monthly|deadline)$", message = "Reminder type must be 'weekly', 'monthly', or 'deadline'")
    private String reminderType = "weekly";
}
