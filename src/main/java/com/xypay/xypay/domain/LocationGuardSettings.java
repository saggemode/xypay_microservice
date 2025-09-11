package com.xypay.xypay.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "location_guard_settings")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LocationGuardSettings extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = false;

    @Column(name = "allowed_states", columnDefinition = "TEXT")
    private String allowedStates = "[]";

    @Column(name = "face_template_hash", length = 128)
    private String faceTemplateHash;

    @Column(name = "face_template_alg", length = 20)
    private String faceTemplateAlg = "sha256";

    @Column(name = "face_registered_at")
    private LocalDateTime faceRegisteredAt;

    // Constructors
    public LocationGuardSettings() {}

    public LocationGuardSettings(User user) {
        this.user = user;
    }

    // Business methods
    public void enableLocationGuard(List<String> allowedStates) {
        this.enabled = true;
        // Convert list to JSON string - in real implementation use ObjectMapper
        this.allowedStates = allowedStates.toString();
    }

    public void disableLocationGuard() {
        this.enabled = false;
    }

    public void registerFace(String faceTemplateHash) {
        this.faceTemplateHash = faceTemplateHash;
        this.faceRegisteredAt = LocalDateTime.now();
    }

    public boolean requiresVerification(String currentState) {
        if (!enabled || allowedStates == null) {
            return false;
        }
        
        // Simple check - in real implementation parse JSON properly
        return !allowedStates.contains(currentState);
    }

    public boolean hasFaceRegistered() {
        return faceTemplateHash != null && faceRegisteredAt != null;
    }

    @Override
    public String toString() {
        return String.format("LocationGuardSettings{user=%s, enabled=%s}", 
            user != null ? user.getUsername() : "null", enabled);
    }
}
