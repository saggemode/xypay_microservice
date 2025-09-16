package com.xypay.xypay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MerchantSettlementAccountDTO {
    @NotNull(message = "Merchant ID is required")
    private UUID merchantId;
    
    @NotBlank(message = "Bank code is required")
    private String bankCode;
    
    @NotBlank(message = "Account number is required")
    private String accountNumber;
    
    @NotBlank(message = "Account name is required")
    private String accountName;
    
    private Boolean isVerified = false;
    
    private String verificationMethod;
    
    private String preferredSchedule;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
