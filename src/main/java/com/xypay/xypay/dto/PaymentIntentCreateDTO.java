package com.xypay.xypay.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PaymentIntentCreateDTO {
    @NotBlank(message = "Order ID is required")
    private String orderId;
    
    @NotNull(message = "Merchant ID is required")
    private UUID merchantId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    private String currency;
    
    private String description;
    
    private String metadata;
}
