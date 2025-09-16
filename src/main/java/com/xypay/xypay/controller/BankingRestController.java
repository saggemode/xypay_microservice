package com.xypay.xypay.controller;

import com.xypay.xypay.domain.*;
import com.xypay.xypay.dto.*;
import com.xypay.xypay.service.*;
import com.xypay.xypay.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Comprehensive REST Controller for Banking Operations
 * Converted from Django REST Framework ViewSets
 */
@RestController
@RequestMapping("/api/v1/banking")
@CrossOrigin(origins = "*")
public class BankingRestController {
    
    private static final Logger logger = LoggerFactory.getLogger(BankingRestController.class);
    
    // Service dependencies
    @Autowired
    private BankAccountService bankAccountService;
    
    @Autowired
    private TransferValidationService transferValidationService;
    
    @Autowired
    private FraudDetectionService fraudDetectionService;
    
    @Autowired
    private TwoFactorAuthService twoFactorAuthService;
    
    @Autowired
    private DeviceFingerprintService deviceFingerprintService;
    
    @Autowired
    private IdempotencyService idempotencyService;
    
    @Autowired
    private BiometricService biometricService;
    
    @Autowired
    private TransactionPinService transactionPinService;
    
    // Repository dependencies
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private BankTransferRepository bankTransferRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // ==================== WALLET OPERATIONS ====================
    
    /**
     * Get current user's wallet
     */
    @GetMapping("/wallet/my-wallet")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Wallet>> getMyWallet(@RequestHeader("X-User-ID") UUID userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found"));
            }
            
            List<Wallet> wallets = walletRepository.findByUser(user);
            if (wallets.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Wallet not found. Complete KYC verification first."));
            }
            
            Wallet wallet = wallets.get(0);
            
