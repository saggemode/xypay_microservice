package com.xypay.xypay.dto;

import com.xypay.xypay.enums.FraudFlag;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class FraudDetectionDTO {
    private UUID id;
    private UUID userId;
    private UUID transferId;
    private String fraudType;
    private Integer riskScore;
    private FraudFlag flag;
    private String description;
    private Boolean isResolved;
    private UUID resolvedBy;
    private LocalDateTime resolvedAt;
    private String resolutionNotes;
    private LocalDateTime createdAt;
    
    // Nested DTOs
    private UserDTO user;
    private BankTransferDTO transfer;
    private UserDTO resolvedByUser;
}
