package com.xypay.xypay.service;

import com.xypay.xypay.domain.BankTransfer;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.repository.BankTransferRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for fraud detection and risk assessment.
 * Implements real-time ML-based transaction monitoring and fraud detection.
 */
@Service
public class FraudDetectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(FraudDetectionService.class);
    
    @Autowired
    private BankTransferRepository bankTransferRepository;
    
    // Time windows for pattern analysis (in minutes)
    private static final int SHORT_WINDOW = 5;  // 5 minutes
    private static final int MEDIUM_WINDOW = 60;  // 1 hour
    private static final int LONG_WINDOW = 1440;  // 24 hours
    
    // Thresholds for different fraud patterns
    private static final int VELOCITY_THRESHOLD = 5;  // Max transactions per SHORT_WINDOW
    private static final double AMOUNT_VARIANCE_THRESHOLD = 3.0;  // Standard deviations from mean
    private static final Set<String> SUSPICIOUS_COUNTRY_CODES = Set.of("NG", "GH", "KE", "ZA");  // High-risk countries
    
    /**
     * Analyze transaction patterns in real-time using ML-based detection.
     * 
     * @param user User making the transfer
     * @param amount Transfer amount
     * @param recipientAccount Recipient account number
     * @param ipAddress IP address of the request
     * @param deviceFingerprint Device fingerprint
     * @return Analysis results with risk factors
     */
    public Map<String, Object> analyzeTransactionPatterns(User user, BigDecimal amount, String recipientAccount, 
                                                         String ipAddress, String deviceFingerprint) {
        try {
            List<Map<String, Object>> riskFactors = new ArrayList<>();
            int riskScore = 0;
            
            // 1. Velocity Check - Unusual number of transactions in short time
            LocalDateTime shortWindowStart = LocalDateTime.now().minus(SHORT_WINDOW, ChronoUnit.MINUTES);
            List<BankTransfer> recentTransfers = bankTransferRepository.findByUser(user);
            long recentTxns = recentTransfers.stream()
                .filter(t -> t.getCreatedAt().isAfter(shortWindowStart))
                .count();
            
            if (recentTxns >= VELOCITY_THRESHOLD) {
                Map<String, Object> factor = new HashMap<>();
                factor.put("type", "velocity_alert");
                factor.put("severity", "high");
                factor.put("details", String.format("Unusual transaction frequency: %d transactions in %d minutes", 
                    recentTxns, SHORT_WINDOW));
                riskFactors.add(factor);
                riskScore += 30;
            }
            
            // 2. Amount Pattern Analysis
            LocalDateTime longWindowStart = LocalDateTime.now().minus(LONG_WINDOW, ChronoUnit.MINUTES);
            List<BankTransfer> userTransactions = bankTransferRepository.findByUserAndStatus(user, "completed")
                .stream()
                .filter(t -> t.getCreatedAt().isAfter(longWindowStart))
                .toList();
            
            if (!userTransactions.isEmpty()) {
                double meanAmount = userTransactions.stream()
                    .mapToDouble(t -> t.getAmount().doubleValue())
                    .average()
                    .orElse(0.0);
                
                double variance = userTransactions.stream()
                    .mapToDouble(t -> Math.pow(t.getAmount().doubleValue() - meanAmount, 2))
                    .average()
                    .orElse(0.0);
                double stdDev = Math.sqrt(variance);
                
                if (amount.doubleValue() > meanAmount + (stdDev * AMOUNT_VARIANCE_THRESHOLD)) {
                    Map<String, Object> factor = new HashMap<>();
                    factor.put("type", "amount_anomaly");
                    factor.put("severity", "medium");
                    factor.put("details", "Transaction amount significantly higher than user pattern");
                    riskFactors.add(factor);
                    riskScore += 20;
                }
            }
            
            // 3. Location/IP Analysis
            try {
                // In production, integrate with GeoIP2 database
                // For now, we'll skip this check
                logger.debug("IP geolocation check skipped - not implemented");
            } catch (Exception e) {
                logger.warn("Error in IP geolocation: {}", e.getMessage());
            }
            
            // 4. Device Switching Pattern
            LocalDateTime mediumWindowStart = LocalDateTime.now().minus(MEDIUM_WINDOW, ChronoUnit.MINUTES);
            List<BankTransfer> mediumWindowTransfers = bankTransferRepository.findByUser(user)
                .stream()
                .filter(t -> t.getCreatedAt().isAfter(mediumWindowStart))
                .toList();
            Set<String> recentDevices = mediumWindowTransfers.stream()
                .map(BankTransfer::getDeviceFingerprint)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
            
            if (recentDevices.size() > 2) {
                Map<String, Object> factor = new HashMap<>();
                factor.put("type", "device_switching");
                factor.put("severity", "high");
                factor.put("details", "Multiple devices used in short time period");
                riskFactors.add(factor);
                riskScore += 35;
            }
            
            // 5. Recipient Risk Analysis
            List<BankTransfer> recipientTransfers = bankTransferRepository.findByAccountNumber(recipientAccount)
                .stream()
                .filter(t -> "completed".equals(t.getStatus()))
                .toList();
            long recipientHistory = recipientTransfers.stream()
                .map(BankTransfer::getUser)
                .distinct()
                .count();
            
            if (recipientHistory > 10) {
                Map<String, Object> factor = new HashMap<>();
                factor.put("type", "recipient_pattern");
                factor.put("severity", "low");
                factor.put("details", "Recipient account has unusual number of different senders");
                riskFactors.add(factor);
                riskScore += 15;
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("risk_score", Math.min(riskScore, 100));
            result.put("risk_factors", riskFactors);
            result.put("timestamp", LocalDateTime.now().toString());
            result.put("requires_review", riskScore > 70);
            result.put("analysis_id", UUID.randomUUID().toString());
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error in transaction pattern analysis: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("risk_score", 50);  // Default medium risk on error
            result.put("risk_factors", Collections.singletonList(Map.of(
                "type", "analysis_error",
                "severity", "medium",
                "details", "Error in pattern analysis"
            )));
            result.put("requires_review", true);  // Err on the side of caution
            return result;
        }
    }
    
    /**
     * Calculate comprehensive fraud score using ML-based pattern analysis.
     * 
     * @param user User making the transfer
     * @param amount Transfer amount
     * @param recipientAccount Recipient account number
     * @param recipientBankCode Recipient bank code
     * @param deviceFingerprint Device fingerprint
     * @param ipAddress IP address
     * @return Fraud analysis result with score and risk factors
     */
    public int calculateFraudScore(User user, BigDecimal amount, String recipientAccount, 
                                 String recipientBankCode, String deviceFingerprint, String ipAddress) {
        try {
            // Get real-time pattern analysis
            Map<String, Object> patternAnalysis = analyzeTransactionPatterns(
                user, amount, recipientAccount, ipAddress, deviceFingerprint
            );
            
            // Initialize base score from pattern analysis
            int score = (Integer) patternAnalysis.get("risk_score");
            
            // Check if amount is unusually high for the user
            try {
                List<BankTransfer> completedTransfers = bankTransferRepository.findByUserAndStatus(user, "completed");
                if (!completedTransfers.isEmpty()) {
                    double avgTransfer = completedTransfers.stream()
                        .mapToDouble(t -> t.getAmount().doubleValue())
                        .average()
                        .orElse(0.0);
                    
                    if (amount.doubleValue() > (avgTransfer * 3)) {  // If amount is 3x higher than average
                        score += 20;
                    }
                    if (amount.doubleValue() > (avgTransfer * 5)) {  // If amount is 5x higher than average
                        score += 20;
                    }
                }
            } catch (Exception e) {
                logger.warn("Error calculating average transfer: {}", e.getMessage());
            }
            
            // Check if recipient is new
            try {
            List<BankTransfer> userTransfers = bankTransferRepository.findByUser(user);
            long previousTransfers = userTransfers.stream()
                .filter(t -> recipientAccount.equals(t.getAccountNumber()) && 
                           recipientBankCode.equals(t.getBankCode()) && 
                           "completed".equals(t.getStatus()))
                .count();
                
                if (previousTransfers == 0) {  // New recipient
                    score += 15;
                }
            } catch (Exception e) {
                logger.warn("Error checking previous transfers: {}", e.getMessage());
            }
            
            // Check device fingerprint
            try {
                List<BankTransfer> userTransfers = bankTransferRepository.findByUser(user);
                long deviceTransfers = userTransfers.stream()
                    .filter(t -> deviceFingerprint.equals(t.getDeviceFingerprint()))
                    .count();
                
                if (deviceTransfers == 0) {  // New device
                    score += 25;
                }
            } catch (Exception e) {
                logger.warn("Error checking device transfers: {}", e.getMessage());
            }
            
            // Check IP address
            try {
                List<BankTransfer> userTransfers = bankTransferRepository.findByUser(user);
                long ipTransfers = userTransfers.stream()
                    .filter(t -> ipAddress.equals(t.getIpAddress()))
                    .count();
                
                if (ipTransfers == 0) {  // New IP
                    score += 20;
                }
            } catch (Exception e) {
                logger.warn("Error checking IP transfers: {}", e.getMessage());
            }
            
            return Math.min(score, 100);  // Cap score at 100
            
        } catch (Exception e) {
            logger.error("Error calculating fraud score: {}", e.getMessage());
            return 50;  // Return medium risk score on error
        }
    }
    
    /**
     * Determine if 2FA should be required for a transfer.
     * 
     * @param user User making the transfer
     * @param amount Transfer amount
     * @param fraudScore Calculated fraud score
     * @return True if 2FA should be required
     */
    public boolean shouldRequire2fa(User user, BigDecimal amount, int fraudScore) {
        try {
            // Get user's average transfer amount
            List<BankTransfer> completedTransfers = bankTransferRepository.findByUserAndStatus(user, "completed");
            double avgTransfer = completedTransfers.stream()
                .mapToDouble(t -> t.getAmount().doubleValue())
                .average()
                .orElse(0.0);
            
            // Require 2FA if:
            // 1. Fraud score is high (>70)
            // 2. Amount is significantly higher than average (>3x)
            // 3. Amount is large (>1M NGN)
            return (fraudScore > 70 ||
                    amount.doubleValue() > (avgTransfer * 3) ||
                    amount.doubleValue() > 1000000);  // 1M NGN
            
        } catch (Exception e) {
            logger.error("Error determining 2FA requirement: {}", e.getMessage());
            return true;  // Require 2FA on error to be safe
        }
    }
    
    /**
     * Determine if staff approval should be required for a transfer.
     * 
     * @param user User making the transfer
     * @param amount Transfer amount
     * @param fraudScore Calculated fraud score
     * @return True if staff approval should be required
     */
    public boolean shouldRequireApproval(User user, BigDecimal amount, int fraudScore) {
        try {
            // Get user's average transfer amount
            List<BankTransfer> completedTransfers = bankTransferRepository.findByUserAndStatus(user, "completed");
            double avgTransfer = completedTransfers.stream()
                .mapToDouble(t -> t.getAmount().doubleValue())
                .average()
                .orElse(0.0);
            
            // Require approval if:
            // 1. Fraud score is very high (>85)
            // 2. Amount is very high (>5M NGN)
            // 3. Amount is significantly higher than average (>5x)
            return (fraudScore > 85 ||
                    amount.doubleValue() > 5000000 ||  // 5M NGN
                    amount.doubleValue() > (avgTransfer * 5));
            
        } catch (Exception e) {
            logger.error("Error determining approval requirement: {}", e.getMessage());
            return true;  // Require approval on error to be safe
        }
    }
    
    /**
     * Get list of fraud flags for a transfer.
     * 
     * @param user User making the transfer
     * @param amount Transfer amount
     * @param recipientAccount Recipient account number
     * @return List of fraud flags
     */
    public List<String> getFraudFlags(User user, BigDecimal amount, String recipientAccount) {
        List<String> flags = new ArrayList<>();
        
        try {
            // Check if amount is unusually high
            List<BankTransfer> completedTransfers = bankTransferRepository.findByUserAndStatus(user, "completed");
            if (!completedTransfers.isEmpty()) {
                double avgTransfer = completedTransfers.stream()
                    .mapToDouble(t -> t.getAmount().doubleValue())
                    .average()
                    .orElse(0.0);
                
                if (amount.doubleValue() > (avgTransfer * 3)) {
                    flags.add("unusual_amount");
                }
            }
            
            // Check if recipient is new
            List<BankTransfer> userTransfers = bankTransferRepository.findByUser(user);
            boolean hasPreviousTransfers = userTransfers.stream()
                .anyMatch(t -> recipientAccount.equals(t.getAccountNumber()) && 
                              "completed".equals(t.getStatus()));
            
            if (!hasPreviousTransfers) {
                flags.add("new_recipient");
            }
            
            // Check for multiple transfers in short time
            LocalDateTime fiveMinutesAgo = LocalDateTime.now().minus(5, ChronoUnit.MINUTES);
            long recentTransfers = userTransfers.stream()
                .filter(t -> t.getCreatedAt().isAfter(fiveMinutesAgo))
                .count();
            
            if (recentTransfers > 3) {
                flags.add("rapid_transfers");
            }
            
            return flags;
            
        } catch (Exception e) {
            logger.error("Error getting fraud flags: {}", e.getMessage());
            return Collections.singletonList("error_checking_flags");
        }
    }
}