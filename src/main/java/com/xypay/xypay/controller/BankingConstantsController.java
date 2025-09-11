package com.xypay.xypay.controller;

import com.xypay.xypay.util.BankingConstants;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test/constants")
public class BankingConstantsController {
    
    /**
     * Get all banking constants for testing purposes
     * @return ResponseEntity with all constants
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllConstants() {
        Map<String, Object> constants = new HashMap<>();
        
        // Transfer Statuses
        Map<String, String> transferStatuses = new HashMap<>();
        for (String[] choice : BankingConstants.TransferStatus.CHOICES) {
            transferStatuses.put(choice[0], choice[1]);
        }
        constants.put("transferStatuses", transferStatuses);
        
        // Transfer Types
        Map<String, String> transferTypes = new HashMap<>();
        for (String[] choice : BankingConstants.TransferType.CHOICES) {
            transferTypes.put(choice[0], choice[1]);
        }
        constants.put("transferTypes", transferTypes);
        
        // Transaction Types
        Map<String, String> transactionTypes = new HashMap<>();
        for (String[] choice : BankingConstants.TransactionType.CHOICES) {
            transactionTypes.put(choice[0], choice[1]);
        }
        constants.put("transactionTypes", transactionTypes);
        
        // Error Codes
        Map<String, String> errorCodes = new HashMap<>();
        for (String[] choice : BankingConstants.ErrorCodes.CHOICES) {
            errorCodes.put(choice[0], choice[1]);
        }
        constants.put("errorCodes", errorCodes);
        
        // Security Levels
        Map<String, String> securityLevels = new HashMap<>();
        for (String[] choice : BankingConstants.SecurityLevel.CHOICES) {
            securityLevels.put(choice[0], choice[1]);
        }
        constants.put("securityLevels", securityLevels);
        
        // Fraud Flags
        Map<String, String> fraudFlags = new HashMap<>();
        for (String[] choice : BankingConstants.FraudFlag.CHOICES) {
            fraudFlags.put(choice[0], choice[1]);
        }
        constants.put("fraudFlags", fraudFlags);
        
        // Transfer Limits
        Map<String, Object> transferLimits = new HashMap<>();
        transferLimits.put("DEFAULT_DAILY_LIMIT", BankingConstants.TransferLimits.DEFAULT_DAILY_LIMIT);
        transferLimits.put("DEFAULT_WEEKLY_LIMIT", BankingConstants.TransferLimits.DEFAULT_WEEKLY_LIMIT);
        transferLimits.put("DEFAULT_MONTHLY_LIMIT", BankingConstants.TransferLimits.DEFAULT_MONTHLY_LIMIT);
        transferLimits.put("HIGH_VALUE_THRESHOLD", BankingConstants.TransferLimits.HIGH_VALUE_THRESHOLD);
        transferLimits.put("STAFF_APPROVAL_THRESHOLD", BankingConstants.TransferLimits.STAFF_APPROVAL_THRESHOLD);
        transferLimits.put("MAX_TRANSFERS_PER_HOUR", BankingConstants.TransferLimits.MAX_TRANSFERS_PER_HOUR);
        transferLimits.put("MAX_TRANSFERS_PER_DAY", BankingConstants.TransferLimits.MAX_TRANSFERS_PER_DAY);
        transferLimits.put("MAX_AMOUNT_PER_HOUR", BankingConstants.TransferLimits.MAX_AMOUNT_PER_HOUR);
        transferLimits.put("MAX_AMOUNT_PER_DAY", BankingConstants.TransferLimits.MAX_AMOUNT_PER_DAY);
        constants.put("transferLimits", transferLimits);
        
        // Fee Structure
        Map<String, Object> feeStructure = new HashMap<>();
        feeStructure.put("INTERNAL_FEE_PERCENT", BankingConstants.FeeStructure.INTERNAL_FEE_PERCENT);
        feeStructure.put("INTERNAL_FEE_FIXED", BankingConstants.FeeStructure.INTERNAL_FEE_FIXED);
        feeStructure.put("EXTERNAL_FEE_PERCENT", BankingConstants.FeeStructure.EXTERNAL_FEE_PERCENT);
        feeStructure.put("EXTERNAL_FEE_FIXED", BankingConstants.FeeStructure.EXTERNAL_FEE_FIXED);
        feeStructure.put("VAT_RATE", BankingConstants.FeeStructure.VAT_RATE);
        feeStructure.put("LEVY_RATE", BankingConstants.FeeStructure.LEVY_RATE);
        constants.put("feeStructure", feeStructure);
        
        // Retry Configuration
        Map<String, Object> retryConfig = new HashMap<>();
        retryConfig.put("MAX_RETRIES", BankingConstants.RetryConfig.MAX_RETRIES);
        retryConfig.put("INITIAL_DELAY", BankingConstants.RetryConfig.INITIAL_DELAY);
        retryConfig.put("MAX_DELAY", BankingConstants.RetryConfig.MAX_DELAY);
        retryConfig.put("BACKOFF_MULTIPLIER", BankingConstants.RetryConfig.BACKOFF_MULTIPLIER);
        constants.put("retryConfig", retryConfig);
        
        // Circuit Breaker Configuration
        Map<String, Object> circuitBreakerConfig = new HashMap<>();
        circuitBreakerConfig.put("FAILURE_THRESHOLD", BankingConstants.CircuitBreakerConfig.FAILURE_THRESHOLD);
        circuitBreakerConfig.put("RECOVERY_TIMEOUT", BankingConstants.CircuitBreakerConfig.RECOVERY_TIMEOUT);
        constants.put("circuitBreakerConfig", circuitBreakerConfig);
        
        // API Response Codes
        constants.put("responseCodes", BankingConstants.ResponseCodes.class);
        
        // Notification Types
        constants.put("notificationTypes", BankingConstants.NotificationType.class);
        
        // Audit Event Types
        constants.put("auditEventTypes", BankingConstants.AuditEventType.class);
        
        // Device Fingerprinting
        Map<String, String> deviceFingerprints = new HashMap<>();
        for (String[] choice : BankingConstants.DeviceFingerprint.CHOICES) {
            deviceFingerprints.put(choice[0], choice[1]);
        }
        constants.put("deviceFingerprints", deviceFingerprints);
        
        // IP Whitelist Status
        Map<String, String> ipWhitelistStatuses = new HashMap<>();
        for (String[] choice : BankingConstants.IPWhitelistStatus.CHOICES) {
            ipWhitelistStatuses.put(choice[0], choice[1]);
        }
        constants.put("ipWhitelistStatuses", ipWhitelistStatuses);
        
        // Scheduled Frequencies
        Map<String, String> scheduledFrequencies = new HashMap<>();
        for (String[] choice : BankingConstants.ScheduledFrequency.CHOICES) {
            scheduledFrequencies.put(choice[0], choice[1]);
        }
        constants.put("scheduledFrequencies", scheduledFrequencies);
        
        // Bulk Transfer Status
        Map<String, String> bulkTransferStatuses = new HashMap<>();
        for (String[] choice : BankingConstants.BulkTransferStatus.CHOICES) {
            bulkTransferStatuses.put(choice[0], choice[1]);
        }
        constants.put("bulkTransferStatuses", bulkTransferStatuses);
        
        // Escrow Status
        Map<String, String> escrowStatuses = new HashMap<>();
        for (String[] choice : BankingConstants.EscrowStatus.CHOICES) {
            escrowStatuses.put(choice[0], choice[1]);
        }
        constants.put("escrowStatuses", escrowStatuses);
        
        return ResponseEntity.ok(constants);
    }
}