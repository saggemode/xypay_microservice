package com.xypay.xypay.dto;

import com.xypay.xypay.domain.KYCProfile;
import com.xypay.xypay.domain.UserProfile;
import java.time.LocalDateTime;
import java.util.List;

public class UserProfileResponseDTO {
    
    private String id;
    private String username;
    private String email;
    private String phone;
    private String accountNumber;
    private Boolean isVerified;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Nested objects
    private KYCResponseDTO kyc;
    private WalletResponseDTO wallet;
    private List<TransactionResponseDTO> transactions;
    private String transactionPin; // Always "***" for security
    private List<NotificationResponseDTO> notifications;
    
    // Constructors
    public UserProfileResponseDTO() {}
    
    public UserProfileResponseDTO(UserProfile profile) {
        this.id = profile.getId().toString();
        this.username = profile.getUser().getUsername();
        this.email = profile.getUser().getEmail();
        this.phone = profile.getPhone();
        // Account number comes from the user's wallet, not the profile
        this.accountNumber = null; // Will be set separately when wallet data is available
        this.isVerified = profile.getIsVerified();
        this.enabled = profile.getUser().isEnabled();
        this.createdAt = profile.getCreatedAt();
        this.updatedAt = profile.getUpdatedAt();
        this.transactionPin = profile.getTransactionPin() != null ? "***" : null;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    
    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }
    
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public KYCResponseDTO getKyc() { return kyc; }
    public void setKyc(KYCResponseDTO kyc) { this.kyc = kyc; }
    
    public WalletResponseDTO getWallet() { return wallet; }
    public void setWallet(WalletResponseDTO wallet) { this.wallet = wallet; }
    
    public List<TransactionResponseDTO> getTransactions() { return transactions; }
    public void setTransactions(List<TransactionResponseDTO> transactions) { this.transactions = transactions; }
    
    public String getTransactionPin() { return transactionPin; }
    public void setTransactionPin(String transactionPin) { this.transactionPin = transactionPin; }
    
    public List<NotificationResponseDTO> getNotifications() { return notifications; }
    public void setNotifications(List<NotificationResponseDTO> notifications) { this.notifications = notifications; }
    
    // Nested DTOs
    public static class KYCResponseDTO {
        private String id;
        private String kycLevel;
        private String kycLevelDisplay;
        private Boolean isApproved;
        private String bvn;
        private String nin;
        private String dateOfBirth;
        private String address;
        private String state;
        private String gender;
        private Double dailyTransactionLimit;
        private Double maxBalanceLimit;
        private LocalDateTime createdAt;
        
        // Constructors, getters, setters
        public KYCResponseDTO() {}
        
        public KYCResponseDTO(KYCProfile kyc) {
            this.id = kyc.getId().toString();
            this.kycLevel = kyc.getKycLevel().getValue();
            this.kycLevelDisplay = kyc.getKycLevel().getDisplayName();
            this.isApproved = kyc.getIsApproved();
            this.bvn = kyc.getBvn();
            this.nin = kyc.getNin();
            this.dateOfBirth = kyc.getDateOfBirth() != null ? kyc.getDateOfBirth().toString() : null;
            this.address = kyc.getAddress();
            this.state = kyc.getState();
            this.gender = kyc.getGender() != null ? kyc.getGender().getValue() : null;
            this.dailyTransactionLimit = kyc.getDailyTransactionLimit();
            this.maxBalanceLimit = kyc.getMaxBalanceLimit();
            this.createdAt = kyc.getCreatedAt();
        }
        
        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getKycLevel() { return kycLevel; }
        public void setKycLevel(String kycLevel) { this.kycLevel = kycLevel; }
        
        public String getKycLevelDisplay() { return kycLevelDisplay; }
        public void setKycLevelDisplay(String kycLevelDisplay) { this.kycLevelDisplay = kycLevelDisplay; }
        
        public Boolean getIsApproved() { return isApproved; }
        public void setIsApproved(Boolean isApproved) { this.isApproved = isApproved; }
        
        public String getBvn() { return bvn; }
        public void setBvn(String bvn) { this.bvn = bvn; }
        
        public String getNin() { return nin; }
        public void setNin(String nin) { this.nin = nin; }
        
        public String getDateOfBirth() { return dateOfBirth; }
        public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        
        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }
        
        public Double getDailyTransactionLimit() { return dailyTransactionLimit; }
        public void setDailyTransactionLimit(Double dailyTransactionLimit) { this.dailyTransactionLimit = dailyTransactionLimit; }
        
        public Double getMaxBalanceLimit() { return maxBalanceLimit; }
        public void setMaxBalanceLimit(Double maxBalanceLimit) { this.maxBalanceLimit = maxBalanceLimit; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
    
    public static class WalletResponseDTO {
        private Long id;
        private String accountNumber;
        private String alternativeAccountNumber;
        private String balance;
        private String currency;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        // Constructors, getters, setters
        public WalletResponseDTO() {}
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
        
        public String getAlternativeAccountNumber() { return alternativeAccountNumber; }
        public void setAlternativeAccountNumber(String alternativeAccountNumber) { this.alternativeAccountNumber = alternativeAccountNumber; }
        
        public String getBalance() { return balance; }
        public void setBalance(String balance) { this.balance = balance; }
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }
    
    public static class TransactionResponseDTO {
        private Long id;
        private String reference;
        private String amount;
        private String transactionType;
        private String status;
        private String description;
        private LocalDateTime timestamp;
        
        // Constructors, getters, setters
        public TransactionResponseDTO() {}
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getReference() { return reference; }
        public void setReference(String reference) { this.reference = reference; }
        
        public String getAmount() { return amount; }
        public void setAmount(String amount) { this.amount = amount; }
        
        public String getTransactionType() { return transactionType; }
        public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
    
    public static class NotificationResponseDTO {
        private String id;
        private String title;
        private String message;
        private String notificationType;
        private String level;
        private String status;
        private Boolean isRead;
        private String actionText;
        private String actionUrl;
        private String link;
        private String priority;
        private String source;
        private String createdAt;
        private String readAt;
        
        // Constructors, getters, setters
        public NotificationResponseDTO() {}
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getNotificationType() { return notificationType; }
        public void setNotificationType(String notificationType) { this.notificationType = notificationType; }
        
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public Boolean getIsRead() { return isRead; }
        public void setIsRead(Boolean isRead) { this.isRead = isRead; }
        
        public String getActionText() { return actionText; }
        public void setActionText(String actionText) { this.actionText = actionText; }
        
        public String getActionUrl() { return actionUrl; }
        public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }
        
        public String getLink() { return link; }
        public void setLink(String link) { this.link = link; }
        
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        
        public String getReadAt() { return readAt; }
        public void setReadAt(String readAt) { this.readAt = readAt; }
    }
}