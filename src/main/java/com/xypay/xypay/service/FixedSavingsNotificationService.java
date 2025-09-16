package com.xypay.xypay.service;

import com.xypay.xypay.domain.FixedSavingsAccount;
import com.xypay.xypay.domain.Notification;
import com.xypay.xypay.domain.NotificationType;
import com.xypay.xypay.domain.NotificationLevel;
import com.xypay.xypay.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
public class FixedSavingsNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(FixedSavingsNotificationService.class);
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Helper method to convert JSON string to JsonNode
     */
    private JsonNode createJsonNode(String jsonString) {
        try {
            return objectMapper.readTree(jsonString);
        } catch (Exception e) {
            logger.error("Error creating JsonNode from string: {}", e.getMessage());
            return objectMapper.createObjectNode();
        }
    }
    
    /**
     * Send notification when fixed savings is created
     */
    public void sendFixedSavingsCreatedNotification(FixedSavingsAccount fixedSavings) {
        try {
            String message = String.format(
                "Your fixed savings of ₦%,.2f has been created successfully. " +
                "Maturity date: %s. " +
                "Interest rate: %.2f%% p.a.",
                fixedSavings.getAmount(),
                fixedSavings.getPaybackDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
                fixedSavings.getInterestRate()
            );
            
            Notification notification = new Notification();
            notification.setRecipient(fixedSavings.getUser());
            notification.setTitle("Fixed Savings Created");
            notification.setMessage(message);
            notification.setNotificationType(NotificationType.FIXED_SAVINGS_CREATED);
            notification.setLevel(NotificationLevel.SUCCESS);
            notification.setSource("fixed_savings");
            notification.setExtraData(createJsonNode(String.format(
                "{\"fixed_savings_id\":\"%s\",\"amount\":\"₦%,.2f\",\"interest_rate\":\"%.2f\",\"maturity_date\":\"%s\",\"purpose\":\"%s\",\"source\":\"%s\"}",
                fixedSavings.getId(),
                fixedSavings.getAmount(),
                fixedSavings.getInterestRate(),
                fixedSavings.getPaybackDate().toString(),
                fixedSavings.getPurpose().getCode(),
                fixedSavings.getSource().getCode()
            )));
            
            notificationRepository.save(notification);
        } catch (Exception e) {
            logger.error("Error sending fixed savings created notification: {}", e.getMessage());
        }
    }
    
    /**
     * Send notification when fixed savings matures
     */
    public void sendFixedSavingsMaturedNotification(FixedSavingsAccount fixedSavings) {
        try {
            String message = String.format(
                "Your fixed savings of ₦%,.2f has matured! " +
                "Total maturity amount: ₦%,.2f. " +
                "Interest earned: ₦%,.2f.",
                fixedSavings.getAmount(),
                fixedSavings.getMaturityAmount(),
                fixedSavings.getTotalInterestEarned()
            );
            
            Notification notification = new Notification();
            notification.setRecipient(fixedSavings.getUser());
            notification.setTitle("Fixed Savings Matured");
            notification.setMessage(message);
            notification.setNotificationType(NotificationType.FIXED_SAVINGS_MATURED);
            notification.setLevel(NotificationLevel.SUCCESS);
            notification.setSource("fixed_savings");
            notification.setExtraData(createJsonNode(String.format(
                "{\"fixed_savings_id\":\"%s\",\"original_amount\":\"₦%,.2f\",\"maturity_amount\":\"₦%,.2f\",\"interest_earned\":\"₦%,.2f\",\"interest_rate\":\"%.2f\"}",
                fixedSavings.getId(),
                fixedSavings.getAmount(),
                fixedSavings.getMaturityAmount(),
                fixedSavings.getTotalInterestEarned(),
                fixedSavings.getInterestRate()
            )));
            
            notificationRepository.save(notification);
        } catch (Exception e) {
            logger.error("Error sending fixed savings matured notification: {}", e.getMessage());
        }
    }
    
    /**
     * Send notification when fixed savings is paid out
     */
    public void sendFixedSavingsPaidOutNotification(FixedSavingsAccount fixedSavings) {
        try {
            String message = String.format(
                "Your matured fixed savings of ₦%,.2f " +
                "has been credited to your XySave account successfully.",
                fixedSavings.getMaturityAmount()
            );
            
            Notification notification = new Notification();
            notification.setRecipient(fixedSavings.getUser());
            notification.setTitle("Fixed Savings Paid Out");
            notification.setMessage(message);
            notification.setNotificationType(NotificationType.FIXED_SAVINGS_PAID_OUT);
            notification.setLevel(NotificationLevel.SUCCESS);
            notification.setSource("fixed_savings");
            notification.setExtraData(createJsonNode(String.format(
                "{\"fixed_savings_id\":\"%s\",\"maturity_amount\":\"₦%,.2f\",\"interest_earned\":\"₦%,.2f\",\"destination\":\"xysave_account\"}",
                fixedSavings.getId(),
                fixedSavings.getMaturityAmount(),
                fixedSavings.getTotalInterestEarned()
            )));
            
            notificationRepository.save(notification);
        } catch (Exception e) {
            logger.error("Error sending fixed savings paid out notification: {}", e.getMessage());
        }
    }
    
    /**
     * Send notification when fixed savings auto-renews
     */
    public void sendFixedSavingsAutoRenewalNotification(FixedSavingsAccount fixedSavings) {
        try {
            String message = String.format(
                "Your fixed savings has been auto-renewed for ₦%,.2f. " +
                "New maturity date: %s. " +
                "Interest rate: %.2f%% p.a.",
                fixedSavings.getAmount(),
                fixedSavings.getPaybackDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
                fixedSavings.getInterestRate()
            );
            
            Notification notification = new Notification();
            notification.setRecipient(fixedSavings.getUser());
            notification.setTitle("Fixed Savings Auto-Renewed");
            notification.setMessage(message);
            notification.setNotificationType(NotificationType.FIXED_SAVINGS_AUTO_RENEWAL);
            notification.setLevel(NotificationLevel.INFO);
            notification.setSource("fixed_savings");
            notification.setExtraData(createJsonNode(String.format(
                "{\"fixed_savings_id\":\"%s\",\"amount\":\"₦%,.2f\",\"interest_rate\":\"%.2f\",\"maturity_date\":\"%s\",\"auto_renewal\":true}",
                fixedSavings.getId(),
                fixedSavings.getAmount(),
                fixedSavings.getInterestRate(),
                fixedSavings.getPaybackDate().toString()
            )));
            
            notificationRepository.save(notification);
        } catch (Exception e) {
            logger.error("Error sending fixed savings auto-renewal notification: {}", e.getMessage());
        }
    }
    
    /**
     * Send reminder notification before maturity
     */
    public void sendFixedSavingsMaturityReminderNotification(FixedSavingsAccount fixedSavings) {
        try {
            int daysRemaining = fixedSavings.getDaysRemaining();
            if (daysRemaining <= 7) { // Send reminder 7 days before maturity
                String message = String.format(
                    "Your fixed savings of ₦%,.2f will mature in %d days. " +
                    "Maturity amount: ₦%,.2f.",
                    fixedSavings.getAmount(),
                    daysRemaining,
                    fixedSavings.getMaturityAmount()
                );
                
                Notification notification = new Notification();
                notification.setRecipient(fixedSavings.getUser());
                notification.setTitle("Fixed Savings Maturity Reminder");
                notification.setMessage(message);
                notification.setNotificationType(NotificationType.FIXED_SAVINGS_MATURITY_REMINDER);
                notification.setLevel(NotificationLevel.WARNING);
                notification.setSource("fixed_savings");
                notification.setExtraData(createJsonNode(String.format(
                    "{\"fixed_savings_id\":\"%s\",\"days_remaining\":%d,\"maturity_amount\":\"₦%,.2f\",\"maturity_date\":\"%s\"}",
                    fixedSavings.getId(),
                    daysRemaining,
                    fixedSavings.getMaturityAmount(),
                    fixedSavings.getPaybackDate().toString()
                )));
                
                notificationRepository.save(notification);
            }
        } catch (Exception e) {
            logger.error("Error sending fixed savings maturity reminder notification: {}", e.getMessage());
        }
    }
    
    /**
     * Send notification when interest is credited
     */
    public void sendFixedSavingsInterestCreditedNotification(FixedSavingsAccount fixedSavings, BigDecimal interestAmount) {
        try {
            String message = String.format(
                "Interest of ₦%,.2f has been credited to your fixed savings. " +
                "Current total: ₦%,.2f.",
                interestAmount,
                fixedSavings.getMaturityAmount()
            );
            
            Notification notification = new Notification();
            notification.setRecipient(fixedSavings.getUser());
            notification.setTitle("Fixed Savings Interest Credited");
            notification.setMessage(message);
            notification.setNotificationType(NotificationType.FIXED_SAVINGS_INTEREST_CREDITED);
            notification.setLevel(NotificationLevel.SUCCESS);
            notification.setSource("fixed_savings");
            notification.setExtraData(createJsonNode(String.format(
                "{\"fixed_savings_id\":\"%s\",\"interest_amount\":\"₦%,.2f\",\"total_amount\":\"₦%,.2f\",\"interest_rate\":\"%.2f\"}",
                fixedSavings.getId(),
                interestAmount,
                fixedSavings.getMaturityAmount(),
                fixedSavings.getInterestRate()
            )));
            
            notificationRepository.save(notification);
        } catch (Exception e) {
            logger.error("Error sending fixed savings interest credited notification: {}", e.getMessage());
        }
    }
    
    /**
     * Send notification for early withdrawal
     */
    public void sendFixedSavingsEarlyWithdrawalNotification(FixedSavingsAccount fixedSavings, 
                                                           BigDecimal withdrawalAmount, 
                                                           BigDecimal penaltyAmount) {
        try {
            String message = String.format(
                "Early withdrawal processed: ₦%,.2f. " +
                "Penalty applied: ₦%,.2f. " +
                "Early withdrawal may affect your interest earnings.",
                withdrawalAmount,
                penaltyAmount
            );
            
            Notification notification = new Notification();
            notification.setRecipient(fixedSavings.getUser());
            notification.setTitle("Fixed Savings Early Withdrawal");
            notification.setMessage(message);
            notification.setNotificationType(NotificationType.FIXED_SAVINGS_EARLY_WITHDRAWAL);
            notification.setLevel(NotificationLevel.WARNING);
            notification.setSource("fixed_savings");
            notification.setExtraData(createJsonNode(String.format(
                "{\"fixed_savings_id\":\"%s\",\"withdrawal_amount\":\"₦%,.2f\",\"penalty_amount\":\"₦%,.2f\",\"original_amount\":\"₦%,.2f\"}",
                fixedSavings.getId(),
                withdrawalAmount,
                penaltyAmount,
                fixedSavings.getAmount()
            )));
            
            notificationRepository.save(notification);
        } catch (Exception e) {
            logger.error("Error sending fixed savings early withdrawal notification: {}", e.getMessage());
        }
    }
}