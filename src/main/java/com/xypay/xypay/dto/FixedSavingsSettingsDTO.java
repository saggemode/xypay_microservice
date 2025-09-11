package com.xypay.xypay.dto;

import java.time.LocalDateTime;

public class FixedSavingsSettingsDTO {
    private Long id;
    private String user;
    private Boolean maturityNotifications;
    private Boolean interestNotifications;
    private Boolean autoRenewalNotifications;
    private Boolean defaultAutoRenewal;
    private Integer defaultRenewalDuration;
    private String defaultSource;
    private String defaultSourceDisplay;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
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

    public String getDefaultSource() {
        return defaultSource;
    }

    public void setDefaultSource(String defaultSource) {
        this.defaultSource = defaultSource;
    }

    public String getDefaultSourceDisplay() {
        return defaultSourceDisplay;
    }

    public void setDefaultSourceDisplay(String defaultSourceDisplay) {
        this.defaultSourceDisplay = defaultSourceDisplay;
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
}