package com.xypay.xypay.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Entity for storing system-wide configuration parameters
 * Used for dynamic configuration management in enterprise banking systems
 */
@Entity
@Table(name = "system_configuration", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"config_key"}),
       indexes = {
           @Index(name = "idx_config_key", columnList = "config_key"),
           @Index(name = "idx_config_category", columnList = "category"),
           @Index(name = "idx_config_active", columnList = "is_active")
       })
public class SystemConfiguration extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Configuration key is required")
    @Size(max = 255, message = "Configuration key must not exceed 255 characters")
    @Column(name = "config_key", nullable = false, unique = true)
    private String configKey;

    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;

    @Size(max = 100, message = "Category must not exceed 100 characters")
    @Column(name = "category", length = 100)
    private String category;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_encrypted", nullable = false)
    private Boolean isEncrypted = false;

    @Column(name = "default_value", columnDefinition = "TEXT")
    private String defaultValue;

    @Size(max = 50, message = "Data type must not exceed 50 characters")
    @Column(name = "data_type", length = 50)
    private String dataType = "STRING";

    @Column(name = "is_system_config", nullable = false)
    private Boolean isSystemConfig = false;

    @Column(name = "requires_restart", nullable = false)
    private Boolean requiresRestart = false;

    @Size(max = 100, message = "Updated by must not exceed 100 characters")
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    // Constructors
    public SystemConfiguration() {}

    public SystemConfiguration(String configKey, String configValue) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.lastUpdated = LocalDateTime.now();
    }

    public SystemConfiguration(String configKey, String configValue, String category, String description) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.category = category;
        this.description = description;
        this.lastUpdated = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
        this.lastUpdated = LocalDateTime.now();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsEncrypted() {
        return isEncrypted;
    }

    public void setIsEncrypted(Boolean isEncrypted) {
        this.isEncrypted = isEncrypted;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Boolean getIsSystemConfig() {
        return isSystemConfig;
    }

    public void setIsSystemConfig(Boolean isSystemConfig) {
        this.isSystemConfig = isSystemConfig;
    }

    public Boolean getRequiresRestart() {
        return requiresRestart;
    }

    public void setRequiresRestart(Boolean requiresRestart) {
        this.requiresRestart = requiresRestart;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    // Utility methods
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    public boolean isEncrypted() {
        return Boolean.TRUE.equals(isEncrypted);
    }

    public boolean isSystemConfig() {
        return Boolean.TRUE.equals(isSystemConfig);
    }

    public boolean requiresRestart() {
        return Boolean.TRUE.equals(requiresRestart);
    }

    @PrePersist
    @PreUpdate
    protected void updateTimestamp() {
        this.lastUpdated = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "SystemConfiguration{" +
                "id=" + id +
                ", configKey='" + configKey + '\'' +
                ", category='" + category + '\'' +
                ", isActive=" + isActive +
                ", dataType='" + dataType + '\'' +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
