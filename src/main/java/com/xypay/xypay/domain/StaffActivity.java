package com.xypay.xypay.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;


/**
 * Staff activity entity for logging staff actions.
 * Equivalent to Django's StaffActivity model.
 */
@Entity
@Table(name = "staff_activities")
public class StaffActivity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private StaffProfile staff;
    
    @Column(name = "activity_type", nullable = false)
    private String activityType;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "related_object_id")
    private String relatedObjectId;
    
    @Column(name = "related_object_type")
    private String relatedObjectType;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public StaffActivity() {
        this.createdAt = LocalDateTime.now();
    }
    
    public StaffActivity(StaffProfile staff, String activityType, String description) {
        this();
        this.staff = staff;
        this.activityType = activityType;
        this.description = description;
    }
    
    public StaffActivity(StaffProfile staff, String activityType, String description, Object relatedObject) {
        this(staff, activityType, description);
        if (relatedObject != null) {
            this.relatedObjectType = relatedObject.getClass().getSimpleName();
            if (relatedObject instanceof KYCProfile) {
                this.relatedObjectId = ((KYCProfile) relatedObject).getId().toString();
            }
        }
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public StaffProfile getStaff() {
        return staff;
    }
    
    public void setStaff(StaffProfile staff) {
        this.staff = staff;
    }
    
    public String getActivityType() {
        return activityType;
    }
    
    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getRelatedObjectId() {
        return relatedObjectId;
    }
    
    public void setRelatedObjectId(String relatedObjectId) {
        this.relatedObjectId = relatedObjectId;
    }
    
    public String getRelatedObjectType() {
        return relatedObjectType;
    }
    
    public void setRelatedObjectType(String relatedObjectType) {
        this.relatedObjectType = relatedObjectType;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
