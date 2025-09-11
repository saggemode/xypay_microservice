package com.xypay.xypay.util;

/**
 * Constants for the bank application.
 */
public class BankingConstants {
    
    // Bank Transfer Statuses
    public static class TransferStatus {
        public static final String PENDING = "pending";
        public static final String PROCESSING = "processing";
        public static final String COMPLETED = "completed";
        public static final String FAILED = "failed";
        public static final String CANCELLED = "cancelled";
        public static final String REVERSED = "reversed";
        public static final String APPROVAL_REQUIRED = "approval_required";
        public static final String APPROVED = "approved";
        public static final String REJECTED = "rejected";
        
        // Array of choices for transfer statuses
        public static final String[][] CHOICES = {
            {PENDING, "Pending"},
            {PROCESSING, "Processing"},
            {COMPLETED, "Completed"},
            {FAILED, "Failed"},
            {CANCELLED, "Cancelled"},
            {REVERSED, "Reversed"},
            {APPROVAL_REQUIRED, "Approval Required"},
            {APPROVED, "Approved"},
            {REJECTED, "Rejected"}
        };
    }
    
    // Transfer Types
    public static class TransferType {
        public static final String INTERNAL = "intra";
        public static final String EXTERNAL = "inter";
        public static final String BULK = "bulk";
        public static final String SCHEDULED = "scheduled";
        public static final String RECURRING = "recurring";
        
        // Array of choices for transfer types
        public static final String[][] CHOICES = {
            {INTERNAL, "Internal Transfer"},
            {EXTERNAL, "External Transfer"},
            {BULK, "Bulk Transfer"},
            {SCHEDULED, "Scheduled Transfer"},
            {RECURRING, "Recurring Transfer"}
        };
    }
    
    // Transaction Types
    public static class TransactionType {
        public static final String CREDIT = "credit";
        public static final String DEBIT = "debit";
        public static final String FEE = "fee";
        public static final String VAT = "vat";
        public static final String LEVY = "levy";
        public static final String REVERSAL = "reversal";
        
        // Array of choices for transaction types
        public static final String[][] CHOICES = {
            {CREDIT, "Credit"},
            {DEBIT, "Debit"},
            {FEE, "Fee"},
            {VAT, "VAT"},
            {LEVY, "Levy"},
            {REVERSAL, "Reversal"}
        };
    }
    
    // Error Codes for Banking Operations
    public static class ErrorCodes {
        // Transaction Errors
        public static final String INSUFFICIENT_FUNDS = "ERR_INSUFFICIENT_FUNDS";
        public static final String INVALID_ACCOUNT = "ERR_INVALID_ACCOUNT";
        public static final String ACCOUNT_BLOCKED = "ERR_ACCOUNT_BLOCKED";
        public static final String DAILY_LIMIT_EXCEEDED = "ERR_DAILY_LIMIT_EXCEEDED";
        public static final String TRANSACTION_FAILED = "ERR_TRANSACTION_FAILED";
        
        // Authentication/Authorization Errors
        public static final String INVALID_PIN = "ERR_INVALID_PIN";
        public static final String EXPIRED_TOKEN = "ERR_EXPIRED_TOKEN";
        public static final String UNAUTHORIZED = "ERR_UNAUTHORIZED";
        
        // Network/System Errors
        public static final String NETWORK_ERROR = "ERR_NETWORK";
        public static final String TIMEOUT = "ERR_TIMEOUT";
        public static final String SYSTEM_ERROR = "ERR_SYSTEM";
        
        // Bank-specific Errors
        public static final String BANK_NOT_AVAILABLE = "ERR_BANK_UNAVAILABLE";
        public static final String INVALID_BANK_CODE = "ERR_INVALID_BANK";
        
        // Array of choices for error codes
        public static final String[][] CHOICES = {
            {INSUFFICIENT_FUNDS, "Insufficient Funds"},
            {INVALID_ACCOUNT, "Invalid Account"},
            {ACCOUNT_BLOCKED, "Account Blocked"},
            {DAILY_LIMIT_EXCEEDED, "Daily Limit Exceeded"},
            {TRANSACTION_FAILED, "Transaction Failed"},
            {INVALID_PIN, "Invalid PIN"},
            {EXPIRED_TOKEN, "Expired Token"},
            {UNAUTHORIZED, "Unauthorized"},
            {NETWORK_ERROR, "Network Error"},
            {TIMEOUT, "Request Timeout"},
            {SYSTEM_ERROR, "System Error"},
            {BANK_NOT_AVAILABLE, "Bank Not Available"},
            {INVALID_BANK_CODE, "Invalid Bank Code"}
        };
    }
    
