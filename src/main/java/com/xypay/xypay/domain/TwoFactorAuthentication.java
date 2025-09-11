package com.xypay.xypay.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "two_factor_authentication", indexes = {
    @Index(name = "idx_2fa_user_type_used", columnList = "user_id, token_type, is_used"),
    @Index(name = "idx_2fa_expires_at", columnList = "expires_at")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TwoFactorAuthentication extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token", length = 6, nullable = false)
    private String token;

    @Column(name = "token_type", length = 20, nullable = false)
    private String tokenType; // sms, email, totp, push

    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    // Constructors
    public TwoFactorAuthentication() {}

    public TwoFactorAuthentication(User user, String token, String tokenType, LocalDateTime expiresAt) {
        this.user = user;
        this.token = token;
        this.tokenType = tokenType;
        this.expiresAt = expiresAt;
    }

    // Business methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !isUsed && !isExpired();
    }

    public void markAsUsed() {
        this.isUsed = true;
        this.usedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("TwoFactorAuthentication{user=%s, tokenType='%s', isUsed=%s, expired=%s}", 
            user != null ? user.getUsername() : "null", tokenType, isUsed, isExpired());
    }
}
