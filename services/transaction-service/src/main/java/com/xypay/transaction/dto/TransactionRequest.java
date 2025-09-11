package com.xypay.transaction.dto;

import com.xypay.transaction.enums.TransactionChannel;
import com.xypay.transaction.enums.TransactionType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionRequest {
    
    @NotBlank(message = "Account number is required")
    @Size(max = 20, message = "Account number must not exceed 20 characters")
    private String accountNumber;
    
    @Size(max = 20, message = "Receiver account number must not exceed 20 characters")
    private String receiverAccountNumber;
    
    @NotBlank(message = "Reference is required")
    @Size(max = 50, message = "Reference must not exceed 50 characters")
    private String reference;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 15, fraction = 4, message = "Amount must have at most 15 integer digits and 4 decimal places")
    private BigDecimal amount;
    
    @NotNull(message = "Transaction type is required")
    private TransactionType type;
    
    @NotNull(message = "Transaction channel is required")
    private TransactionChannel channel;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @Pattern(regexp = "^(NGN|USD|EUR|GBP)$", message = "Currency must be one of: NGN, USD, EUR, GBP")
    private String currency = "NGN";
    
    private String metadata;
    
    private String idempotencyKey;
    
    private Long parentId;
    
    // Additional fields for enhanced processing
    private String customerId;
    private String customerName;
    private String deviceId;
    private String ipAddress;
    private String userAgent;
    private String location;
    private Boolean isRecurring = false;
    private String recurringReference;
}
