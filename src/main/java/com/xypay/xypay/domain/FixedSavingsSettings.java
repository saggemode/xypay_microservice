package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "fixed_savings_settings")
public class FixedSavingsSettings {
    
    public enum Source {
        WALLET, XYSAVE, BOTH
    }
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(name = "maturity_notifications", nullable = false)
    private Boolean maturityNotifications = true;
    
    @Column(name = "interest_notifications", nullable = false)
    private Boolean interestNotifications = true;
    
    @Column(name = "auto_renewal_notifications", nullable = false)
    private Boolean autoRenewalNotifications = true;
    
    @Column(name = "default_auto_renewal", nullable = false)
    private Boolean defaultAutoRenewal = false;
    
    @Column(name = "default_renewal_duration", nullable = false)
    private Integer defaultRenewalDuration = 30;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "default_source", length = 10, nullable = false)
    private Source defaultSource = Source.WALLET;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public FixedSavingsSettings() {}
    
    // Getters and Setters
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Boolean getMaturityNotifications() {
        return maturityNotifications;
    }
    
    public void setMaturityNotifications(Boolean maturityNotifications) {
        this.maturityNotifications = maturityNotifications;
    }
    
    public Boolean getInterestNotifications() {
        return interestNotifications;
    }
    
    public void setInterestNotifications(Boolean interestNotifications) {
        this.interestNotifications = interestNotifications;
    }
    
    public Boolean getAutoRenewalNotifications() {
        return autoRenewalNotifications;
    }
    
    public void setAutoRenewalNotifications(Boolean autoRenewalNotifications) {
        this.autoRenewalNotifications = autoRenewalNotifications;
    }
    
    public Boolean getDefaultAutoRenewal() {
        return defaultAutoRenewal;
    }
    
    public void setDefaultAutoRenewal(Boolean defaultAutoRenewal) {
        this.defaultAutoRenewal = defaultAutoRenewal;
    }
    
    public Integer getDefaultRenewalDuration() {
        return defaultRenewalDuration;
    }
    
    public void setDefaultRenewalDuration(Integer defaultRenewalDuration) {
        this.defaultRenewalDuration = defaultRenewalDuration;
    }
    
    public Source getDefaultSource() {
        return defaultSource;
    }
    
    public void setDefaultSource(Source defaultSource) {
        this.defaultSource = defaultSource;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}