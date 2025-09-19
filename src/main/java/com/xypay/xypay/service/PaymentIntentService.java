package com.xypay.xypay.service;

import com.xypay.xypay.domain.NotificationLevel;
import com.xypay.xypay.domain.NotificationType;
import com.xypay.xypay.domain.PaymentIntent;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.domain.Wallet;
import com.xypay.xypay.repository.PaymentIntentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Service
@Transactional
public class PaymentIntentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentIntentService.class);

    @Autowired
    private PaymentIntentRepository paymentIntentRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private WalletService walletService;

    /**
     * Create a new payment intent
     */
    public PaymentIntent createPaymentIntent(User user, String orderId, String merchantId, 
            BigDecimal amount, String currency, String description) {
        logger.info("Creating payment intent for user {} with amount {} {}", 
            user.getUsername(), amount, currency);

        PaymentIntent paymentIntent = new PaymentIntent();
        paymentIntent.setUser(user);
        paymentIntent.setOrderId(orderId);
        paymentIntent.setMerchantId(merchantId);
        paymentIntent.setAmount(amount);
        paymentIntent.setCurrency(currency);
        paymentIntent.setDescription(description);
        paymentIntent.setReference(generateReference());
        paymentIntent.setStatus("pending");

        paymentIntent = paymentIntentRepository.save(paymentIntent);
        logger.info("Payment intent created with ID {}", paymentIntent.getId());

        return paymentIntent;
    }

    /**
     * Confirm payment intent
     */
    public PaymentIntent confirmPaymentIntent(UUID paymentIntentId) {
        logger.info("Confirming payment intent {}", paymentIntentId);

        PaymentIntent paymentIntent = paymentIntentRepository.findById(paymentIntentId)
            .orElseThrow(() -> new RuntimeException("Payment intent not found"));

        if (!"pending".equals(paymentIntent.getStatus())) {
            throw new RuntimeException("Payment intent is not in pending status");
        }

        // Check wallet balance
        Optional<Wallet> wallet = walletService.getUserPrimaryWallet(paymentIntent.getUser());
        if (wallet.isEmpty() || !walletService.hasSufficientBalance(wallet.get().getId(), paymentIntent.getAmount())) { // Use UUID directly
            paymentIntent.fail("Insufficient balance");
            paymentIntentRepository.save(paymentIntent);
            throw new RuntimeException("Insufficient balance");
        }

        paymentIntent.confirm();
        paymentIntent = paymentIntentRepository.save(paymentIntent);

        // Debit wallet
        walletService.debitWallet(wallet.get().getId(), paymentIntent.getAmount(), 
            "Payment for order " + paymentIntent.getOrderId()); // Use UUID directly

        // Send notification
        sendPaymentNotification(paymentIntent, "confirmed");

        logger.info("Payment intent {} confirmed successfully", paymentIntentId);
        return paymentIntent;
    }

    /**
     * Cancel payment intent
     */
    public PaymentIntent cancelPaymentIntent(UUID paymentIntentId, String reason) {
        logger.info("Cancelling payment intent {} with reason: {}", paymentIntentId, reason);

        PaymentIntent paymentIntent = paymentIntentRepository.findById(paymentIntentId)
            .orElseThrow(() -> new RuntimeException("Payment intent not found"));

        paymentIntent.cancel(reason);
        paymentIntent = paymentIntentRepository.save(paymentIntent);

        // Send notification
        sendPaymentNotification(paymentIntent, "cancelled");

        logger.info("Payment intent {} cancelled successfully", paymentIntentId);
        return paymentIntent;
    }

    /**
     * Fail payment intent
     */
    public PaymentIntent failPaymentIntent(UUID paymentIntentId, String reason) {
        logger.info("Failing payment intent {} with reason: {}", paymentIntentId, reason);

        PaymentIntent paymentIntent = paymentIntentRepository.findById(paymentIntentId)
            .orElseThrow(() -> new RuntimeException("Payment intent not found"));

        paymentIntent.fail(reason);
        paymentIntent = paymentIntentRepository.save(paymentIntent);

        // Send notification
        sendPaymentNotification(paymentIntent, "failed");

        logger.info("Payment intent {} failed", paymentIntentId);
        return paymentIntent;
    }

    /**
     * Get payment intent by ID
     */
    @Transactional(readOnly = true)
    public Optional<PaymentIntent> getPaymentIntent(UUID paymentIntentId) {
        return paymentIntentRepository.findById(paymentIntentId);
    }

    /**
     * Get payment intents by user
     */
    @Transactional(readOnly = true)
    public List<PaymentIntent> getUserPaymentIntents(User user) {
        return paymentIntentRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * Get payment intents by merchant
     */
    @Transactional(readOnly = true)
    public List<PaymentIntent> getMerchantPaymentIntents(String merchantId) {
        return paymentIntentRepository.findByMerchantIdOrderByCreatedAtDesc(merchantId);
    }

    /**
     * Get pending payment intents
     */
    @Transactional(readOnly = true)
    public List<PaymentIntent> getPendingPaymentIntents() {
        return paymentIntentRepository.findByStatusOrderByCreatedAtDesc("pending");
    }

    /**
     * Get expired payment intents
     */
    @Transactional(readOnly = true)
    public List<PaymentIntent> getExpiredPaymentIntents() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        return paymentIntentRepository.findExpiredPendingIntents(oneHourAgo);
    }

    /**
     * Process expired payment intents
     */
    public void processExpiredPaymentIntents() {
        List<PaymentIntent> expiredIntents = getExpiredPaymentIntents();
        
        for (PaymentIntent intent : expiredIntents) {
            try {
                failPaymentIntent(intent.getId(), "Payment intent expired");
                logger.info("Expired payment intent {} processed", intent.getId());
            } catch (Exception e) {
                logger.error("Failed to process expired payment intent {}: {}", 
                    intent.getId(), e.getMessage());
            }
        }
    }

    /**
     * Generate payment reference
     */
    private String generateReference() {
        return "PI_" + System.currentTimeMillis() + "_" + java.util.UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Send payment notification
     */
    private void sendPaymentNotification(PaymentIntent paymentIntent, String action) {
        try {
            String title = String.format("Payment %s", action.substring(0, 1).toUpperCase() + action.substring(1));
            String message = String.format("Your payment of %s %s for order %s has been %s.",
                paymentIntent.getAmount(), paymentIntent.getCurrency(), 
                paymentIntent.getOrderId(), action);

            NotificationLevel level = "failed".equals(action) ? NotificationLevel.ERROR : NotificationLevel.INFO;

            notificationService.createBankingNotification(
                paymentIntent.getUser(),
                title,
                message,
                NotificationType.PAYMENT_SUCCESS,
                level,
                paymentIntent
            );
        } catch (Exception e) {
            logger.error("Failed to send payment notification: {}", e.getMessage());
        }
    }
}
