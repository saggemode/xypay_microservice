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
    
    // TODO: Inject actual email and SMS services when implemented
    // @Autowired
    // private EmailService emailService;
    // @Autowired
    // private SmsService smsService;
    
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
            // TODO: Implement email sending logic
            logger.info("Would send email to sender {} for transfer {}", senderEmail, transferInstance.getId());
            // Example: emailService.sendTransferSentNotification(senderEmail, amount, 
            //     receiverWallet.getAccountNumber(), transferInstance.getReference());
        }
        
        // SMS notification for sender
        String senderPhone = getUserPhone(sender);
        if (senderPhone != null && !senderPhone.isEmpty()) {
            // TODO: Implement SMS sending logic
            logger.info("Would send SMS to sender {} for transfer {}", senderPhone, transferInstance.getId());
            // Example: smsService.sendTransferSentNotification(senderPhone, amount, 
            //     receiverWallet.getAccountNumber());
        }
    }
    
    private void sendReceiverNotifications(Wallet senderWallet, Wallet receiverWallet, 
                                         BigDecimal amount, BankTransfer transferInstance) {
        User receiver = receiverWallet.getUser();
        
        // Email notification for receiver
        String receiverEmail = receiver.getEmail();
        if (receiverEmail != null && !receiverEmail.isEmpty()) {
            // TODO: Implement email sending logic
            logger.info("Would send email to receiver {} for transfer {}", receiverEmail, transferInstance.getId());
            // Example: emailService.sendTransferReceivedNotification(receiverEmail, amount, 
            //     senderWallet.getAccountNumber(), transferInstance.getReference());
        }
        
        // SMS notification for receiver
        String receiverPhone = getUserPhone(receiver);
        if (receiverPhone != null && !receiverPhone.isEmpty()) {
            // TODO: Implement SMS sending logic
            logger.info("Would send SMS to receiver {} for transfer {}", receiverPhone, transferInstance.getId());
            // Example: smsService.sendTransferReceivedNotification(receiverPhone, amount, 
            //     senderWallet.getAccountNumber());
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
}
