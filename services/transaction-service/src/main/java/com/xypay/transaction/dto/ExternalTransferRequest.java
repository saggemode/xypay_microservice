package com.xypay.transaction.dto;

import com.xypay.transaction.enums.TransactionChannel;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExternalTransferRequest {
    
    @NotBlank(message = "Source account number is required")
    @Size(max = 20, message = "Source account number must not exceed 20 characters")
    private String sourceAccountNumber;
    
    @NotBlank(message = "Destination bank is required")
    @Size(max = 50, message = "Destination bank must not exceed 50 characters")
    private String destinationBank;
    
    @NotBlank(message = "Destination account number is required")
    @Size(max = 20, message = "Destination account number must not exceed 20 characters")
    private String destinationAccountNumber;
    
    @NotBlank(message = "Destination account name is required")
    @Size(max = 100, message = "Destination account name must not exceed 100 characters")
    private String destinationAccountName;
    
    @NotBlank(message = "Reference is required")
    @Size(max = 50, message = "Reference must not exceed 50 characters")
    private String reference;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1000.00", message = "External transfer minimum amount is ₦1,000")
    @DecimalMax(value = "1000000.00", message = "External transfer maximum amount is ₦1,000,000")
    @Digits(integer = 15, fraction = 4, message = "Amount must have at most 15 integer digits and 4 decimal places")
    private BigDecimal amount;
    
    @NotNull(message = "Transaction channel is required")
    private TransactionChannel channel;
    
    @Pattern(regexp = "^(NGN|USD|EUR|GBP)$", message = "Currency must be one of: NGN, USD, EUR, GBP")
    private String currency = "NGN";
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    private String routingNumber;
    private String swiftCode;
    private String iban;
    
    // Transfer type: EXTERNAL, INTER_BANK, RTGS
    private String transferType = "EXTERNAL";
    
    private String idempotencyKey;
    
    // Additional fields for enhanced processing
    private String customerId;
    private String customerName;
    private String deviceId;
    private String ipAddress;
    private String userAgent;
    private String location;
    
    // Security fields
    private String pin;
    private String otp;
    private String deviceFingerprint;
    
    // Compliance fields
    private String purposeOfTransfer;
    private String sourceOfFunds;
    private Boolean isHighValue = false;
    private Boolean requiresApproval = false;
}
