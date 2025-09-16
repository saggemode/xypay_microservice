package com.xypay.xypay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class VirtualCardDTO {
    private UUID id;
    
    @NotNull(message = "User is required")
    private UUID userId;
    
    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "^[0-9]{16}$", message = "Card number must be 16 digits")
    private String cardNumber;
    
    @NotBlank(message = "Expiry date is required")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$", message = "Expiry date must be in MM/YY format")
    private String expiry;
    
    @NotBlank(message = "CVV is required")
    @Pattern(regexp = "^[0-9]{3}$", message = "CVV must be 3 digits")
    private String cvv;
    
    @NotBlank(message = "Provider is required")
    @Size(max = 50, message = "Provider name must not exceed 50 characters")
    private String provider;
    
    private String status; // ACTIVE, BLOCKED, EXPIRED
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime issuedAt;
    
    // User information (populated from related user)
    private String userEmail;
    private String userPhone;
    private String userName;
    
    // Additional fields for enhanced functionality
    private String cardType; // VISA, MASTERCARD, etc.
    private String currency;
    private BigDecimal balance;
    private BigDecimal spendingLimit;
    private BigDecimal dailyLimit;
    private BigDecimal monthlyLimit;
    private Boolean isInternationalEnabled;
    private Boolean isOnlineEnabled;
    private Boolean isAtmEnabled;
    private String cardHolderName;
    private String maskedCardNumber; // For display purposes
    private String lastFourDigits;
    private String brand; // VISA, MASTERCARD, etc.
    private String fundingSource;
    private String cardDesign;
    private String pinStatus; // SET, NOT_SET, BLOCKED
    private LocalDateTime lastUsedAt;
    private Integer usageCount;
    private String metadata;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // Helper method to get masked card number
    public String getMaskedCardNumber() {
        if (cardNumber != null && cardNumber.length() >= 4) {
            return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
        }
        return cardNumber;
    }
    
    // Helper method to get last four digits
    public String getLastFourDigits() {
        if (cardNumber != null && cardNumber.length() >= 4) {
            return cardNumber.substring(cardNumber.length() - 4);
        }
        return null;
    }
}
