package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@Entity
@Table(name = "channels")
@EqualsAndHashCode(callSuper = true)
public class Channel extends BaseEntity {
    
    
    
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
    
    
    @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL)
    private List<ChannelTransaction> transactions;
    
    // Getters and Setters
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
}