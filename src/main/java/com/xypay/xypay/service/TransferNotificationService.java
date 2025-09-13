package com.xypay.xypay.service;

import com.xypay.xypay.domain.BankTransfer;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.domain.UserProfile;
import com.xypay.xypay.domain.Wallet;
import com.xypay.xypay.repository.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Service for sending transfer notifications via email and SMS.
 * Equivalent to Django's send_transfer_notifications function.
 */
@Service
public class TransferNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransferNotificationService.class);
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Autowired
    private EmailNotificationService emailNotificationService;
    
    @Autowired
    private SMSNotificationService smsNotificationService;
    
    /**
     * Send email and SMS notifications for successful transfer.
     * Matches Django's send_transfer_notifications function exactly.
     */
    public void sendTransferNotifications(Wallet senderWallet, Wallet receiverWallet, 
                                        BigDecimal amount, BankTransfer transferInstance) {
        try {
            // Send notifications to sender
            sendSenderNotifications(senderWallet, receiverWallet, amount, transferInstance);
            
            // Send notifications to receiver
            sendReceiverNotifications(senderWallet, receiverWallet, amount, transferInstance);
            
            logger.info("Notifications logged for transfer {}", transferInstance.getId());
            
        } catch (Exception e) {
            logger.error("Error sending notifications for transfer {}: {}", 
                transferInstance.getId(), e.getMessage());
            // Don't fail the transfer if notifications fail
        }
    }
    
    private void sendSenderNotifications(Wallet senderWallet, Wallet receiverWallet, 
                                       BigDecimal amount, BankTransfer transferInstance) {
        User sender = senderWallet.getUser();
        
        // Email notification for sender
        String senderEmail = sender.getEmail();
        if (senderEmail != null && !senderEmail.isEmpty()) {
            try {
                String subject = "Transfer Sent Successfully";
                String message = String.format(
                    "Dear %s,\n\n" +
                    "Your transfer of %s to account %s has been completed successfully.\n\n" +
                    "Transfer Details:\n" +
                    "Amount: %s\n" +
                    "Recipient Account: %s\n" +
                    "Reference: %s\n" +
                    "Date: %s\n\n" +
                    "Thank you for using XyPay.",
                    this.getFullNameOrUsername(sender),
                    amount,
                    receiverWallet.getAccountNumber(),
                    amount,
                    receiverWallet.getAccountNumber(),
                    transferInstance.getReference(),
                    java.time.LocalDateTime.now().toString()
                );
                
                boolean emailSent = emailNotificationService.sendEmailNotification(senderEmail, subject, message);
                if (emailSent) {
                    logger.info("Transfer sent email notification sent to: {}", senderEmail);
                } else {
                    logger.warn("Failed to send transfer sent email to: {}", senderEmail);
                }
            } catch (Exception e) {
                logger.error("Error sending transfer sent email to {}: {}", senderEmail, e.getMessage());
            }
        }
        
        // SMS notification for sender
        String senderPhone = getUserPhone(sender);
        if (senderPhone != null && !senderPhone.isEmpty()) {
            try {
                String smsMessage = String.format(
                    "XyPay: Transfer of %s to %s completed successfully. Ref: %s",
                    amount,
                    receiverWallet.getAccountNumber(),
                    transferInstance.getReference()
                );
                
                boolean smsSent = smsNotificationService.sendSMSNotification(senderPhone, smsMessage);
                if (smsSent) {
                    logger.info("Transfer sent SMS notification sent to: {}", senderPhone);
                } else {
                    logger.warn("Failed to send transfer sent SMS to: {}", senderPhone);
                }
            } catch (Exception e) {
                logger.error("Error sending transfer sent SMS to {}: {}", senderPhone, e.getMessage());
            }
        }
    }
    
    private void sendReceiverNotifications(Wallet senderWallet, Wallet receiverWallet, 
                                         BigDecimal amount, BankTransfer transferInstance) {
        User receiver = receiverWallet.getUser();
        
        // Email notification for receiver
        String receiverEmail = receiver.getEmail();
        if (receiverEmail != null && !receiverEmail.isEmpty()) {
            try {
                String subject = "Money Received";
                String message = String.format(
                    "Dear %s,\n\n" +
                    "You have received %s from account %s.\n\n" +
                    "Transfer Details:\n" +
                    "Amount: %s\n" +
                    "Sender Account: %s\n" +
                    "Reference: %s\n" +
                    "Date: %s\n" +
                    "New Balance: %s\n\n" +
                    "Thank you for using XyPay.",
                    this.getFullNameOrUsername(receiver),
                    amount,
                    senderWallet.getAccountNumber(),
                    amount,
                    senderWallet.getAccountNumber(),
                    transferInstance.getReference(),
                    java.time.LocalDateTime.now().toString(),
                    receiverWallet.getBalance()
                );
                
                boolean emailSent = emailNotificationService.sendEmailNotification(receiverEmail, subject, message);
                if (emailSent) {
                    logger.info("Transfer received email notification sent to: {}", receiverEmail);
                } else {
                    logger.warn("Failed to send transfer received email to: {}", receiverEmail);
                }
            } catch (Exception e) {
                logger.error("Error sending transfer received email to {}: {}", receiverEmail, e.getMessage());
            }
        }
        
        // SMS notification for receiver
        String receiverPhone = getUserPhone(receiver);
        if (receiverPhone != null && !receiverPhone.isEmpty()) {
            try {
                String smsMessage = String.format(
                    "XyPay: You received %s from %s. Ref: %s. New balance: %s",
                    amount,
                    senderWallet.getAccountNumber(),
                    transferInstance.getReference(),
                    receiverWallet.getBalance()
                );
                
                boolean smsSent = smsNotificationService.sendSMSNotification(receiverPhone, smsMessage);
                if (smsSent) {
                    logger.info("Transfer received SMS notification sent to: {}", receiverPhone);
                } else {
                    logger.warn("Failed to send transfer received SMS to: {}", receiverPhone);
                }
            } catch (Exception e) {
                logger.error("Error sending transfer received SMS to {}: {}", receiverPhone, e.getMessage());
            }
        }
    }
    
    private String getUserPhone(User user) {
        try {
            Optional<UserProfile> profileOpt = userProfileRepository.findByUser(user);
            return profileOpt.map(UserProfile::getPhone).orElse(null);
        } catch (Exception e) {
            logger.warn("Error getting phone for user {}: {}", user.getId(), e.getMessage());
            return null;
        }
    }
    
    private String getFullNameOrUsername(User user) {
        if (user.getFirstName() != null && user.getLastName() != null) {
            return user.getFirstName() + " " + user.getLastName();
        } else if (user.getFirstName() != null) {
            return user.getFirstName();
        } else {
            return user.getUsername();
        }
    }
}
