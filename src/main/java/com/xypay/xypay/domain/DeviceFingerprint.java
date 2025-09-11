package com.xypay.xypay.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "device_fingerprints", indexes = {
    @Index(name = "idx_device_user_trusted", columnList = "user_id, is_trusted"),
    @Index(name = "idx_device_device_id", columnList = "device_id")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DeviceFingerprint extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "device_id", length = 255, unique = true, nullable = false)
    private String deviceId;

    @Column(name = "device_type", length = 20, nullable = false)
    private String deviceType; // MOBILE, DESKTOP, TABLET

    @Column(name = "user_agent", columnDefinition = "TEXT", nullable = false)
    private String userAgent;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "is_trusted", nullable = false)
    private Boolean isTrusted = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "last_used", nullable = false)
    private LocalDateTime lastUsed = LocalDateTime.now();

    // Constructors
    public DeviceFingerprint() {}

    public DeviceFingerprint(User user, String deviceId, String deviceType, String userAgent, String ipAddress) {
        this.user = user;
        this.deviceId = deviceId;
        this.deviceType = deviceType;
        this.userAgent = userAgent;
        this.ipAddress = ipAddress;
    }

    // Business methods
    public void trust() {
        this.isTrusted = true;
    }

    public void untrust() {
        this.isTrusted = false;
    }

    public void updateLastUsed() {
        this.lastUsed = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
    }

    @Override
    public String toString() {
        return String.format("DeviceFingerprint{user=%s, deviceType='%s', trusted=%s}", 
            user != null ? user.getUsername() : "null", deviceType, isTrusted);
    }
}
