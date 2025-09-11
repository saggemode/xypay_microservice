package com.xypay.xypay.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "night_guard_settings")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class NightGuardSettings extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = false;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "primary_method", length = 20, nullable = false)
    private String primaryMethod = "face";

    @Column(name = "fallback_method", length = 20, nullable = false)
    private String fallbackMethod = "2fa";

    @Column(name = "applies_to", length = 20, nullable = false)
    private String appliesTo = "app_only";

    @Column(name = "face_template_hash", length = 128)
    private String faceTemplateHash;

    @Column(name = "face_template_alg", length = 20)
    private String faceTemplateAlg = "sha256";

    @Column(name = "face_registered_at")
    private LocalDateTime faceRegisteredAt;

    // Constructors
    public NightGuardSettings() {}

    public NightGuardSettings(User user) {
        this.user = user;
    }

    // Business methods
    public void enableNightGuard(LocalTime startTime, LocalTime endTime) {
        this.enabled = true;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void disableNightGuard() {
        this.enabled = false;
    }

    public void registerFace(String faceTemplateHash) {
        this.faceTemplateHash = faceTemplateHash;
        this.faceRegisteredAt = LocalDateTime.now();
    }

    public boolean isActiveNow() {
        if (!enabled || startTime == null || endTime == null) {
            return false;
        }
        
        LocalTime now = LocalTime.now();
        if (startTime.isBefore(endTime)) {
            return now.isAfter(startTime) && now.isBefore(endTime);
        } else {
            // Spans midnight
            return now.isAfter(startTime) || now.isBefore(endTime);
        }
    }

    public boolean hasFaceRegistered() {
        return faceTemplateHash != null && faceRegisteredAt != null;
    }

    @Override
    public String toString() {
        return String.format("NightGuardSettings{user=%s, enabled=%s, active=%s}", 
            user != null ? user.getUsername() : "null", enabled, isActiveNow());
    }
}