            return ResponseEntity.ok(ApiResponse.success(wallet, "Wallet retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error getting wallet for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve wallet"));
        }
    }
    
    /**
     * Get all wallets (admin only)
     */
    @GetMapping("/wallet")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Wallet>> getAllWallets(
            @PageableDefault(sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        try {
            Page<Wallet> wallets = walletRepository.findAll(pageable);
            return ResponseEntity.ok(wallets);
        } catch (Exception e) {
            logger.error("Error getting all wallets: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // ==================== TRANSACTION OPERATIONS ====================
    
    /**
     * Get current user's transactions
     */
    @GetMapping("/transactions/my-transactions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Transaction>>> getMyTransactions(
            @RequestHeader("X-User-ID") UUID userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String status,
            @PageableDefault(sort = "timestamp", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found"));
            }
            
            List<Wallet> wallets = walletRepository.findByUser(user);
            if (wallets.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Wallet not found. Complete KYC verification first."));
            }
            
            Wallet wallet = wallets.get(0);
            List<Transaction> transactions = transactionRepository.findAll().stream()
                .filter(t -> t.getWallet().equals(wallet))
                .collect(Collectors.toList());
            
            // Apply filters
            if (type != null) {
                transactions = transactions.stream()
                    .filter(t -> type.equals(t.getType()))
                    .collect(Collectors.toList());
            }
            if (channel != null) {
                transactions = transactions.stream()
                    .filter(t -> channel.equals(t.getChannel()))
                    .collect(Collectors.toList());
            }
            if (status != null) {
                transactions = transactions.stream()
                    .filter(t -> status.equals(t.getStatus()))
                    .collect(Collectors.toList());
            }
            
            return ResponseEntity.ok(ApiResponse.success(transactions, "Transactions retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error getting transactions for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve transactions"));
        }
    }
    
    // ==================== BANK TRANSFER OPERATIONS ====================
    
    /**
     * Validate account number and return bank info
     */
    @PostMapping("/bank-transfers/validate-account")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateAccount(
            @RequestBody Map<String, String> request) {
        try {
            String accountNumber = request.get("account_number");
            
            if (accountNumber == null || accountNumber.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Account number is required"));
            }
            
            // Validate account number format (10 digits for Nigerian banks)
            if (!accountNumber.matches("\\d{10}")) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid account number format. Must be 10 digits."));
            }
            
            // First check if it's an internal account
            Optional<Wallet> internalWallet = walletRepository.findByAccountNumber(accountNumber);
            if (internalWallet.isPresent()) {
                Wallet wallet = internalWallet.get();
                Map<String, Object> response = new HashMap<>();
                response.put("account_number", accountNumber);
                response.put("account_name", wallet.getUser().getFirstName() + " " + wallet.getUser().getLastName());
                response.put("bank_name", "XYPay Bank");
                response.put("bank_code", "880");
                response.put("is_internal", true);
                response.put("status", "valid");
                
                return ResponseEntity.ok(ApiResponse.success(response, "Account validated successfully"));
            }
            
            // Check external banks using BankAccountService
            List<Map<String, Object>> matchingBanks = bankAccountService.searchBanksByAccountNumber(accountNumber);
            
            if (matchingBanks.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("account_number", accountNumber);
                response.put("error", "Account not found in any bank");
                response.put("status", "invalid");
                
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Account not found", response));
            }
            
            // Return first matching bank
            Map<String, Object> bankInfo = matchingBanks.get(0);
            bankInfo.put("is_internal", false);
            bankInfo.put("status", "valid");
            
            return ResponseEntity.ok(ApiResponse.success(bankInfo, "Account validated successfully"));
            
        } catch (Exception e) {
            logger.error("Error validating account: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to validate account"));
        }
    }
    
    /**
     * Get list of all available banks
     */
    @GetMapping("/bank-transfers/banks")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBanks() {
        try {
            List<Map<String, Object>> banks = bankAccountService.getAllBanks();
            
            Map<String, Object> response = new HashMap<>();
            response.put("banks", banks);
            response.put("count", banks.size());
            
            return ResponseEntity.ok(ApiResponse.success(response, "Banks retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error getting banks: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve banks"));
        }
    }
    
    /**
     * Search banks by account number
     */
    @GetMapping("/bank-transfers/search-banks-by-account")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> searchBanksByAccount(
            @RequestParam String account_number) {
        try {
            if (account_number == null || account_number.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Account number is required"));
            }
            
            // Validate account number format
            if (!account_number.matches("\\d{10,}")) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid account number format. Must be at least 10 digits."));
            }
            
            List<Map<String, Object>> matchingBanks = bankAccountService.searchBanksByAccountNumber(account_number);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", String.format("Found %d banks for account number %s", matchingBanks.size(), account_number));
            response.put("account_number", account_number);
            response.put("banks", matchingBanks);
            
            return ResponseEntity.ok(ApiResponse.success(response, "Banks found successfully"));
            
        } catch (Exception e) {
            logger.error("Error searching banks by account: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to search banks"));
        }
    }
    
    /**
     * Validate transfer request
     */
    @PostMapping("/bank-transfers/validate-transfer")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateTransfer(
            @RequestHeader("X-User-ID") UUID userId,
            @RequestBody Map<String, Object> request) {
        try {
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String recipientAccount = request.get("account_number").toString();
            String recipientBankCode = request.get("bank_code").toString();
            
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found"));
            }
            
            Map<String, Object> validationResult = transferValidationService.validateTransferRequest(
                user, amount, recipientAccount, recipientBankCode);
            
            if ((Boolean) validationResult.get("is_valid")) {
                Map<String, Object> response = new HashMap<>();
                response.put("is_valid", true);
                response.put("message", "Transfer validation successful");
                response.put("recipient_name", validationResult.get("recipient_name"));
                response.put("bank_name", validationResult.get("bank_name"));
                response.put("verification_method", validationResult.get("verification_method"));
                
                return ResponseEntity.ok(ApiResponse.success(response, "Transfer validation successful"));
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("is_valid", false);
                response.put("error", validationResult.get("error"));
                response.put("details", validationResult);
                
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Transfer validation failed", response));
            }
            
        } catch (Exception e) {
            logger.error("Error validating transfer: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Validation failed. Please try again."));
        }
    }
    
    /**
     * Create bank transfer with enhanced security
     */
    @PostMapping("/bank-transfers")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<ApiResponse<BankTransfer>> createBankTransfer(
            @RequestHeader("X-User-ID") UUID userId,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found"));
            }
            
            // Check idempotency
            if (idempotencyKey != null && idempotencyService.isDuplicateRequest(idempotencyKey, userId)) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Duplicate request detected", null));
            }
            
            // Extract transfer details
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String recipientAccount = request.get("account_number").toString();
            String recipientBankCode = request.get("bank_code").toString();
            String description = request.getOrDefault("description", "").toString();
            String transactionPin = request.getOrDefault("transaction_pin", "").toString();
            
            // Generate device fingerprint
            String deviceFingerprint = deviceFingerprintService.generateDeviceFingerprint(httpRequest);
            String ipAddress = getClientIp(httpRequest);
            
            // Validate transaction PIN
            Map<String, Object> pinValidation = transactionPinService.verifyTransactionPin(user, transactionPin);
            if (!(Boolean) pinValidation.get("success")) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid transaction PIN", pinValidation));
            }
            
            // Fraud detection
            int fraudScore = fraudDetectionService.calculateFraudScore(
                user, amount, recipientAccount, recipientBankCode, deviceFingerprint, ipAddress);
            
            boolean requires2FA = fraudDetectionService.shouldRequire2fa(user, amount, fraudScore);
            boolean requiresApproval = fraudDetectionService.shouldRequireApproval(user, amount, fraudScore);
            
            // Create bank transfer
            BankTransfer transfer = new BankTransfer();
            transfer.setUser(user);
            transfer.setAccountNumber(recipientAccount);
            transfer.setBankCode(recipientBankCode);
            transfer.setAmount(amount);
            transfer.setDescription(description);
            transfer.setReference("TRF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            transfer.setStatus(requiresApproval ? "pending_approval" : "pending");
            transfer.setDeviceFingerprint(deviceFingerprint);
            transfer.setIpAddress(ipAddress);
            transfer.setFraudScore(BigDecimal.valueOf(fraudScore));
            transfer.setRequires2fa(requires2FA);
            transfer.setIsSuspicious(fraudScore > 70);
            
            // Generate 2FA code if required
            if (requires2FA) {
                String twoFACode = twoFactorAuthService.generate2faCode();
                transfer.setTwoFaCode(twoFACode);
                transfer.setTwoFaExpiresAt(LocalDateTime.now().plusMinutes(10));
                
                // Send 2FA code
                twoFactorAuthService.send2faCode(user, twoFACode, amount, recipientAccount);
            }
            
            // Save transfer
            transfer = bankTransferRepository.save(transfer);
            
            // Log audit event
            logger.info("Bank transfer created: {} for user: {} (fraud score: {}, 2FA required: {})", 
                transfer.getId(), userId, fraudScore, requires2FA);
            
            return ResponseEntity.ok(ApiResponse.success(transfer, "Bank transfer created successfully"));
            
        } catch (Exception e) {
            logger.error("Error creating bank transfer: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to create bank transfer"));
        }
    }
    
    /**
     * Verify 2FA code for a transfer
     */
    @PostMapping("/bank-transfers/{transferId}/verify-2fa")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verify2FA(
            @PathVariable String transferId,
            @RequestHeader("X-User-ID") UUID userId,
            @RequestBody Map<String, String> request) {
        try {
            Optional<BankTransfer> transferOpt = bankTransferRepository.findById(UUID.fromString(transferId));
            if (transferOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Transfer not found"));
            }
            
            BankTransfer transfer = transferOpt.get();
            String twoFACode = request.get("two_fa_code");
            
            if (twoFACode == null || twoFACode.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("2FA code is required"));
            }
            
            boolean isValid = twoFactorAuthService.verify2faCode(transfer, twoFACode);
            
            if (isValid) {
                transfer.setTwoFaVerified(true);
                transfer.setStatus("processing");
                bankTransferRepository.save(transfer);
                
                Map<String, Object> response = new HashMap<>();
                response.put("message", "2FA verification successful. Transfer is being processed.");
                response.put("transfer_id", transferId);
                response.put("status", transfer.getStatus());
                
                return ResponseEntity.ok(ApiResponse.success(response, "2FA verification successful"));
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid or expired 2FA code"));
            }
            
        } catch (Exception e) {
            logger.error("Error verifying 2FA: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("2FA verification failed"));
        }
    }
    
    /**
     * Get fraud analysis for user's transfers
     */
    @GetMapping("/bank-transfers/fraud-analysis")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFraudAnalysis(
            @RequestHeader("X-User-ID") UUID userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found"));
            }
            
            List<BankTransfer> recentTransfers = bankTransferRepository.findByUser(user)
                .stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(10)
                .collect(Collectors.toList());
            
            List<Map<String, Object>> fraudAnalysis = recentTransfers.stream()
                .map(transfer -> {
                    Map<String, Object> analysis = new HashMap<>();
                    analysis.put("transfer_id", transfer.getId().toString());
                    analysis.put("amount", transfer.getAmount().toString());
                    analysis.put("fraud_score", transfer.getFraudScore());
                    analysis.put("is_suspicious", transfer.getIsSuspicious());
                    analysis.put("created_at", transfer.getCreatedAt().toString());
                    analysis.put("status", transfer.getStatus());
                    return analysis;
                })
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("fraud_analysis", fraudAnalysis);
            response.put("total_transfers", fraudAnalysis.size());
            
            return ResponseEntity.ok(ApiResponse.success(response, "Fraud analysis retrieved successfully"));
            
        } catch (Exception e) {
            logger.error("Error getting fraud analysis: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to get fraud analysis"));
        }
    }
    
    // ==================== BIOMETRIC AUTHENTICATION ====================
    
    /**
     * Register biometric
     */
    @PostMapping("/biometric/register")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> registerBiometric(
            @RequestHeader("X-User-ID") UUID userId,
            @RequestBody Map<String, String> request) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found"));
            }
            
            String biometricType = request.get("type");
            String biometricData = request.get("data");
            
            if (biometricType == null || biometricData == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Biometric type and data are required"));
            }
            
            Map<String, Object> result = biometricService.registerBiometric(user, biometricType, biometricData);
            
            if ((Boolean) result.get("success")) {
                return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(result, "Biometric registered successfully"));
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(result.get("error").toString()));
            }
            
        } catch (Exception e) {
            logger.error("Error registering biometric: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Biometric registration failed"));
        }
    }
    
    /**
     * Verify biometric
     */
    @PostMapping("/biometric/verify")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyBiometric(
            @RequestHeader("X-User-ID") UUID userId,
            @RequestBody Map<String, String> request) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found"));
            }
            
            String biometricType = request.get("type");
            String biometricData = request.get("data");
            
            if (biometricType == null || biometricData == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Biometric type and data are required"));
            }
            
            Map<String, Object> result = biometricService.verifyBiometric(user, biometricType, biometricData);
            
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(ApiResponse.success(result, "Biometric verification successful"));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(result.get("error").toString()));
            }
            
        } catch (Exception e) {
            logger.error("Error verifying biometric: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Biometric verification failed"));
        }
    }
    
    /**
     * Get registered biometrics
     */
    @GetMapping("/biometric/registered")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRegisteredBiometrics(
            @RequestHeader("X-User-ID") UUID userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found"));
            }
            
            Map<String, Object> result = biometricService.getRegisteredBiometrics(user);
            return ResponseEntity.ok(ApiResponse.success(result, "Registered biometrics retrieved successfully"));
            
        } catch (Exception e) {
            logger.error("Error getting registered biometrics: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to get registered biometrics"));
        }
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Get client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
