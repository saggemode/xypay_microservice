package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "virtual_cards")
public class VirtualCard extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "card_number", length = 16)
    private String cardNumber;
    
    @Column(name = "expiry", length = 5)
    private String expiry;
    
    @Column(name = "cvv", length = 3)
    private String cvv;
    
    @Column(name = "provider", length = 50)
    private String provider;
    
    @Column(name = "status", length = 20)
    private String status; // ACTIVE, BLOCKED
    
    @CreationTimestamp
    @Column(name = "issued_at")
    private LocalDateTime issuedAt;
    
    // Constructors
    public VirtualCard() {}
    
    // Getters and Setters
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getCardNumber() {
        return cardNumber;
    }
    
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
    
    public String getExpiry() {
        return expiry;
    }
    
    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }
    
    public String getCvv() {
        return cvv;
    }
    
    public void setCvv(String cvv) {
        this.cvv = cvv;
    }
    
    public String getProvider() {
        return provider;
    }
    
    public void setProvider(String provider) {
        this.provider = provider;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }
    
    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }
}