package com.xypay.xypay.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class StaffRoleDTO {
    private UUID id;
    private String name;
    private Integer level;
    private String description;
    private BigDecimal maxTransactionApproval;
    private Boolean canApproveKyc;
    private Boolean canManageStaff;
    private Boolean canViewReports;
    private Boolean canOverrideTransactions;
    private Boolean canHandleEscalations;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
