package com.xypay.xypay.service;

import com.xypay.xypay.domain.TargetSaving;
import com.xypay.xypay.domain.TargetSavingDeposit;
import com.xypay.xypay.domain.TargetSavingWithdrawal;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.domain.Notification;
import com.xypay.xypay.domain.NotificationType;
import com.xypay.xypay.domain.NotificationLevel;
import com.xypay.xypay.domain.NotificationStatus;
import com.xypay.xypay.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class TargetSavingNotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private JsonNode convertToJsonNode(Map<String, Object> data) {
        try {
            return objectMapper.valueToTree(data);
        } catch (Exception e) {
            log.error("Error converting Map to JsonNode: {}", e.getMessage());
            return objectMapper.createObjectNode();
        }
    }
    
    public void sendTargetCreatedNotification(User user, TargetSaving targetSaving) {
        try {
            String title = "🎯 New Target Saving Created!";
            String message = String.format(
                "Your target '%s' has been created successfully. " +
                "Target amount: ₦%,.2f, " +
                "End date: %s",
                targetSaving.getName(),
                targetSaving.getTargetAmount(),
                targetSaving.getEndDate()
            );
            
            Map<String, Object> extraData = new HashMap<>();
            extraData.put("target_id", targetSaving.getId().toString());
            extraData.put("target_name", targetSaving.getName());
            extraData.put("target_amount", targetSaving.getTargetAmount());
            extraData.put("category", targetSaving.getCategory().getCode());
            extraData.put("frequency", targetSaving.getFrequency().getCode());
            extraData.put("end_date", targetSaving.getEndDate().toString());
            extraData.put("action_url", "/target-savings/" + targetSaving.getId());
            
            Notification notification = new Notification();
            notification.setRecipient(user);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setNotificationType(NotificationType.OTHER);
            notification.setLevel(NotificationLevel.SUCCESS);
            notification.setStatus(NotificationStatus.PENDING);
            notification.setSource("target_saving");
            notification.setExtraData(convertToJsonNode(extraData));
            notification.setCreatedAt(LocalDateTime.now());
            
            notificationRepository.save(notification);
            log.info("Target created notification sent to user {}", user.getUsername());
            
        } catch (Exception e) {
            log.error("Error sending target created notification: {}", e.getMessage());
        }
    }
    
    public void sendTargetUpdatedNotification(User user, TargetSaving targetSaving) {
        try {
            String title = "📝 Target Saving Updated";
            String message = String.format(
                "Your target '%s' has been updated successfully. " +
                "Current progress: %.1f%%",
                targetSaving.getName(),
                targetSaving.getProgressPercentage()
            );
            
            Map<String, Object> extraData = new HashMap<>();
            extraData.put("target_id", targetSaving.getId().toString());
            extraData.put("target_name", targetSaving.getName());
            extraData.put("progress_percentage", targetSaving.getProgressPercentage());
            extraData.put("current_amount", targetSaving.getCurrentAmount());
            extraData.put("action_url", "/target-savings/" + targetSaving.getId());
            
            Notification notification = new Notification();
            notification.setRecipient(user);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setNotificationType(NotificationType.OTHER);
            notification.setLevel(NotificationLevel.INFO);
            notification.setStatus(NotificationStatus.PENDING);
            notification.setSource("target_saving");
            notification.setExtraData(convertToJsonNode(extraData));
            notification.setCreatedAt(LocalDateTime.now());
            
            notificationRepository.save(notification);
            log.info("Target updated notification sent to user {}", user.getUsername());
            
        } catch (Exception e) {
            log.error("Error sending target updated notification: {}", e.getMessage());
        }
    }
    
    public void sendTargetCompletedNotification(User user, TargetSaving targetSaving) {
        try {
            String title = "🎉 Target Saving Completed!";
            String message = String.format(
                "Congratulations! You've successfully completed your target '%s'. " +
                "Final amount saved: ₦%,.2f",
                targetSaving.getName(),
                targetSaving.getCurrentAmount()
            );
            
            Map<String, Object> extraData = new HashMap<>();
            extraData.put("target_id", targetSaving.getId().toString());
            extraData.put("target_name", targetSaving.getName());
            extraData.put("final_amount", targetSaving.getCurrentAmount());
            extraData.put("target_amount", targetSaving.getTargetAmount());
            extraData.put("action_url", "/target-savings/" + targetSaving.getId());
            
            Notification notification = new Notification();
            notification.setRecipient(user);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setNotificationType(NotificationType.OTHER);
            notification.setLevel(NotificationLevel.SUCCESS);
            notification.setStatus(NotificationStatus.PENDING);
            notification.setSource("target_saving");
            notification.setExtraData(convertToJsonNode(extraData));
            notification.setCreatedAt(LocalDateTime.now());
            
            notificationRepository.save(notification);
            log.info("Target completed notification sent to user {}", user.getUsername());
            
        } catch (Exception e) {
            log.error("Error sending target completed notification: {}", e.getMessage());
        }
    }
    
    public void sendDepositNotification(User user, TargetSaving targetSaving, TargetSavingDeposit deposit) {
        try {
            String title = "💰 Deposit Made to Target";
            String message = String.format(
                "₦%,.2f deposited to '%s'. " +
                "Progress: %.1f%% " +
                "(%s/%s)",
                deposit.getAmount(),
                targetSaving.getName(),
                targetSaving.getProgressPercentage(),
                targetSaving.getCurrentAmount(),
                targetSaving.getTargetAmount()
            );
            
            Map<String, Object> extraData = new HashMap<>();
            extraData.put("target_id", targetSaving.getId().toString());
            extraData.put("target_name", targetSaving.getName());
            extraData.put("deposit_amount", deposit.getAmount());
            extraData.put("progress_percentage", targetSaving.getProgressPercentage());
            extraData.put("current_amount", targetSaving.getCurrentAmount());
            extraData.put("remaining_amount", targetSaving.getRemainingAmount());
            extraData.put("action_url", "/target-savings/" + targetSaving.getId());
            
            Notification notification = new Notification();
            notification.setRecipient(user);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setNotificationType(NotificationType.WALLET_CREDIT);
            notification.setLevel(NotificationLevel.SUCCESS);
            notification.setStatus(NotificationStatus.PENDING);
            notification.setSource("target_saving");
            notification.setExtraData(convertToJsonNode(extraData));
            notification.setCreatedAt(LocalDateTime.now());
            
            notificationRepository.save(notification);
            log.info("Deposit notification sent to user {}", user.getUsername());
            
        } catch (Exception e) {
            log.error("Error sending deposit notification: {}", e.getMessage());
        }
    }
    
    public void sendMilestoneNotification(User user, TargetSaving targetSaving, String milestoneType, BigDecimal progressPercentage) {
        try {
            Map<String, Map<String, Object>> milestoneMessages = Map.of(
                "quarter", Map.of(
                    "title", "🎯 25% Target Milestone!",
                    "message", String.format("Great progress! You've reached 25%% of your target '%s'. Keep up the excellent work!", targetSaving.getName()),
                    "level", "SUCCESS"
                ),
                "half", Map.of(
                    "title", "🏆 50% Target Milestone!",
                    "message", String.format("Outstanding! You're halfway to your target '%s'. You're doing amazing!", targetSaving.getName()),
                    "level", "SUCCESS"
                ),
                "three_quarters", Map.of(
                    "title", "💎 75% Target Milestone!",
                    "message", String.format("Fantastic! You're 75%% of the way to your target '%s'. Almost there!", targetSaving.getName()),
                    "level", "SUCCESS"
                ),
                "ninety", Map.of(
                    "title", "🔥 90% Target Milestone!",
                    "message", String.format("Incredible! You're 90%% of the way to your target '%s'. Final stretch!", targetSaving.getName()),
                    "level", "SUCCESS"
                )
            );
            
            if (milestoneMessages.containsKey(milestoneType)) {
                Map<String, Object> messageData = milestoneMessages.get(milestoneType);
                
                Map<String, Object> extraData = new HashMap<>();
                extraData.put("target_id", targetSaving.getId().toString());
                extraData.put("target_name", targetSaving.getName());
                extraData.put("milestone_type", milestoneType);
                extraData.put("progress_percentage", progressPercentage);
                extraData.put("current_amount", targetSaving.getCurrentAmount());
                extraData.put("action_url", "/target-savings/" + targetSaving.getId());
                
                Notification notification = new Notification();
                notification.setRecipient(user);
                notification.setTitle((String) messageData.get("title"));
                notification.setMessage((String) messageData.get("message"));
                notification.setNotificationType(NotificationType.OTHER);
                notification.setLevel(NotificationLevel.valueOf(((String) messageData.get("level")).toUpperCase()));
                notification.setStatus(NotificationStatus.PENDING);
                notification.setSource("target_saving");
                notification.setExtraData(convertToJsonNode(extraData));
                notification.setCreatedAt(LocalDateTime.now());
                
                notificationRepository.save(notification);
                log.info("Target milestone notification sent to user {}: {}", user.getUsername(), milestoneType);
            }
            
        } catch (Exception e) {
            log.error("Error sending target milestone notification: {}", e.getMessage());
        }
    }
    
    public void checkAndSendMilestoneNotifications(User user, TargetSaving targetSaving) {
        try {
            BigDecimal progressPercentage = targetSaving.getProgressPercentage();
            
            // Define milestones
            Map<BigDecimal, String> milestones = Map.of(
                new BigDecimal("25"), "quarter",
                new BigDecimal("50"), "half",
                new BigDecimal("75"), "three_quarters",
                new BigDecimal("90"), "ninety"
            );
            
            for (Map.Entry<BigDecimal, String> entry : milestones.entrySet()) {
                BigDecimal milestonePercentage = entry.getKey();
                String milestoneType = entry.getValue();
                
                if (progressPercentage.compareTo(milestonePercentage) >= 0) {
                    // Check if we've already sent this milestone notification
                    boolean existingNotification = notificationRepository.existsByRecipientAndSourceAndExtraDataContaining(
                        user, "target_saving", "milestone_type", milestoneType
                    );
                    
                    if (!existingNotification) {
                        sendMilestoneNotification(user, targetSaving, milestoneType, progressPercentage);
                        break; // Only send one milestone at a time
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Error checking target milestone notifications: {}", e.getMessage());
        }
    }
    
    public void sendTargetOverdueNotification(User user, TargetSaving targetSaving) {
        try {
            String title = "⚠️ Target Saving Overdue";
            String message = String.format(
                "Your target '%s' is overdue. " +
                "End date was %s. " +
                "Current progress: %.1f%%",
                targetSaving.getName(),
                targetSaving.getEndDate(),
                targetSaving.getProgressPercentage()
            );
            
            Map<String, Object> extraData = new HashMap<>();
            extraData.put("target_id", targetSaving.getId().toString());
            extraData.put("target_name", targetSaving.getName());
            extraData.put("end_date", targetSaving.getEndDate().toString());
            extraData.put("progress_percentage", targetSaving.getProgressPercentage());
            extraData.put("remaining_amount", targetSaving.getRemainingAmount());
            extraData.put("action_url", "/target-savings/" + targetSaving.getId());
            
            Notification notification = new Notification();
            notification.setRecipient(user);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setNotificationType(NotificationType.OTHER);
            notification.setLevel(NotificationLevel.WARNING);
            notification.setStatus(NotificationStatus.PENDING);
            notification.setSource("target_saving");
            notification.setExtraData(convertToJsonNode(extraData));
            notification.setCreatedAt(LocalDateTime.now());
            
            notificationRepository.save(notification);
            log.info("Target overdue notification sent to user {}", user.getUsername());
            
        } catch (Exception e) {
            log.error("Error sending target overdue notification: {}", e.getMessage());
        }
    }
    
    public void sendTargetReminderNotification(User user, TargetSaving targetSaving, String reminderType) {
        try {
            Map<String, Map<String, Object>> reminderMessages = Map.of(
                "weekly", Map.of(
                    "title", "📅 Weekly Target Reminder",
                    "message", String.format("Don't forget to contribute to your target '%s'. Progress: %.1f%%", 
                                           targetSaving.getName(), targetSaving.getProgressPercentage()),
                    "level", "INFO"
                ),
                "monthly", Map.of(
                    "title", "📅 Monthly Target Reminder",
                    "message", String.format("Monthly reminder for your target '%s'. Progress: %.1f%%", 
                                           targetSaving.getName(), targetSaving.getProgressPercentage()),
                    "level", "INFO"
                ),
                "deadline", Map.of(
                    "title", "⏰ Target Deadline Approaching",
                    "message", String.format("Your target '%s' deadline is approaching. Days remaining: %d", 
                                           targetSaving.getName(), targetSaving.getDaysRemaining()),
                    "level", "WARNING"
                )
            );
            
            if (reminderMessages.containsKey(reminderType)) {
                Map<String, Object> messageData = reminderMessages.get(reminderType);
                
                Map<String, Object> extraData = new HashMap<>();
                extraData.put("target_id", targetSaving.getId().toString());
                extraData.put("target_name", targetSaving.getName());
                extraData.put("reminder_type", reminderType);
                extraData.put("progress_percentage", targetSaving.getProgressPercentage());
                extraData.put("days_remaining", targetSaving.getDaysRemaining());
                extraData.put("action_url", "/target-savings/" + targetSaving.getId());
                
                Notification notification = new Notification();
                notification.setRecipient(user);
                notification.setTitle((String) messageData.get("title"));
                notification.setMessage((String) messageData.get("message"));
                notification.setNotificationType(NotificationType.OTHER);
                notification.setLevel(NotificationLevel.valueOf(((String) messageData.get("level")).toUpperCase()));
                notification.setStatus(NotificationStatus.PENDING);
                notification.setSource("target_saving");
                notification.setExtraData(convertToJsonNode(extraData));
                notification.setCreatedAt(LocalDateTime.now());
                
                notificationRepository.save(notification);
                log.info("Target reminder notification sent to user {}: {}", user.getUsername(), reminderType);
            }
            
        } catch (Exception e) {
            log.error("Error sending target reminder notification: {}", e.getMessage());
        }
    }
    
    public void sendTargetDeactivatedNotification(User user, TargetSaving targetSaving) {
        try {
            String title = "⏸️ Target Saving Deactivated";
            String message = String.format(
                "Your target '%s' has been deactivated. " +
                "Final progress: %.1f%% " +
                "(%s/%s)",
                targetSaving.getName(),
                targetSaving.getProgressPercentage(),
                targetSaving.getCurrentAmount(),
                targetSaving.getTargetAmount()
            );
            
            Map<String, Object> extraData = new HashMap<>();
            extraData.put("target_id", targetSaving.getId().toString());
            extraData.put("target_name", targetSaving.getName());
            extraData.put("final_progress", targetSaving.getProgressPercentage());
            extraData.put("final_amount", targetSaving.getCurrentAmount());
            extraData.put("action_url", "/target-savings/" + targetSaving.getId());
            
            Notification notification = new Notification();
            notification.setRecipient(user);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setNotificationType(NotificationType.OTHER);
            notification.setLevel(NotificationLevel.WARNING);
            notification.setStatus(NotificationStatus.PENDING);
            notification.setSource("target_saving");
            notification.setExtraData(convertToJsonNode(extraData));
            notification.setCreatedAt(LocalDateTime.now());
            
            notificationRepository.save(notification);
            log.info("Target deactivated notification sent to user {}", user.getUsername());
            
        } catch (Exception e) {
            log.error("Error sending target deactivated notification: {}", e.getMessage());
        }
    }
    
    public void sendWithdrawalNotification(User user, TargetSaving targetSaving, TargetSavingWithdrawal withdrawal) {
        try {
            String title = "💸 Withdrawal from Target Saving";
            String message = String.format(
                "₦%,.2f withdrawn from '%s'. " +
                "New balance: ₦%,.2f " +
                "Progress: %.1f%%",
                withdrawal.getAmount(),
                targetSaving.getName(),
                targetSaving.getCurrentAmount(),
                targetSaving.getProgressPercentage()
            );
            
            Map<String, Object> extraData = new HashMap<>();
            extraData.put("target_id", targetSaving.getId().toString());
            extraData.put("target_name", targetSaving.getName());
            extraData.put("withdrawal_amount", withdrawal.getAmount());
            extraData.put("destination", withdrawal.getDestination());
            extraData.put("current_amount", targetSaving.getCurrentAmount());
            extraData.put("progress_percentage", targetSaving.getProgressPercentage());
            extraData.put("action_url", "/target-savings/" + targetSaving.getId());
            
            Notification notification = new Notification();
            notification.setRecipient(user);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setNotificationType(NotificationType.WALLET_DEBIT);
            notification.setLevel(NotificationLevel.INFO);
            notification.setStatus(NotificationStatus.PENDING);
            notification.setSource("target_saving");
            notification.setExtraData(convertToJsonNode(extraData));
            notification.setCreatedAt(LocalDateTime.now());
            
            notificationRepository.save(notification);
            log.info("Withdrawal notification sent to user {}", user.getUsername());
            
        } catch (Exception e) {
            log.error("Error sending withdrawal notification: {}", e.getMessage());
        }
    }
}
