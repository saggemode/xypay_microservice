package com.xypay.xypay.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class FixedSavingsAccountDetailDTO {
    
    private UUID id;
    private String user;
    private String userId;
    private String accountNumber;
    private String amount;
    private String source;
    private String sourceDisplay;
    private String purpose;
    private String purposeDisplay;
    private String purposeDescription;
    private LocalDate startDate;
    private LocalDate paybackDate;
    private Boolean autoRenewalEnabled;
    private Boolean isActive;
    private Boolean isMatured;
    private Boolean isPaidOut;
    private BigDecimal interestRate;
    private String totalInterestEarned;
    private String maturityAmount;
    private Integer durationDays;
    private Integer daysRemaining;
    private Boolean isMature;
    private Boolean canBePaidOut;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime maturedAt;
    private LocalDateTime paidOutAt;
    private List<FixedSavingsTransactionDTO> transactions;
}