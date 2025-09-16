package com.xypay.xypay.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class StaffProfileDTO {
    private UUID id;
    private UUID userId;
    private UUID roleId;
    private String employeeId;
    private String branch;
    private String department;
    private UUID supervisorId;
    private Boolean isActive;
    private LocalDate hireDate;
    private LocalDate lastReviewDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Nested DTOs
    private UserDTO user;
    private StaffRoleDTO role;
    private StaffProfileDTO supervisor;
}