    // Security Levels
    public static class SecurityLevel {
        public static final String LOW = "low";
        public static final String MEDIUM = "medium";
        public static final String HIGH = "high";
        public static final String CRITICAL = "critical";
        
        // Array of choices for security levels
        public static final String[][] CHOICES = {
            {LOW, "Low"},
            {MEDIUM, "Medium"},
            {HIGH, "High"},
            {CRITICAL, "Critical"}
        };
    }
    
    // Fraud Detection Flags
    public static class FraudFlag {
        public static final String NORMAL = "normal";
        public static final String SUSPICIOUS = "suspicious";
        public static final String HIGH_RISK = "high_risk";
        public static final String BLOCKED = "blocked";
        
        // Array of choices for fraud flags
        public static final String[][] CHOICES = {
            {NORMAL, "Normal"},
            {SUSPICIOUS, "Suspicious"},
            {HIGH_RISK, "High Risk"},
            {BLOCKED, "Blocked"}
        };
    }
    
    // Transfer Limits (in NGN)
    public static class TransferLimits {
        // Default limits
        public static final long DEFAULT_DAILY_LIMIT = 1000000L;  // 1M NGN
        public static final long DEFAULT_WEEKLY_LIMIT = 5000000L;  // 5M NGN
        public static final long DEFAULT_MONTHLY_LIMIT = 20000000L;  // 20M NGN
        
        // High-value transfer thresholds
        public static final long HIGH_VALUE_THRESHOLD = 500000L;  // 500K NGN - requires 2FA
        public static final long STAFF_APPROVAL_THRESHOLD = 1000000L;  // 1M NGN - requires staff approval
        
        // Velocity limits
        public static final int MAX_TRANSFERS_PER_HOUR = 10;
        public static final int MAX_TRANSFERS_PER_DAY = 50;
        public static final long MAX_AMOUNT_PER_HOUR = 500000L;  // 500K NGN
        public static final long MAX_AMOUNT_PER_DAY = 2000000L;  // 2M NGN
    }
    
    // Fee Structure (in NGN)
    public static class FeeStructure {
        // Internal transfer fees
        public static final double INTERNAL_FEE_PERCENT = 0.0;  // 0%
        public static final double INTERNAL_FEE_FIXED = 0.0;  // 0 NGN
        
        // External transfer fees
        public static final double EXTERNAL_FEE_PERCENT = 0.5;  // 0.5%
        public static final double EXTERNAL_FEE_FIXED = 50.0;  // 50 NGN
        
        // VAT rate
        public static final double VAT_RATE = 7.5;  // 7.5%
        
        // Levy rate
        public static final double LEVY_RATE = 0.5;  // 0.5%
    }
    
    // Retry Configuration
    public static class RetryConfig {
        public static final int MAX_RETRIES = 3;
        public static final int INITIAL_DELAY = 1;  // seconds
        public static final int MAX_DELAY = 60;  // seconds
        public static final int BACKOFF_MULTIPLIER = 2;
    }
    
    // Circuit Breaker Configuration
    public static class CircuitBreakerConfig {
        public static final int FAILURE_THRESHOLD = 5;
        public static final int RECOVERY_TIMEOUT = 60;  // seconds
        // Note: Java doesn't have a direct equivalent to Python's Exception class reference
    }
    
    // API Response Codes
    public static class ResponseCodes {
        public static final String SUCCESS = "SUCCESS";
        public static final String INSUFFICIENT_BALANCE = "INSUFFICIENT_BALANCE";
        public static final String INVALID_ACCOUNT = "INVALID_ACCOUNT";
        public static final String TRANSACTION_LIMIT_EXCEEDED = "TRANSACTION_LIMIT_EXCEEDED";
        public static final String FRAUD_DETECTED = "FRAUD_DETECTED";
        public static final String APPROVAL_REQUIRED = "APPROVAL_REQUIRED";
        public static final String TWO_FA_REQUIRED = "TWO_FA_REQUIRED";
        public static final String EXTERNAL_SERVICE_UNAVAILABLE = "EXTERNAL_SERVICE_UNAVAILABLE";
        public static final String IDEMPOTENCY_KEY_REQUIRED = "IDEMPOTENCY_KEY_REQUIRED";
        public static final String DUPLICATE_TRANSACTION = "DUPLICATE_TRANSACTION";
    }
    
