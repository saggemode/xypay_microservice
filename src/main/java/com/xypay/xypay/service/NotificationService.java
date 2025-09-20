package com.xypay.xypay.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.xypay.xypay.domain.Notification;
import com.xypay.xypay.domain.NotificationType;
import com.xypay.xypay.domain.NotificationLevel;
import com.xypay.xypay.domain.NotificationStatus;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.domain.Transaction;
import com.xypay.xypay.domain.CustomerEscalation;
import com.xypay.xypay.domain.BankTransfer;
import com.xypay.xypay.domain.StaffProfile;
import com.xypay.xypay.repository.NotificationRepository;
import com.xypay.xypay.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired(required = false)
    private EmailNotificationService emailNotificationService;
    
    @Autowired(required = false)
    private SMSNotificationService smsNotificationService;
    
    @Autowired(required = false)
    private PushNotificationService pushNotificationService;
    
    @Autowired(required = false)
    private WebSocketNotificationService webSocketNotificationService;
    
    /**
     * Create a new notification
     */
    public Notification createNotification(Notification notification) {
        return notificationRepository.save(notification);
    }
    
    /**
     * Create a banking notification
     */
    public Notification createBankingNotification(
            User recipient,
            String title,
            String message,
            NotificationType type,
            NotificationLevel level,
            Object data) {
        
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setNotificationType(type);
        notification.setLevel(level);
        notification.setCreatedAt(LocalDateTime.now());
        
        if (data != null) {
            try {
                ObjectNode node = objectMapper.valueToTree(data);
                notification.setExtraData(node);
            } catch (Exception e) {
                logger.warn("Failed to serialize notification data: {}", e.getMessage());
            }
        }
        
        return notificationRepository.save(notification);
    }
    
    /**
     * Handle notifications for all transaction types
     */
    public void handleTransactionNotifications(Transaction transaction) {
        logger.info("Creating comprehensive notifications for transaction {} - type: {}", 
            transaction.getId(), transaction.getType());

        // Get user details
        User user = transaction.getWallet().getUser();
        String userFullName = getUserFullName(user);
        
        String title;
        String message;
        NotificationType notificationType;
        String emailSubject;
        boolean isCredit;
        
        // Determine notification content based on transaction type
        if ("debit".equalsIgnoreCase(transaction.getType())) {
            title = "Money Sent: " + transaction.getAmount();
            message = "Your transfer of " + transaction.getAmount() + " to " + transaction.getDescription() + 
                " was successful. Reference: " + transaction.getReference();
            notificationType = NotificationType.WALLET_DEBIT;
            emailSubject = "Transaction Successful: " + transaction.getReference();
            isCredit = false;
        } else if ("credit".equalsIgnoreCase(transaction.getType())) {
            String senderName = "Unknown";
            
            logger.info("Processing WALLET_CREDIT notification for transaction: {}", transaction.getId());
            logger.info("Transaction metadata: {}", transaction.getMetadata());
            logger.info("Transaction description: {}", transaction.getDescription());
            
            // Try to get sender name from metadata first
            if (transaction.getMetadata() != null && !transaction.getMetadata().isEmpty()) {
                try {
                    JsonNode metadata = objectMapper.readTree(transaction.getMetadata());
                    logger.info("Parsed metadata: {}", metadata);
                    if (metadata.has("sender_name")) {
                        senderName = metadata.get("sender_name").asText();
                        logger.info("Found sender_name in metadata: {}", senderName);
                    } else if (metadata.has("sender_account")) {
                        senderName = metadata.get("sender_account").asText();
                        logger.info("Found sender_account in metadata: {}", senderName);
                    }
                } catch (Exception e) {
                    logger.warn("Failed to parse transaction metadata: {}", e.getMessage());
                }
            }
            
            // Fallback to description if metadata doesn't have sender info
            if ("Unknown".equals(senderName) && transaction.getDescription() != null && !transaction.getDescription().isEmpty()) {
                // Extract sender name from description like "Received 5000 from 8063163231"
                String description = transaction.getDescription();
                logger.info("Trying to extract from description: {}", description);
                if (description.startsWith("Received ") && description.contains(" from ")) {
                    String[] parts = description.split(" from ");
                    if (parts.length > 1) {
                        senderName = parts[1];
                        logger.info("Extracted sender from description: {}", senderName);
                    }
                } else if (description.contains(" from ")) {
                    // Handle other patterns like "Transfer from account123"
                    String[] parts = description.split(" from ");
                    if (parts.length > 1) {
                        senderName = parts[1];
                        logger.info("Extracted sender from description pattern: {}", senderName);
                    }
                } else {
                    // If no "from" pattern, try to extract meaningful sender info
                    // Look for account numbers or other identifiers
                    if (description.matches(".*\\d{10,}.*")) {
                        // Extract account number pattern
                        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\d{10,}");
                        java.util.regex.Matcher matcher = pattern.matcher(description);
                        if (matcher.find()) {
                            senderName = "Account " + matcher.group();
                            logger.info("Extracted account number from description: {}", senderName);
                        } else {
                            senderName = description;
                            logger.info("Using full description as sender: {}", senderName);
                        }
                    } else {
                        senderName = description;
                        logger.info("Using full description as sender: {}", senderName);
                    }
                }
            }
            
            // Note: For credit transactions, transaction.getWallet().getUser() is the recipient, not the sender
            // So we don't use it as a fallback for sender name
            
            logger.info("Final sender name: {}", senderName);
            
            title = "Money Received: " + transaction.getAmount();
            message = "You received " + transaction.getAmount() + " from " + senderName + 
                ". Reference: " + transaction.getReference();
            notificationType = NotificationType.WALLET_CREDIT;
            emailSubject = "You received a transfer: " + transaction.getReference();
            isCredit = true;
        } else {
            // Handle other transaction types
            title = "Transaction: " + transaction.getAmount();
            message = "Your " + transaction.getType() + " transaction of " + transaction.getAmount() + 
                " was " + transaction.getStatus() + ". Reference: " + transaction.getReference();
            notificationType = NotificationType.BANK_TRANSACTION;
            emailSubject = "Transaction " + transaction.getStatus() + ": " + transaction.getReference();
            isCredit = false;
        }

        // Create in-app notification
        Notification notification = new Notification();
        notification.setRecipient(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setNotificationType(notificationType);
        notification.setLevel(NotificationLevel.SUCCESS);
        notification.setStatus(NotificationStatus.SENT);
        notification.setTransaction(transaction);
        notification.setSource("bank");
        
        // Set extra data
        try {
            ObjectNode extraData = objectMapper.createObjectNode();
            extraData.put("amount", transaction.getAmount().toString());
            extraData.put("reference", transaction.getReference());
            extraData.put("balance_after", transaction.getBalanceAfter() != null ? transaction.getBalanceAfter().toString() : "");
            extraData.put("transaction_type", transaction.getType());
            extraData.put("is_credit", isCredit);
            notification.setExtraData(extraData);
        } catch (Exception e) {
            logger.error("Error setting extra data for transaction notification: {}", e.getMessage());
        }
        
        notificationRepository.save(notification);
        logger.info("Created {} notification for user: {}", transaction.getType(), user.getEmail());

        // Send email notification
        try {
            String plainMessage = String.format(
                "Dear %s,\n\n" +
                "Your transaction of %s (%s) is now '%s'.\n\n" +
                "Reference: %s\n" +
                "Type: %s\n" +
                "Channel: %s\n" +
                "Balance after transaction: %s\n\n" +
                "Thank you for banking with us.",
                userFullName,
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getStatus(),
                transaction.getReference(),
                transaction.getType(),
                transaction.getChannel(),
                transaction.getBalanceAfter()
            );
            
            emailNotificationService.sendEmailNotification(user.getEmail(), emailSubject, plainMessage);
        } catch (Exception e) {
            logger.error("Email sending failed: {}", e.getMessage());
        }

        // Send SMS notification (if phone number exists)
        try {
            if (smsNotificationService != null) {
                // Check if user has a profile with phone number
                String userPhone = getUserPhone(user);
                if (userPhone != null && !userPhone.isEmpty()) {
                    String smsMessage = title + ": " + message;
                    smsNotificationService.sendSMSNotification(userPhone, smsMessage);
                } else {
                    logger.info("No phone number found for user {} - SMS skipped", user.getId());
                }
            }
        } catch (Exception e) {
            logger.error("SMS sending failed: {}", e.getMessage());
        }

        // Send push notification
        try {
            if (pushNotificationService != null) {
                // Get user's FCM token (you'll need to implement this in UserProfile)
                String fcmToken = getUserFcmToken(user);
                if (fcmToken != null && !fcmToken.isEmpty()) {
                    Map<String, String> pushData = new HashMap<>();
                    pushData.put("transaction_id", transaction.getId().toString());
                    pushData.put("reference", transaction.getReference());
                    pushData.put("amount", transaction.getAmount().toString());
                    pushData.put("type", transaction.getType());
                    pushNotificationService.sendPushNotification(fcmToken, title, message, pushData);
                } else {
                    logger.info("No FCM token found for user {} - Push notification skipped", user.getId());
                }
            }
        } catch (Exception e) {
            logger.error("Push notification failed: {}", e.getMessage());
        }

        // Send WebSocket notification
        try {
            if (webSocketNotificationService != null) {
                Map<String, Object> wsData = new HashMap<>();
                wsData.put("id", notification.getId().toString());
                wsData.put("title", title);
                wsData.put("message", message);
                wsData.put("type", notificationType.toString());
                wsData.put("created_at", notification.getCreatedAt().toString());
                
                String wsDataJson = objectMapper.writeValueAsString(wsData);
                webSocketNotificationService.sendWebSocketNotification(user.getId().toString(), wsDataJson);
            }
        } catch (Exception e) {
            logger.error("WebSocket notification failed: {}", e.getMessage());
        }
    }
    
    /**
     * Handle escalation status changes and send notifications
     */
    public void handleEscalationStatusChange(CustomerEscalation escalation, boolean created) {
        if (created) {
            // Log staff activity
            // In a real implementation, you would create a StaffActivity record
            
            // Send notification to the creator
            StaffProfile creator = escalation.getCreatedBy();
            if (creator != null && creator.getUser() != null) {
                Notification notification = new Notification();
                notification.setRecipient(creator.getUser()); // Use the User from StaffProfile
                notification.setTitle("Escalation Created");
                notification.setMessage("Your escalation '" + escalation.getSubject() + "' has been created and is being reviewed.");
                notification.setNotificationType(NotificationType.ESCALATION);
                notification.setLevel(NotificationLevel.INFO);
                notification.setStatus(NotificationStatus.SENT);
                notification.setSource("bank");
                
                // Set extra data
                try {
                    ObjectNode extraData = objectMapper.createObjectNode();
                    extraData.put("escalation_id", escalation.getId().toString());
                    extraData.put("subject", escalation.getSubject());
                    extraData.put("priority", escalation.getPriority().getCode()); // Convert enum to string
                    notification.setExtraData(extraData);
                } catch (Exception e) {
                    logger.error("Error setting extra data for escalation notification: {}", e.getMessage());
                }
                
                notificationRepository.save(notification);
                
                // Send email to creator
                try {
                    String emailSubject = "Escalation Created";
                    String emailMessage = "Your escalation '" + escalation.getSubject() + "' has been created and is being reviewed.";
                    emailNotificationService.sendEmailNotification(creator.getUser().getEmail(), emailSubject, emailMessage); // Use User email
                } catch (Exception e) {
                    logger.error("Escalation email failed: {}", e.getMessage());
                }
            }
        } else {
            // Handle status changes
            if ("resolved".equals(escalation.getStatus()) && escalation.getResolvedAt() == null) {
                escalation.setResolvedAt(LocalDateTime.now());
                // In a real implementation, you would save the escalation
                
                StaffProfile assignedTo = escalation.getAssignedTo();
                if (assignedTo != null && assignedTo.getUser() != null) {
                    // Log staff activity
                    // In a real implementation, you would create a StaffActivity record
                    
                    // Send notification to assigned staff
                    Notification notification = new Notification();
                    notification.setRecipient(assignedTo.getUser()); // Use the User from StaffProfile
                    notification.setTitle("Escalation Resolved");
                    notification.setMessage("The escalation '" + escalation.getSubject() + "' assigned to you has been resolved.");
                    notification.setNotificationType(NotificationType.ESCALATION);
                    notification.setLevel(NotificationLevel.SUCCESS);
                    notification.setStatus(NotificationStatus.SENT);
                    notification.setSource("bank");
                    
                    // Set extra data
                    try {
                        ObjectNode extraData = objectMapper.createObjectNode();
                        extraData.put("escalation_id", escalation.getId().toString());
                        extraData.put("subject", escalation.getSubject());
                        extraData.put("resolved_at", escalation.getResolvedAt().toString());
                        notification.setExtraData(extraData);
                    } catch (Exception e) {
                        logger.error("Error setting extra data for resolved escalation notification: {}", e.getMessage());
                    }
                    
                    notificationRepository.save(notification);
                    
                    // Send email to assigned staff
                    try {
                        String emailSubject = "Escalation Resolved";
                        String emailMessage = "The escalation '" + escalation.getSubject() + "' assigned to you has been resolved.";
                        emailNotificationService.sendEmailNotification(assignedTo.getUser().getEmail(), emailSubject, emailMessage); // Use User email
                    } catch (Exception e) {
                        logger.error("Escalation resolution email failed: {}", e.getMessage());
                    }
                }
            }
        }
    }
    
    /**
     * Handle notifications for bank transfer status changes
     */
    public void handleBankTransferNotifications(BankTransfer transfer, boolean created) {
        if (created) {
            // Handle new transfer creation
            // Could send a "transfer initiated" notification
            logger.info("New bank transfer created: {}", transfer.getId());
        } else {
            // Handle status changes for existing transfers
            User user = transfer.getUser();
            if (user == null) {
                logger.warn("No user associated with bank transfer {}", transfer.getId());
                return;
            }
            
            if ("completed".equalsIgnoreCase(transfer.getStatus()) || "successful".equalsIgnoreCase(transfer.getStatus())) {
                // Send completion notification
                Notification notification = new Notification();
                notification.setRecipient(user);
                notification.setTitle("Transfer Completed");
                notification.setMessage("Your transfer of " + transfer.getAmount() + " to " + 
                    transfer.getAccountNumber() + " has been completed successfully.");
                notification.setNotificationType(NotificationType.BANK_TRANSFER);
                notification.setLevel(NotificationLevel.SUCCESS);
                notification.setStatus(NotificationStatus.SENT);
                notification.setBankTransfer(transfer);
                notification.setSource("bank");
                
                // Set extra data
                try {
                    ObjectNode extraData = objectMapper.createObjectNode();
                    extraData.put("amount", transfer.getAmount().toString());
                    extraData.put("account_number", transfer.getAccountNumber());
                    extraData.put("reference", transfer.getReference());
                    notification.setExtraData(extraData);
                } catch (Exception e) {
                    logger.error("Error setting extra data for completed transfer notification: {}", e.getMessage());
                }
                
                notificationRepository.save(notification);
                
                // Send email notification
                try {
                    String emailSubject = "Transfer Completed";
                    String emailMessage = "Your transfer of " + transfer.getAmount() + " to " + 
                        transfer.getAccountNumber() + " has been completed successfully.";
                    emailNotificationService.sendEmailNotification(user.getEmail(), emailSubject, emailMessage);
                } catch (Exception e) {
                    logger.error("Transfer completion email failed: {}", e.getMessage());
                }
                
            } else if ("failed".equals(transfer.getStatus())) {
                // Send failure notification
                Notification notification = new Notification();
                notification.setRecipient(user);
                notification.setTitle("Transfer Failed");
                notification.setMessage("Your transfer of " + transfer.getAmount() + " to " + 
                    transfer.getAccountNumber() + " has failed. Please contact support.");
                notification.setNotificationType(NotificationType.BANK_TRANSFER);
                notification.setLevel(NotificationLevel.ERROR);
                notification.setStatus(NotificationStatus.SENT);
                notification.setBankTransfer(transfer);
                notification.setSource("bank");
                
                // Set extra data
                try {
                    ObjectNode extraData = objectMapper.createObjectNode();
                    extraData.put("amount", transfer.getAmount().toString());
                    extraData.put("account_number", transfer.getAccountNumber());
                    extraData.put("reference", transfer.getReference());
                    extraData.put("failure_reason", transfer.getFailureReason() != null ? transfer.getFailureReason() : "");
                    notification.setExtraData(extraData);
                } catch (Exception e) {
                    logger.error("Error setting extra data for failed transfer notification: {}", e.getMessage());
                }
                
                notificationRepository.save(notification);
                
                // Send email notification
                try {
                    String emailSubject = "Transfer Failed";
                    String emailMessage = "Your transfer of " + transfer.getAmount() + " to " + 
                        transfer.getAccountNumber() + " has failed. Please contact support.";
                    emailNotificationService.sendEmailNotification(user.getEmail(), emailSubject, emailMessage);
                } catch (Exception e) {
                    logger.error("Transfer failure email failed: {}", e.getMessage());
                }
            }
        }
    }
    
    /**
     * Get notifications for a user
     */
    @Transactional(readOnly = true)
    public Page<Notification> getUserNotifications(User user, Pageable pageable) {
        List<Notification> allNotifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allNotifications.size());
        List<Notification> pageContent = allNotifications.subList(start, end);
        return new PageImpl<>(pageContent, pageable, allNotifications.size());
    }
    
    /**
     * Get all notifications for admin view
     */
    @Transactional(readOnly = true)
    public Page<Notification> getAllNotifications(Pageable pageable) {
        return notificationRepository.findAll(pageable);
    }
    
    /**
     * Get unread notifications for a user
     */
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user)
            .stream()
            .filter(notification -> !notification.isRead())
            .toList();
    }
    
    /**
     * Count unread notifications for a user
     */
    @Transactional(readOnly = true)
    public long countUnreadNotifications(User user) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user)
            .stream()
            .filter(notification -> !notification.isRead())
            .count();
    }
    
    /**
     * Mark a notification as read
     */
    public Notification markAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification != null) {
            notification.markAsRead();
            return notificationRepository.save(notification);
        }
        return null;
    }
    
    /**
     * Mark a notification as unread
     */
    public Notification markAsUnread(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification != null) {
            notification.markAsUnread();
            return notificationRepository.save(notification);
        }
        return null;
    }
    
    /**
     * Mark all notifications as read for a user
     */
    @Transactional
    public int markAllAsRead(User user) {
        List<Notification> unreadNotifications = getUnreadNotifications(user);
        for (Notification notification : unreadNotifications) {
            notification.markAsRead();
            notificationRepository.save(notification);
        }
        return unreadNotifications.size();
    }
    
    /**
     * Get user notifications by type with pagination
     */
    @Transactional(readOnly = true)
    public Page<Notification> getUserNotificationsByType(User user, NotificationType type, Pageable pageable) {
        List<Notification> notifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(user)
            .stream()
            .filter(notification -> notification.getNotificationType() == type)
            .toList();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), notifications.size());
        return new PageImpl<>(notifications.subList(start, end), pageable, notifications.size());
    }
    
    /**
     * Get user notifications by level with pagination
     */
    @Transactional(readOnly = true)
    public Page<Notification> getUserNotificationsByLevel(User user, NotificationLevel level, Pageable pageable) {
        List<Notification> notifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(user)
            .stream()
            .filter(notification -> notification.getLevel() == level)
            .toList();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), notifications.size());
        return new PageImpl<>(notifications.subList(start, end), pageable, notifications.size());
    }
    
    /**
     * Get user notifications by status with pagination
     */
    @Transactional(readOnly = true)
    public Page<Notification> getUserNotificationsByStatus(User user, NotificationStatus status, Pageable pageable) {
        List<Notification> notifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(user)
            .stream()
            .filter(notification -> notification.getStatus() == status)
            .toList();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), notifications.size());
        return new PageImpl<>(notifications.subList(start, end), pageable, notifications.size());
    }
    
    /**
     * Get notification by ID
     */
    @Transactional(readOnly = true)
    public Notification getNotificationById(UUID notificationId) {
        return notificationRepository.findById(notificationId).orElse(null);
    }
    
    /**
     * Get urgent notifications for a user
     */
    @Transactional(readOnly = true)
    public List<Notification> getUrgentNotifications(User user) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user)
            .stream()
            .filter(notification -> notification.getLevel() == NotificationLevel.CRITICAL || 
                                   notification.getLevel() == NotificationLevel.ERROR)
            .toList();
    }
    
    /**
     * Get actionable notifications for a user
     */
    @Transactional(readOnly = true)
    public List<Notification> getActionableNotifications(User user) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user)
            .stream()
            .filter(notification -> notification.getActionUrl() != null && !notification.getActionUrl().isEmpty())
            .toList();
    }
    
    /**
     * Bulk mark notifications as read
     */
    @Transactional
    public int bulkMarkAsRead(List<UUID> notificationIds, User user) {
        int count = 0;
        for (UUID notificationId : notificationIds) {
            Notification notification = notificationRepository.findById(notificationId).orElse(null);
            if (notification != null && notification.getRecipient().getId().equals(user.getId())) {
                notification.markAsRead();
                notificationRepository.save(notification);
                count++;
            }
        }
        return count;
    }
    
    /**
     * Bulk mark notifications as unread
     */
    @Transactional
    public int bulkMarkAsUnread(List<UUID> notificationIds, User user) {
        int count = 0;
        for (UUID notificationId : notificationIds) {
            Notification notification = notificationRepository.findById(notificationId).orElse(null);
            if (notification != null && notification.getRecipient().getId().equals(user.getId())) {
                notification.markAsUnread();
                notificationRepository.save(notification);
                count++;
            }
        }
        return count;
    }
    
    /**
     * Delete a notification
     */
    @Transactional
    public boolean deleteNotification(UUID notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification != null && notification.getRecipient().getId().equals(user.getId())) {
            notificationRepository.delete(notification);
            return true;
        }
        return false;
    }
    
    /**
     * Delete a notification (admin version - no user validation)
     */
    @Transactional
    public boolean deleteNotification(UUID notificationId) {
        try {
            Notification notification = notificationRepository.findById(notificationId).orElse(null);
            if (notification != null) {
                notificationRepository.delete(notification);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error deleting notification {}: {}", notificationId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Get notification statistics for a user
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getNotificationStatistics(User user) {
        Map<String, Object> stats = new HashMap<>();
        List<Notification> allNotifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
        
        // Count by type
        Map<String, Long> typeMap = allNotifications.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                notification -> notification.getNotificationType().toString(),
                java.util.stream.Collectors.counting()
            ));
        stats.put("byType", typeMap);
        
        // Count by level
        Map<String, Long> levelMap = allNotifications.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                notification -> notification.getLevel().toString(),
                java.util.stream.Collectors.counting()
            ));
        stats.put("byLevel", levelMap);
        
        // Count by status
        Map<String, Long> statusMap = allNotifications.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                notification -> notification.getStatus().toString(),
                java.util.stream.Collectors.counting()
            ));
        stats.put("byStatus", statusMap);
        
        // Total counts
        stats.put("totalUnread", countUnreadNotifications(user));
        stats.put("totalUrgent", getUrgentNotifications(user).size());
        stats.put("totalActionable", getActionableNotifications(user).size());
        
        return stats;
    }
    
    /**
     * Get notifications by date range
     */
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByDateRange(User user, LocalDateTime startDate, LocalDateTime endDate) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user)
            .stream()
            .filter(notification -> {
                LocalDateTime createdAt = notification.getCreatedAt();
                return createdAt != null && 
                       (createdAt.isEqual(startDate) || createdAt.isAfter(startDate)) &&
                       (createdAt.isEqual(endDate) || createdAt.isBefore(endDate));
            })
            .toList();
    }
    
    /**
     * Create notification from data map (for admin use)
     */
    @Transactional
    public Notification createNotificationFromData(Map<String, Object> data) {
        Notification notification = new Notification();
        
        // Set basic fields
        if (data.containsKey("title")) {
            notification.setTitle((String) data.get("title"));
        }
        if (data.containsKey("message")) {
            notification.setMessage((String) data.get("message"));
        }
        if (data.containsKey("type")) {
            notification.setNotificationType(NotificationType.valueOf((String) data.get("type")));
        }
        if (data.containsKey("level")) {
            notification.setLevel(NotificationLevel.valueOf((String) data.get("level")));
        }
        if (data.containsKey("priority")) {
            notification.setPriority((Integer) data.get("priority"));
        }
        if (data.containsKey("source")) {
            notification.setSource((String) data.get("source"));
        }
        
        // Set recipient (required)
        if (data.containsKey("recipientId")) {
            User recipient = userRepository.findById((UUID) data.get("recipientId")).orElse(null);
            if (recipient != null) {
                notification.setRecipient(recipient);
            }
        }
        
        // Set action fields
        if (data.containsKey("actionText")) {
            notification.setActionText((String) data.get("actionText"));
        }
        if (data.containsKey("actionUrl")) {
            notification.setActionUrl((String) data.get("actionUrl"));
        }
        
        // Set extra data
        if (data.containsKey("extraData")) {
            try {
                ObjectNode extraData = objectMapper.valueToTree(data.get("extraData"));
                notification.setExtraData(extraData);
            } catch (Exception e) {
                logger.error("Error setting extra data: {}", e.getMessage());
            }
        }
        
        notification.setStatus(NotificationStatus.SENT);
        notification.setRead(false);
        
        return notificationRepository.save(notification);
    }
    
    /**
     * Send HTML email with both HTML and plain text content
     * @param email Email address to send to
     * @param subject Email subject
     * @param htmlContent HTML content
     * @param textContent Plain text content
     * @return true if email was sent successfully, false otherwise
     */
    public boolean sendHtmlEmail(String email, String subject, String htmlContent, String textContent) {
        try {
            return emailNotificationService.sendHtmlEmailNotification(email, subject, textContent, htmlContent);
        } catch (Exception e) {
            logger.error("Failed to send HTML email to {}: {}", email, e.getMessage());
            return false;
        }
    }
    
    /**
     * Send OTP via SMS
     * @param phone Phone number to send OTP to
     * @param otp OTP code to send
     * @return true if SMS was sent successfully, false otherwise
     */
    public boolean sendOtpSms(String phone, String otp) {
        try {
            String message = String.format("Your OTP code is: %s", otp);
            return smsNotificationService.sendSMSNotification(phone, message);
        } catch (Exception e) {
            logger.error("Failed to send OTP SMS to {}: {}", phone, e.getMessage());
            return false;
        }
    }

    /**
     * Send notification to user by username
     * @param username Username of the user to notify
     * @param message Message to send
     * @return true if notification was sent successfully, false otherwise
     */
    public boolean notifyUser(String username, String message) {
        try {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                logger.warn("User not found for notification: {}", username);
                return false;
            }
            
            Notification notification = new Notification();
            notification.setRecipient(user);
            notification.setTitle("System Notification");
            notification.setMessage(message);
            notification.setNotificationType(NotificationType.SYSTEM_ALERT);
            notification.setLevel(NotificationLevel.INFO);
            notification.setStatus(NotificationStatus.SENT);
            notification.setSource("system");
            
            notificationRepository.save(notification);
            logger.info("Notification sent to user {}: {}", username, message);
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to send notification to user {}: {}", username, e.getMessage());
            return false;
        }
    }

    /**
     * Send notification to user by ID with type and message
     * @param userId User ID to send notification to
     * @param notificationType Type of notification
     * @param message Message to send
     */
    public void sendNotification(UUID userId, String notificationType, String message) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                logger.warn("User not found for notification: {}", userId);
                return;
            }
            
            Notification notification = new Notification();
            notification.setRecipient(user);
            notification.setTitle(notificationType.replace("_", " "));
            notification.setMessage(message);
            
            // Map string type to enum
            try {
                notification.setNotificationType(NotificationType.valueOf(notificationType));
            } catch (IllegalArgumentException e) {
                notification.setNotificationType(NotificationType.SYSTEM_ALERT);
            }
            
            notification.setLevel(NotificationLevel.INFO);
            notification.setStatus(NotificationStatus.SENT);
            notification.setSource("workflow");
            
            notificationRepository.save(notification);
            logger.info("Workflow notification sent to user {}: {}", userId, message);
            
        } catch (Exception e) {
            logger.error("Failed to send workflow notification to user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Get user full name helper method
     */
    private String getUserFullName(User user) {
        if (user.getFirstName() != null && user.getLastName() != null) {
            return user.getFirstName() + " " + user.getLastName();
        } else if (user.getUsername() != null && !user.getUsername().isEmpty()) {
            return user.getUsername();
        } else if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            return user.getEmail();
        } else {
            return "User " + user.getId();
        }
    }
    
    /**
     * Get user phone number from profile
     */
    private String getUserPhone(User user) {
        try {
            // You'll need to implement this based on your UserProfile structure
            // For now, return null as placeholder
            return null;
        } catch (Exception e) {
            logger.warn("Error getting phone for user {}: {}", user.getId(), e.getMessage());
            return null;
        }
    }
    
    /**
     * Get user FCM token from profile
     */
    private String getUserFcmToken(User user) {
        try {
            // You'll need to implement this based on your UserProfile structure
            // For now, return null as placeholder
            return null;
        } catch (Exception e) {
            logger.warn("Error getting FCM token for user {}: {}", user.getId(), e.getMessage());
            return null;
        }
    }
}