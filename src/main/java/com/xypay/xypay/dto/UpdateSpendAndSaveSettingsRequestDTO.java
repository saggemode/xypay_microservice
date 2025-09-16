package com.xypay.xypay.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateSpendAndSaveSettingsRequestDTO {
    
    private Boolean autoSaveNotifications;
    
    private Boolean interestNotifications;
    
    private Boolean withdrawalNotifications;
    
    @DecimalMin(value = "1.0", message = "Preferred savings percentage must be at least 1%")
    @DecimalMax(value = "50.0", message = "Preferred savings percentage must not exceed 50%")
    @Digits(integer = 3, fraction = 2, message = "Preferred savings percentage must have at most 3 integer digits and 2 decimal places")
    private BigDecimal preferredSavingsPercentage;
    
    @DecimalMin(value = "0.01", message = "Minimum transaction threshold must be greater than 0")
    @Digits(integer = 15, fraction = 4, message = "Minimum transaction threshold must have at most 15 integer digits and 4 decimal places")
    private BigDecimal minTransactionThreshold;
    
    @Pattern(regexp = "^(wallet|xysave)$", message = "Default withdrawal destination must be 'wallet' or 'xysave'")
    private String defaultWithdrawalDestination;
    
    @Pattern(regexp = "^(daily|weekly|monthly)$", message = "Interest payout frequency must be 'daily', 'weekly', or 'monthly'")
    private String interestPayoutFrequency;
    
    @Pattern(regexp = "^(auto|wallet|xysave)$", message = "Funding preference must be 'auto', 'wallet', or 'xysave'")
    private String fundingPreference;
    
    private Boolean autoWithdrawalEnabled;
    
    @DecimalMin(value = "0.01", message = "Auto withdrawal threshold must be greater than 0")
    @Digits(integer = 15, fraction = 4, message = "Auto withdrawal threshold must have at most 15 integer digits and 4 decimal places")
    private BigDecimal autoWithdrawalThreshold;
}