    // Notification Types
    public static class NotificationType {
        public static final String TRANSFER_SUCCESS = "transfer_success";
        public static final String TRANSFER_FAILED = "transfer_failed";
        public static final String TRANSFER_PENDING = "transfer_pending";
        public static final String APPROVAL_REQUIRED = "approval_required";
        public static final String APPROVAL_GRANTED = "approval_granted";
        public static final String APPROVAL_DENIED = "approval_denied";
        public static final String TWO_FA_REQUIRED = "two_fa_required";
        public static final String FRAUD_ALERT = "fraud_alert";
        public static final String LIMIT_WARNING = "limit_warning";
        public static final String SECURITY_ALERT = "security_alert";
    }
    
    // Audit Event Types
    public static class AuditEventType {
        public static final String TRANSFER_CREATED = "transfer_created";
        public static final String TRANSFER_COMPLETED = "transfer_completed";
        public static final String TRANSFER_FAILED = "transfer_failed";
        public static final String TRANSFER_REVERSED = "transfer_reversed";
        public static final String APPROVAL_REQUESTED = "approval_requested";
        public static final String APPROVAL_GRANTED = "approval_granted";
        public static final String APPROVAL_DENIED = "approval_denied";
        public static final String TWO_FA_TRIGGERED = "two_fa_triggered";
        public static final String FRAUD_DETECTED = "fraud_detected";
        public static final String LIMIT_EXCEEDED = "limit_exceeded";
        public static final String SECURITY_VIOLATION = "security_violation";
    }
    
    // Device Fingerprinting
    public static class DeviceFingerprint {
        public static final String BROWSER = "browser";
        public static final String MOBILE_APP = "mobile_app";
        public static final String API = "api";
        public static final String WEBHOOK = "webhook";
        
        // Array of choices for device fingerprinting
        public static final String[][] CHOICES = {
            {BROWSER, "Browser"},
            {MOBILE_APP, "Mobile App"},
            {API, "API"},
            {WEBHOOK, "Webhook"}
        };
    }
    
    // IP Whitelist Status
    public static class IPWhitelistStatus {
        public static final String ALLOWED = "allowed";
        public static final String BLOCKED = "blocked";
        public static final String PENDING = "pending";
        
        // Array of choices for IP whitelist status
        public static final String[][] CHOICES = {
            {ALLOWED, "Allowed"},
            {BLOCKED, "Blocked"},
            {PENDING, "Pending"}
        };
    }
    
    // Scheduled Transfer Frequencies
    public static class ScheduledFrequency {
        public static final String ONCE = "once";
        public static final String DAILY = "daily";
        public static final String WEEKLY = "weekly";
        public static final String MONTHLY = "monthly";
        public static final String YEARLY = "yearly";
        
        // Array of choices for scheduled frequencies
        public static final String[][] CHOICES = {
            {ONCE, "Once"},
            {DAILY, "Daily"},
            {WEEKLY, "Weekly"},
            {MONTHLY, "Monthly"},
            {YEARLY, "Yearly"}
        };
    }
    
    // Bulk Transfer Status
    public static class BulkTransferStatus {
        public static final String PENDING = "pending";
        public static final String PROCESSING = "processing";
        public static final String PARTIAL_COMPLETED = "partial_completed";
        public static final String COMPLETED = "completed";
        public static final String FAILED = "failed";
        public static final String CANCELLED = "cancelled";
        
        // Array of choices for bulk transfer status
        public static final String[][] CHOICES = {
            {PENDING, "Pending"},
            {PROCESSING, "Processing"},
            {PARTIAL_COMPLETED, "Partially Completed"},
            {COMPLETED, "Completed"},
            {FAILED, "Failed"},
            {CANCELLED, "Cancelled"}
        };
    }
    
    // Escrow Status
    public static class EscrowStatus {
        public static final String PENDING = "pending";
        public static final String FUNDED = "funded";
        public static final String RELEASED = "released";
        public static final String REFUNDED = "refunded";
        public static final String EXPIRED = "expired";
        
        // Array of choices for escrow status
        public static final String[][] CHOICES = {
            {PENDING, "Pending"},
            {FUNDED, "Funded"},
            {RELEASED, "Released"},
            {REFUNDED, "Refunded"},
            {EXPIRED, "Expired"}
        };
    }
}