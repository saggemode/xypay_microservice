package com.xypay.xypay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BillPaymentDTO {
    private UUID id;
    
    @NotNull(message = "User is required")
    private UUID userId;
    
    @NotBlank(message = "Service type is required")
    @Size(max = 50, message = "Service type must not exceed 50 characters")
    private String serviceType;
    
    @NotBlank(message = "Account or meter number is required")
    @Size(max = 50, message = "Account or meter number must not exceed 50 characters")
    private String accountOrMeter;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    private String status; // SUCCESS, FAILED, PENDING
    
    private String reference;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    // User information (populated from related user)
    private String userEmail;
    private String userPhone;
    
    // Additional fields for enhanced functionality
    private String provider;
    private String category;
    private String description;
    private String failureReason;
    private String externalReference;
    private String metadata;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
