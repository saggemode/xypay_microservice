package com.xypay.xypay.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // Order reference - using orderId string instead of Order entity
    @Column(name = "order_id")
    private String orderId;

    // Banking references
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_transfer_id")
    private BankTransfer bankTransfer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_payment_id")
    private BillPayment billPayment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "virtual_card_id")
    private VirtualCard virtualCard;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", length = 35)
    private NotificationType notificationType = NotificationType.OTHER;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", length = 10)
    private NotificationLevel level = NotificationLevel.INFO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(name = "is_read")
    private boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "action_text", length = 64)
    private String actionText;

    @Column(name = "action_url", length = 500)
    private String actionUrl;

    @Column(name = "link", length = 500)
    private String link;

    @Column(name = "priority")
    private Integer priority = 0;

    @Column(name = "source", length = 64)
    private String source;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extra_data")
    private JsonNode extraData;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Getters and setters

    public User getRecipient() {
        return recipient;
    }

    public void setRecipient(User recipient) {
        this.recipient = recipient;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public BankTransfer getBankTransfer() {
        return bankTransfer;
    }

    public void setBankTransfer(BankTransfer bankTransfer) {
        this.bankTransfer = bankTransfer;
    }

    public BillPayment getBillPayment() {
        return billPayment;
    }

    public void setBillPayment(BillPayment billPayment) {
        this.billPayment = billPayment;
    }

    public VirtualCard getVirtualCard() {
        return virtualCard;
    }

    public void setVirtualCard(VirtualCard virtualCard) {
        this.virtualCard = virtualCard;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public NotificationLevel getLevel() {
        return level;
    }

    public void setLevel(NotificationLevel level) {
        this.level = level;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public String getActionText() {
        return actionText;
    }

    public void setActionText(String actionText) {
        this.actionText = actionText;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public JsonNode getExtraData() {
        return extraData;
    }

    public void setExtraData(JsonNode extraData) {
        this.extraData = extraData;
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

    /**
     * Mark notification as read
     */
    public void markAsRead() {
        if (!this.isRead) {
            this.isRead = true;
            this.readAt = LocalDateTime.now();
            this.status = NotificationStatus.READ;
        }
    }

    /**
     * Mark notification as unread
     */
    public void markAsUnread() {
        if (this.isRead) {
            this.isRead = false;
            this.readAt = null;
            this.status = NotificationStatus.DELIVERED;
        }
    }

    /**
     * Check if notification is actionable (has both action text and URL)
     */
    public boolean isActionable() {
        return (this.actionText != null && !this.actionText.isEmpty()) && 
               (this.actionUrl != null && !this.actionUrl.isEmpty());
    }

    /**
     * Calculate notification age in hours
     */
    public double getAgeInHours() {
        if (this.createdAt == null) {
            return 0.0;
        }
        return java.time.Duration.between(this.createdAt, LocalDateTime.now()).toHours();
    }

    /**
     * Check if notification is urgent
     */
    public boolean isUrgent() {
        return (this.priority != null && this.priority >= 8) || 
               NotificationLevel.CRITICAL.equals(this.level);
    }

    @Override
    public String toString() {
        return "Notification for " + (recipient != null ? recipient.getUsername() : "unknown") + ": " + title;
    }
}