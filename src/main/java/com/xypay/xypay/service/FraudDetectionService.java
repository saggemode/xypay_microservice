package com.xypay.xypay.service;

import com.xypay.xypay.domain.BankTransfer;
import com.xypay.xypay.domain.FraudDetection;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.domain.NotificationType;
import com.xypay.xypay.domain.NotificationLevel;
import com.xypay.xypay.repository.FraudDetectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Service
@Transactional
public class FraudDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(FraudDetectionService.class);

    @Autowired
    private FraudDetectionRepository fraudDetectionRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Analyze a bank transfer for fraud indicators
     */
    public FraudDetection analyzeTransfer(BankTransfer transfer) {
        logger.info("Analyzing transfer {} for fraud indicators", transfer.getId());

        int riskScore = calculateRiskScore(transfer);
        String fraudType = determineFraudType(transfer, riskScore);
        String flag = determineRiskFlag(riskScore);
        String description = generateFraudDescription(transfer, fraudType, riskScore);

        FraudDetection fraudDetection = new FraudDetection(
            transfer.getUser(), 
            fraudType, 
            riskScore, 
            description
        );
        fraudDetection.setTransfer(transfer);
        fraudDetection.setFlag(flag);

        fraudDetection = fraudDetectionRepository.save(fraudDetection);

        // Send notifications for high-risk cases
        if (riskScore >= 70) {
            sendFraudAlert(fraudDetection);
        }

        logger.info("Fraud analysis completed for transfer {} with risk score {}", 
            transfer.getId(), riskScore);

        return fraudDetection;
    }

    /**
     * Calculate risk score based on various factors
     */
    private int calculateRiskScore(BankTransfer transfer) {
        int score = 0;

        // Amount-based scoring
        BigDecimal amount = transfer.getAmount();
        if (amount.compareTo(new BigDecimal("1000000")) > 0) { // > 1M
            score += 30;
        } else if (amount.compareTo(new BigDecimal("500000")) > 0) { // > 500K
            score += 20;
        } else if (amount.compareTo(new BigDecimal("100000")) > 0) { // > 100K
            score += 10;
        }

        // Time-based scoring (unusual hours)
        int hour = LocalDateTime.now().getHour();
        if (hour < 6 || hour > 22) {
            score += 15;
        }

        // Device and location scoring
        if (transfer.getDeviceFingerprint() == null) {
            score += 20;
        }

        if (transfer.getIpAddress() == null) {
            score += 10;
        }

        // Velocity scoring - check recent transfers
        Long recentTransfers = getRecentTransferCount(transfer.getUser());
        if (recentTransfers > 10) {
            score += 25;
        } else if (recentTransfers > 5) {
            score += 15;
        }

        // Suspicious flags
        if (Boolean.TRUE.equals(transfer.getSuspicious())) {
            score += 40;
        }

        return Math.min(score, 100); // Cap at 100
    }

    /**
     * Determine fraud type based on transfer characteristics
     */
    private String determineFraudType(BankTransfer transfer, int riskScore) {
        if (riskScore >= 80) {
            return "account_takeover_pattern";
        } else if (riskScore >= 60) {
            return "velocity_check";
        } else if (transfer.getDeviceFingerprint() == null) {
            return "device_mismatch";
        } else if (transfer.getIpAddress() == null) {
            return "ip_anomaly";
        } else {
            return "amount_anomaly";
        }
    }

    /**
     * Determine risk flag based on score
     */
    private String determineRiskFlag(int riskScore) {
        if (riskScore >= 90) {
            return "CRITICAL";
        } else if (riskScore >= 70) {
            return "HIGH";
        } else if (riskScore >= 50) {
            return "MEDIUM";
        } else if (riskScore >= 30) {
            return "LOW";
        } else {
            return "NORMAL";
        }
    }

    /**
     * Generate fraud description
     */
    private String generateFraudDescription(BankTransfer transfer, String fraudType, int riskScore) {
        return String.format("Fraud detection triggered for transfer %s. Type: %s, Risk Score: %d. " +
            "Amount: %s, Time: %s", 
            transfer.getReference(), fraudType, riskScore, 
            transfer.getAmount(), LocalDateTime.now());
    }

    /**
     * Get recent transfer count for velocity checking
     */
    private Long getRecentTransferCount(User user) {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        return fraudDetectionRepository.countByUserSince(user, oneDayAgo);
    }

    /**
     * Send fraud alert notification
     */
    private void sendFraudAlert(FraudDetection fraudDetection) {
        try {
            // Create high-priority notification
            notificationService.createBankingNotification(
                fraudDetection.getUser(),
                "Fraud Alert",
                "Suspicious activity detected on your account. Please review immediately.",
                NotificationType.SECURITY_ALERT,
                NotificationLevel.ERROR,
                fraudDetection
            );

            logger.info("Fraud alert sent for detection {}", fraudDetection.getId());
        } catch (Exception e) {
            logger.error("Failed to send fraud alert for detection {}: {}", 
                fraudDetection.getId(), e.getMessage());
        }
    }

    /**
     * Resolve fraud detection
     */
    public FraudDetection resolveFraudDetection(Long fraudDetectionId, User resolvedBy, String notes) {
        FraudDetection fraudDetection = fraudDetectionRepository.findById(fraudDetectionId)
            .orElseThrow(() -> new RuntimeException("Fraud detection not found"));

        fraudDetection.resolve(resolvedBy, notes);
        return fraudDetectionRepository.save(fraudDetection);
    }

    /**
     * Get unresolved fraud detections for user
     */
    @Transactional(readOnly = true)
    public List<FraudDetection> getUnresolvedFraudDetections(User user) {
        return fraudDetectionRepository.findByUserAndIsResolvedFalseOrderByCreatedAtDesc(user);
    }

    /**
     * Get high-risk unresolved fraud detections
     */
    @Transactional(readOnly = true)
    public List<FraudDetection> getHighRiskUnresolved() {
        return fraudDetectionRepository.findHighRiskUnresolved(70);
    }

    /**
     * Get critical unresolved fraud detections
     */
    @Transactional(readOnly = true)
    public List<FraudDetection> getCriticalUnresolved() {
        return fraudDetectionRepository.findCriticalUnresolved();
    }

    /**
     * Get fraud detections with pagination
     */
    @Transactional(readOnly = true)
    public Page<FraudDetection> getFraudDetections(Pageable pageable) {
        return fraudDetectionRepository.findByIsResolvedFalseOrderByRiskScoreDescCreatedAtDesc(pageable);
    }

    /**
     * Get user's average risk score
     */
    @Transactional(readOnly = true)
    public Double getUserAverageRiskScore(User user) {
        return fraudDetectionRepository.getAverageRiskScoreForUser(user);
    }
}
