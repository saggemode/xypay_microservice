package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "channels")
public class Channel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    @Column(name = "channel_type")
    private String channelType; // ATM, INTERNET_BANKING, MOBILE, BRANCH, API
    
    @Column(name = "branch_id")
    private String branchId;
    
    private String status; // ACTIVE, INACTIVE, MAINTENANCE
    
    @Column(name = "endpoint_url")
    private String endpointUrl;
    
    @Column(name = "api_key")
    private String apiKey;
    
    @Column(name = "configuration")
    @Lob
    private String configuration; // JSON configuration for channel-specific settings
    
    @Column(name = "max_transaction_limit")
    private java.math.BigDecimal maxTransactionLimit;
    
    @Column(name = "daily_limit")
    private java.math.BigDecimal dailyLimit;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL)
    private List<ChannelTransaction> transactions;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getChannelType() {
        return channelType;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}