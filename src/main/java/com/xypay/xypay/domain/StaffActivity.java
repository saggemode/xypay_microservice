package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "staff_activities", indexes = {
    @Index(name = "idx_staff_activity_staff", columnList = "staff_id"),
    @Index(name = "idx_staff_activity_type", columnList = "activity_type"),
    @Index(name = "idx_staff_activity_timestamp", columnList = "timestamp")
})
public class StaffActivity extends BaseEntity {
    
    public enum ActivityType {
        TRANSACTION_PROCESSED("transaction_processed", "Transaction Processed"),
        KYC_APPROVED("kyc_approved", "KYC Approved"),
        ESCALATION_HANDLED("escalation_handled", "Escalation Handled"),
        CUSTOMER_SERVED("customer_served", "Customer Served"),
        REPORT_GENERATED("report_generated", "Report Generated"),
        STAFF_MANAGED("staff_managed", "Staff Managed");
        
        private final String code;
        private final String displayName;
        
        ActivityType(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private StaffProfile staff;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", length = 50, nullable = false)
    private ActivityType activityType;
    
    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;
    
    @Column(name = "related_object_id")
    private Long relatedObjectId;
    
    @Column(name = "related_object_type", length = 100)
    private String relatedObjectType;
    
    @Column(name = "timestamp", nullable = false)
    private java.time.LocalDateTime timestamp;
    
    // Constructors
    public StaffActivity() {
        this.timestamp = java.time.LocalDateTime.now();
    }
    
    public StaffActivity(StaffProfile staff, ActivityType activityType, String description) {
        this();
        this.staff = staff;
        this.activityType = activityType;
        this.description = description;
    }
    
    public StaffActivity(StaffProfile staff, ActivityType activityType, String description, 
                        Long relatedObjectId, String relatedObjectType) {
        this(staff, activityType, description);
        this.relatedObjectId = relatedObjectId;
        this.relatedObjectType = relatedObjectType;
    }
}