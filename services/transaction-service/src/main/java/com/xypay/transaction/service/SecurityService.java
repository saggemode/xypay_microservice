package com.xypay.transaction.service;

import com.xypay.transaction.dto.TransactionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityService {
    
    private final Map<String, Integer> failedAttempts = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastAttempt = new ConcurrentHashMap<>();
    
    public SecurityResult validatePIN(String accountNumber, String pin) {
        log.info("Validating PIN for account: {}", accountNumber);
        
        try {
            // Check for brute force attempts
            if (isAccountLocked(accountNumber)) {
                return SecurityResult.failed("Account temporarily locked due to multiple failed PIN attempts");
            }
            
            // Validate PIN (this would integrate with your PIN service)
            boolean isValid = validatePINWithService(accountNumber, pin);
            
            if (isValid) {
                // Reset failed attempts on successful validation
                failedAttempts.remove(accountNumber);
                lastAttempt.remove(accountNumber);
                return SecurityResult.passed("PIN validation successful");
            } else {
                // Increment failed attempts
                incrementFailedAttempts(accountNumber);
                return SecurityResult.failed("Invalid PIN");
            }
            
        } catch (Exception e) {
            log.error("PIN validation failed: {}", e.getMessage(), e);
            return SecurityResult.failed("PIN validation failed: " + e.getMessage());
        }
    }
    
    public SecurityResult validateOTP(String accountNumber, String otp) {
        log.info("Validating OTP for account: {}", accountNumber);
        
        try {
            // Validate OTP (this would integrate with your OTP service)
            boolean isValid = validateOTPWithService(accountNumber, otp);
            
            if (isValid) {
                return SecurityResult.passed("OTP validation successful");
            } else {
                return SecurityResult.failed("Invalid or expired OTP");
            }
            
        } catch (Exception e) {
            log.error("OTP validation failed: {}", e.getMessage(), e);
            return SecurityResult.failed("OTP validation failed: " + e.getMessage());
        }
    }
    
    public SecurityResult validate2FA(String accountNumber, String pin, String otp) {
        log.info("Validating 2FA for account: {}", accountNumber);
        
        try {
            // Validate both PIN and OTP
            SecurityResult pinResult = validatePIN(accountNumber, pin);
            if (!pinResult.isPassed()) {
                return pinResult;
            }
            
            SecurityResult otpResult = validateOTP(accountNumber, otp);
            if (!otpResult.isPassed()) {
                return otpResult;
            }
            
            return SecurityResult.passed("2FA validation successful");
            
        } catch (Exception e) {
            log.error("2FA validation failed: {}", e.getMessage(), e);
            return SecurityResult.failed("2FA validation failed: " + e.getMessage());
        }
    }
    
    public SecurityResult validateHighValueTransaction(TransactionRequest request) {
        log.info("Validating high-value transaction: {} amount: {}", request.getReference(), request.getAmount());
        
        try {
            BigDecimal highValueThreshold = new BigDecimal("100000"); // ₦100,000
            
            if (request.getAmount().compareTo(highValueThreshold) > 0) {
                // High-value transaction requires additional security
                if (request.getPin() == null || request.getPin().isEmpty()) {
                    return SecurityResult.failed("PIN required for high-value transactions");
                }
                
                if (request.getOtp() == null || request.getOtp().isEmpty()) {
                    return SecurityResult.failed("OTP required for high-value transactions");
                }
                
                // Validate 2FA for high-value transactions
                return validate2FA(request.getAccountNumber(), request.getPin(), request.getOtp());
            }
            
            return SecurityResult.passed("Transaction amount below high-value threshold");
            
        } catch (Exception e) {
            log.error("High-value transaction validation failed: {}", e.getMessage(), e);
            return SecurityResult.failed("High-value transaction validation failed: " + e.getMessage());
        }
    }
    
    public SecurityResult validateFraudDetection(TransactionRequest request) {
        log.info("Running fraud detection for transaction: {}", request.getReference());
        
        try {
            // Check for suspicious patterns
            if (isSuspiciousAmount(request.getAmount())) {
                return SecurityResult.warning("Transaction amount flagged for review");
            }
            
            if (isSuspiciousTime(request.getTimestamp())) {
                return SecurityResult.warning("Transaction time flagged for review");
            }
            
            if (isSuspiciousLocation(request.getLocation(), request.getAccountNumber())) {
                return SecurityResult.warning("Transaction location flagged for review");
            }
            
            if (isSuspiciousDevice(request.getDeviceId(), request.getAccountNumber())) {
                return SecurityResult.warning("Transaction device flagged for review");
            }
            
            return SecurityResult.passed("Fraud detection passed");
            
        } catch (Exception e) {
            log.error("Fraud detection failed: {}", e.getMessage(), e);
            return SecurityResult.failed("Fraud detection failed: " + e.getMessage());
        }
    }
    
    private boolean validatePINWithService(String accountNumber, String pin) {
        // This would integrate with your actual PIN validation service
        // For now, we'll simulate validation
        return pin != null && pin.length() >= 4;
    }
    
    private boolean validateOTPWithService(String accountNumber, String otp) {
        // This would integrate with your actual OTP validation service
        // For now, we'll simulate validation
        return otp != null && otp.length() == 6;
    }
    
    private boolean isAccountLocked(String accountNumber) {
        Integer attempts = failedAttempts.get(accountNumber);
        if (attempts == null) {
            return false;
        }
        
        if (attempts >= 3) {
            LocalDateTime lastAttemptTime = lastAttempt.get(accountNumber);
            if (lastAttemptTime != null && lastAttemptTime.isAfter(LocalDateTime.now().minusMinutes(15))) {
                return true; // Locked for 15 minutes
            } else {
                // Reset after 15 minutes
                failedAttempts.remove(accountNumber);
                lastAttempt.remove(accountNumber);
                return false;
            }
        }
        
        return false;
    }
    
    private void incrementFailedAttempts(String accountNumber) {
        failedAttempts.merge(accountNumber, 1, Integer::sum);
        lastAttempt.put(accountNumber, LocalDateTime.now());
    }
    
    private boolean isSuspiciousAmount(BigDecimal amount) {
        // Check for round numbers (potential money laundering)
        return amount.remainder(new BigDecimal("1000")).compareTo(BigDecimal.ZERO) == 0;
    }
    
    private boolean isSuspiciousTime(LocalDateTime timestamp) {
        // Check for transactions outside business hours
        int hour = timestamp.getHour();
        return hour < 6 || hour > 22; // Suspicious if outside 6 AM - 10 PM
    }
    
    private boolean isSuspiciousLocation(String location, String accountNumber) {
        // Check for transactions from unusual locations
        // This would integrate with your location tracking service
        return false; // Simplified for now
    }
    
    private boolean isSuspiciousDevice(String deviceId, String accountNumber) {
        // Check for transactions from new or suspicious devices
        // This would integrate with your device tracking service
        return false; // Simplified for now
    }
    
    public static class SecurityResult {
        private final boolean passed;
        private final boolean warning;
        private final String message;
        private final String code;
        
        private SecurityResult(boolean passed, boolean warning, String message, String code) {
            this.passed = passed;
            this.warning = warning;
            this.message = message;
            this.code = code;
        }
        
        public static SecurityResult passed(String message) {
            return new SecurityResult(true, false, message, "PASSED");
        }
        
        public static SecurityResult warning(String message) {
            return new SecurityResult(true, true, message, "WARNING");
        }
        
        public static SecurityResult failed(String message) {
            return new SecurityResult(false, false, message, "FAILED");
        }
        
        public boolean isPassed() { return passed; }
        public boolean isWarning() { return warning; }
        public boolean isFailed() { return !passed; }
        public String getMessage() { return message; }
        public String getCode() { return code; }
    }
}
