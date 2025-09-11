package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "bill_payments")
public class BillPayment extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "service_type", length = 50)
    private String serviceType;
    
    @Column(name = "account_or_meter", length = 50)
    private String accountOrMeter;
    
    @Column(name = "amount", precision = 19, scale = 4)
    private BigDecimal amount = BigDecimal.ZERO;
    
    @Column(name = "status", length = 10)
    private String status; // SUCCESS, FAILED, PENDING
    
    @Column(name = "reference", unique = true)
    private String reference;
    
    @CreationTimestamp
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    
    // Constructors
    public BillPayment() {}
    
    // Getters and Setters
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getServiceType() {
        return serviceType;
    }
    
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
    
    public String getAccountOrMeter() {
        return accountOrMeter;
    }
    
    public void setAccountOrMeter(String accountOrMeter) {
        this.accountOrMeter = accountOrMeter;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getReference() {
        return reference;
    }
    
    public void setReference(String reference) {
        this.reference = reference;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}