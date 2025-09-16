package com.xypay.xypay.domain;

public enum TransferStatus {
    PENDING("pending", "Pending"),
    PROCESSING("processing", "Processing"),
    COMPLETED("completed", "Completed"),
    FAILED("failed", "Failed"),
    CANCELLED("cancelled", "Cancelled"),
    REVERSED("reversed", "Reversed"),
    APPROVAL_REQUIRED("approval_required", "Approval Required"),
    APPROVED("approved", "Approved"),
    REJECTED("rejected", "Rejected");
    
    private final String code;
    private final String description;
    
    TransferStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
}
