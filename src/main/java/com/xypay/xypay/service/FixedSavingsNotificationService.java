package com.xypay.xypay.service;

import com.xypay.xypay.domain.FixedSavingsAccount;
import com.xypay.xypay.domain.Notification;
import com.xypay.xypay.domain.NotificationType;
import com.xypay.xypay.domain.NotificationLevel;
import com.xypay.xypay.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class FixedSavingsNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(FixedSavingsNotificationService.class);
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    /**
     * Send notification when fixed savings is created
     */
    public void sendFixedSavingsCreatedNotification(FixedSavingsAccount fixedSavings) {
        try {
            Notification notification = new Notification();
            notification.setRecipient(fixedSavings.getUser());
            notification.setMessage(String.format(
                "Your fixed savings of ₦%,.2f has been created successfully. Maturity date: %s. Interest rate: %.2f%% p.a.",
                fixedSavings.getAmount(),
                fixedSavings.getPaybackDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
                fixedSavings.getInterestRate()));
            notification.setRead(false);
            notification.setCreatedAt(java.time.LocalDateTime.now());
            
            Map<String, Object> extraData = new HashMap<>();
            extraData.put("fixed_savings_id", fixedSavings.getId().toString());
            extraData.put("amount", fixedSavings.getAmount().toString());
            extraData.put("interest_rate", String.valueOf(fixedSavings.getInterestRate()));
            extraData.put("maturity_date", fixedSavings.getPaybackDate().toString());
            extraData.put("purpose", fixedSavings.getPurpose());
            extraData.put("source", fixedSavings.getSource());
            // Convert Map to JsonNode for extraData
            ObjectMapper mapper = new ObjectMapper();
            notification.setExtraData(mapper.valueToTree(extraData));
            
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
            Notification notification = new Notification();
            notification.setRecipient(fixedSavings.getUser());
            notification.setMessage(String.format(
                "Your fixed savings of ₦%,.2f has matured! Total maturity amount: ₦%,.2f. Interest earned: ₦%,.2f.",
                fixedSavings.getAmount(),
                fixedSavings.getMaturityAmount(),
                fixedSavings.getTotalInterestEarned()));
            notification.setRead(false);
            notification.setCreatedAt(java.time.LocalDateTime.now());
            
            Map<String, Object> extraData = new HashMap<>();
            extraData.put("fixed_savings_id", fixedSavings.getId().toString());
            extraData.put("original_amount", fixedSavings.getAmount().toString());
            extraData.put("maturity_amount", fixedSavings.getMaturityAmount().toString());
            extraData.put("interest_earned", fixedSavings.getTotalInterestEarned().toString());
            extraData.put("interest_rate", String.valueOf(fixedSavings.getInterestRate()));
            // Convert Map to JsonNode for extraData
            ObjectMapper mapper = new ObjectMapper();
            notification.setExtraData(mapper.valueToTree(extraData));
            
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
            Notification notification = new Notification();
            notification.setRecipient(fixedSavings.getUser());
            notification.setMessage(String.format(
                "Your matured fixed savings of ₦%,.2f has been credited to your XySave account successfully.",
                fixedSavings.getMaturityAmount()));
            notification.setRead(false);
            notification.setCreatedAt(java.time.LocalDateTime.now());
            
            Map<String, Object> extraData = new HashMap<>();
            extraData.put("fixed_savings_id", fixedSavings.getId().toString());
            extraData.put("maturity_amount", fixedSavings.getMaturityAmount().toString());
            extraData.put("interest_earned", fixedSavings.getTotalInterestEarned().toString());
            extraData.put("destination", "xysave_account");
            // Convert Map to JsonNode for extraData
            ObjectMapper mapper = new ObjectMapper();
            notification.setExtraData(mapper.valueToTree(extraData));
            
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
            Notification notification = new Notification();
            notification.setRecipient(fixedSavings.getUser());
            notification.setMessage(String.format(
                "Your fixed savings has been auto-renewed for ₦%,.2f. New maturity date: %s. Interest rate: %.2f%% p.a.",
                fixedSavings.getAmount(),
                fixedSavings.getPaybackDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
                fixedSavings.getInterestRate()));
            notification.setRead(false);
            notification.setCreatedAt(java.time.LocalDateTime.now());
            
            Map<String, Object> extraData = new HashMap<>();
            extraData.put("fixed_savings_id", fixedSavings.getId().toString());
            extraData.put("amount", fixedSavings.getAmount().toString());
            extraData.put("interest_rate", String.valueOf(fixedSavings.getInterestRate()));
            extraData.put("maturity_date", fixedSavings.getPaybackDate().toString());
            extraData.put("auto_renewal", true);
            // Convert Map to JsonNode for extraData
            ObjectMapper mapper = new ObjectMapper();
            notification.setExtraData(mapper.valueToTree(extraData));
            
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
                Notification notification = new Notification();
                notification.setRecipient(fixedSavings.getUser());
                notification.setMessage(String.format(
                    "Your fixed savings of ₦%,.2f will mature in %d days. Maturity amount: ₦%,.2f.",
                    fixedSavings.getAmount(),
                    daysRemaining,
                    fixedSavings.getMaturityAmount()));
                notification.setNotificationType(NotificationType.FIXED_SAVINGS_MATURITY_REMINDER);
                notification.setLevel(NotificationLevel.WARNING);
                notification.setSource("fixed_savings");
                
                Map<String, Object> extraData = new HashMap<>();
                extraData.put("fixed_savings_id", fixedSavings.getId().toString());
                extraData.put("days_remaining", daysRemaining);
                extraData.put("maturity_amount", fixedSavings.getMaturityAmount().toString());
                extraData.put("maturity_date", fixedSavings.getPaybackDate().toString());
                // Convert Map to JsonNode for extraData
                ObjectMapper mapper = new ObjectMapper();
                notification.setExtraData(mapper.valueToTree(extraData));
                
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
            Notification notification = new Notification();
            notification.setRecipient(fixedSavings.getUser());
            notification.setMessage(String.format(
                "Interest of ₦%,.2f has been credited to your fixed savings. Current total: ₦%,.2f.",
                interestAmount,
                fixedSavings.getMaturityAmount()));
            notification.setRead(false);
            notification.setCreatedAt(java.time.LocalDateTime.now());
            
            Map<String, Object> extraData = new HashMap<>();
            extraData.put("fixed_savings_id", fixedSavings.getId().toString());
            extraData.put("interest_amount", interestAmount.toString());
            extraData.put("total_amount", fixedSavings.getMaturityAmount().toString());
            extraData.put("interest_rate", String.valueOf(fixedSavings.getInterestRate()));
            // Convert Map to JsonNode for extraData
            ObjectMapper mapper = new ObjectMapper();
            notification.setExtraData(mapper.valueToTree(extraData));
            
            notificationRepository.save(notification);
        } catch (Exception e) {
            logger.error("Error sending fixed savings interest credited notification: {}", e.getMessage());
        }
    }
    
    /**
     * Send notification for early withdrawal
     */
    public void sendFixedSavingsEarlyWithdrawalNotification(FixedSavingsAccount fixedSavings, 
                                                           BigDecimal withdrawalAmount, BigDecimal penaltyAmount) {
        try {
            Notification notification = new Notification();
            notification.setRecipient(fixedSavings.getUser());
            notification.setMessage(String.format(
                "Early withdrawal processed: ₦%,.2f. Penalty applied: ₦%,.2f. Early withdrawal may affect your interest earnings.",
                withdrawalAmount,
                penaltyAmount));
            notification.setRead(false);
            notification.setCreatedAt(java.time.LocalDateTime.now());
            
            Map<String, Object> extraData = new HashMap<>();
            extraData.put("fixed_savings_id", fixedSavings.getId().toString());
            extraData.put("withdrawal_amount", withdrawalAmount.toString());
            extraData.put("penalty_amount", penaltyAmount.toString());
            extraData.put("original_amount", fixedSavings.getAmount().toString());
            // Convert Map to JsonNode for extraData
            ObjectMapper mapper = new ObjectMapper();
            notification.setExtraData(mapper.valueToTree(extraData));
            
            notificationRepository.save(notification);
        } catch (Exception e) {
            logger.error("Error sending fixed savings early withdrawal notification: {}", e.getMessage());
        }
    }
}