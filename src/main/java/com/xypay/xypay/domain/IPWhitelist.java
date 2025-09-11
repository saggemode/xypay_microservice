package com.xypay.xypay.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ip_whitelist", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "ip_address"}),
    indexes = {
        @Index(name = "idx_ip_whitelist_user_status", columnList = "user_id, status"),
        @Index(name = "idx_ip_whitelist_ip_address", columnList = "ip_address")
    })
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class IPWhitelist extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    // Constructors
    public IPWhitelist() {}

    public IPWhitelist(User user, String ipAddress, String description) {
        this.user = user;
        this.ipAddress = ipAddress;
        this.description = description;
    }

    // Business methods
    public void approve(User approver) {
        this.status = "APPROVED";
        this.approvedBy = approver;
        this.approvedAt = LocalDateTime.now();
    }

    public void reject() {
        this.status = "REJECTED";
        this.isActive = false;
    }

    public boolean isApproved() {
        return "APPROVED".equals(status) && isActive;
    }

    @Override
    public String toString() {
        return String.format("IPWhitelist{user=%s, ipAddress='%s', status='%s'}", 
            user != null ? user.getUsername() : "null", ipAddress, status);
    }
}
