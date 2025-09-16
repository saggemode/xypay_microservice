package com.xypay.xypay.dto;

import com.xypay.xypay.enums.SecurityLevel;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SecurityAlertDTO {
    private UUID id;
    private UUID userId;
    private String alertType;
    private SecurityLevel severity;
    private String title;
    private String message;
    private String ipAddress;
    private String userAgent;
    private String location;
    private Boolean isRead;
    private Boolean isResolved;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
    
    // Nested DTOs
    private UserDTO user;
}
