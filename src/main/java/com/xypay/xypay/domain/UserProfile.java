package com.xypay.xypay.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
public class UserProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "phone", unique = true)
    private String phone;
    
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;
    
    @Column(name = "otp_code", length = 6)
    private String otpCode;
    
    @Column(name = "otp_expiry")
    private LocalDateTime otpExpiry;
    
    @Column(name = "email_verification_token")
    private String emailVerificationToken;
    
    @Column(name = "email_verification_token_expiry")
    private LocalDateTime emailVerificationTokenExpiry;
    
    @Column(name = "phone_verification_token")
    private String phoneVerificationToken;
    
    @Column(name = "phone_verification_token_expiry")
    private LocalDateTime phoneVerificationTokenExpiry;
    
    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;
    
    @Column(name = "notify_email", nullable = false)
    private Boolean notifyEmail = true;
    
    @Column(name = "notify_sms", nullable = false)
    private Boolean notifySms = false;
    
    @Column(name = "notify_push", nullable = false)
    private Boolean notifyPush = false;
    
    @Column(name = "notify_in_app", nullable = false)
    private Boolean notifyInApp = true;
    
    @Column(name = "fcm_token")
    private String fcmToken;
    
    @Column(name = "transaction_pin")
    private String transactionPin;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public UserProfile() {}
    
    public UserProfile(User user) {
        this.user = user;
        this.isVerified = false;
        this.notifyEmail = true;
        this.notifySms = false;
        this.notifyPush = false;
        this.notifyInApp = true;
    }
    
    // Transaction PIN methods
    public void setTransactionPinHashed(String rawPin) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        this.transactionPin = encoder.encode(rawPin);
    }
    
    public boolean checkTransactionPin(String rawPin) {
        if (this.transactionPin == null) {
            return false;
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(rawPin, this.transactionPin);
    }
    
    // OTP methods
    public boolean isOtpValid(String code) {
        if (otpCode == null || otpExpiry == null) {
            return false;
        }
        return otpCode.equals(code) && LocalDateTime.now().isBefore(otpExpiry);
    }

    public boolean isOtpExpired() {
        return otpExpiry != null && LocalDateTime.now().isAfter(otpExpiry);
    }

    public void clearOtp() {
        this.otpCode = null;
        this.otpExpiry = null;
    }

    // Email verification methods
    public boolean isEmailVerificationTokenValid(String token) {
        if (emailVerificationToken == null || emailVerificationTokenExpiry == null) {
            return false;
        }
        return emailVerificationToken.equals(token) && LocalDateTime.now().isBefore(emailVerificationTokenExpiry);
    }

    public void setEmailVerificationToken(String token, int expiryHours) {
        this.emailVerificationToken = token;
        this.emailVerificationTokenExpiry = LocalDateTime.now().plusHours(expiryHours);
    }

    public void verifyEmail() {
        this.emailVerified = true;
        this.emailVerificationToken = null;
        this.emailVerificationTokenExpiry = null;
    }

    public void clearEmailVerificationToken() {
        this.emailVerificationToken = null;
        this.emailVerificationTokenExpiry = null;
    }

    public void generateEmailVerificationToken() {
        this.emailVerificationToken = UUID.randomUUID().toString();
        this.emailVerificationTokenExpiry = LocalDateTime.now().plusHours(24); // Token expires in 24 hours
    }

    // Phone verification token methods
    public boolean isPhoneVerificationTokenValid(String token) {
        if (phoneVerificationToken == null || phoneVerificationTokenExpiry == null) {
            return false;
        }
        return phoneVerificationToken.equals(token) && LocalDateTime.now().isBefore(phoneVerificationTokenExpiry);
    }

    public void setPhoneVerificationToken(String token, int expiryMinutes) {
        this.phoneVerificationToken = token;
        this.phoneVerificationTokenExpiry = LocalDateTime.now().plusMinutes(expiryMinutes);
    }

    public void generatePhoneVerificationToken() {
        this.phoneVerificationToken = UUID.randomUUID().toString();
        this.phoneVerificationTokenExpiry = LocalDateTime.now().plusMinutes(30); // Token expires in 30 minutes
    }

    public void clearPhoneVerificationToken() {
        this.phoneVerificationToken = null;
        this.phoneVerificationTokenExpiry = null;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }

    public LocalDateTime getOtpExpiry() {
        return otpExpiry;
    }

    public void setOtpExpiry(LocalDateTime otpExpiry) {
        this.otpExpiry = otpExpiry;
    }

    public String getEmailVerificationToken() {
        return emailVerificationToken;
    }

    public void setEmailVerificationToken(String emailVerificationToken) {
        this.emailVerificationToken = emailVerificationToken;
    }

    public LocalDateTime getEmailVerificationTokenExpiry() {
        return emailVerificationTokenExpiry;
    }

    public void setEmailVerificationTokenExpiry(LocalDateTime emailVerificationTokenExpiry) {
        this.emailVerificationTokenExpiry = emailVerificationTokenExpiry;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public Boolean getNotifyEmail() {
        return notifyEmail;
    }

    public void setNotifyEmail(Boolean notifyEmail) {
        this.notifyEmail = notifyEmail;
    }

    public Boolean getNotifySms() {
        return notifySms;
    }

    public void setNotifySms(Boolean notifySms) {
        this.notifySms = notifySms;
    }

    public Boolean getNotifyPush() {
        return notifyPush;
    }

    public void setNotifyPush(Boolean notifyPush) {
        this.notifyPush = notifyPush;
    }

    public Boolean getNotifyInApp() {
        return notifyInApp;
    }

    public void setNotifyInApp(Boolean notifyInApp) {
        this.notifyInApp = notifyInApp;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getTransactionPin() {
        return transactionPin;
    }

    public void setTransactionPin(String transactionPin) {
        this.transactionPin = transactionPin;
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

    public String getPhoneVerificationToken() {
        return phoneVerificationToken;
    }

    public void setPhoneVerificationToken(String phoneVerificationToken) {
        this.phoneVerificationToken = phoneVerificationToken;
    }

    public LocalDateTime getPhoneVerificationTokenExpiry() {
        return phoneVerificationTokenExpiry;
    }

    public void setPhoneVerificationTokenExpiry(LocalDateTime phoneVerificationTokenExpiry) {
        this.phoneVerificationTokenExpiry = phoneVerificationTokenExpiry;
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "id=" + id +
                ", phone='" + phone + '\'' +
                ", isVerified=" + isVerified +
                '}';
    }
}