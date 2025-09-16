package com.xypay.xypay.service;

import com.xypay.xypay.domain.*;
import com.xypay.xypay.repository.*;
import com.xypay.xypay.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;
import java.util.Optional;

@Service
public class UserRegistrationService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Autowired
    private KYCProfileRepository kycProfileRepository;
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private AuditTrailService auditTrailService;
    
    @Autowired
    private EmailOtpService emailOtpService;
    
    @Autowired
    private WalletService walletService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    // Note: PhoneOtpService not found, using EmailOtpService for now

    // Phone number validation pattern (Nigerian format)
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?234[789][01]\\d{8}$|^0[789][01]\\d{8}$");
    
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    
    @Transactional
    public Map<String, Object> registerUser(String username, String email, String password, String phone) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate input
            if (username == null || username.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Username is required");
                return response;
            }
            
            if (email == null || email.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Email is required");
                return response;
            }
            
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                response.put("success", false);
                response.put("message", "Invalid email format");
                return response;
            }
            
            if (password == null || password.length() < 6) {
                response.put("success", false);
                response.put("message", "Password must be at least 6 characters");
                return response;
            }
            
            if (phone == null || phone.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Phone number is required");
                return response;
            }
            
            if (!PHONE_PATTERN.matcher(phone).matches()) {
                response.put("success", false);
                response.put("message", "Invalid Nigerian phone number format");
                return response;
            }
            
            // Check if user already exists
            if (userRepository.findByUsername(username).isPresent()) {
                response.put("success", false);
                response.put("message", "Username already exists");
                return response;
            }
            
            if (userRepository.findByEmail(email).isPresent()) {
                response.put("success", false);
                response.put("message", "Email already exists");
                return response;
            }
            
            if (userProfileRepository.existsByPhone(phone)) {
                response.put("success", false);
                response.put("message", "Phone number already exists");
                return response;
            }
            
            // Create user
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setEnabled(false); // User is not enabled until phone is verified
            user = userRepository.save(user);
            
            // Create user profile
            UserProfile profile = new UserProfile(user);
            profile.setPhone(phone);
            
            // Generate phone verification token
            profile.generatePhoneVerificationToken();
            profile = userProfileRepository.save(profile);
            
            // Create wallet for the user with phone number as account number
            Wallet wallet = null;
            try {
                wallet = walletService.createWallet(user, "NGN", phone);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create wallet: " + e.getMessage(), e);
            }
            
            // Generate OTP for phone verification
            String otp = null;
            try {
                otp = emailOtpService.setOtp(profile);
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate OTP: " + e.getMessage(), e);
            }
            
            // Send OTP via SMS (non-blocking)
            boolean otpSent = false;
            try {
                otpSent = notificationService.sendOtpSms(phone, otp);
            } catch (Exception e) {
                // Log but don't fail registration
                System.err.println("Failed to send SMS: " + e.getMessage());
            }
            
            // Also send verification email (non-blocking)
            boolean emailSent = false;
            try {
                emailSent = emailOtpService.sendOtpEmail(profile, otp);
            } catch (Exception e) {
                // Log but don't fail registration
                System.err.println("Failed to send email: " + e.getMessage());
            }
            
            // Log registration event (non-blocking)
            try {
                auditTrailService.logEvent("USER_REGISTRATION", 
                    String.format("User registered: %s, Email: %s, Phone: %s", username, email, phone));
            } catch (Exception e) {
                // Log but don't fail registration
                System.err.println("Failed to log audit event: " + e.getMessage());
            }
            
            // Prepare response
            // Generate JWT tokens for the registered user
            String accessToken = jwtUtil.generateToken(user.getUsername());
            String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
            
            response.put("success", true);
            response.put("message", "Registration successful. Please verify your phone number with the OTP sent.");
            response.put("user_id", user.getId());
            response.put("account_number", wallet.getAccountNumber());
            response.put("alternative_account_number", wallet.getAlternativeAccountNumber());
            response.put("phone", phone);
            response.put("verification_token", profile.getPhoneVerificationToken());
            response.put("access_token", accessToken);
            response.put("refresh_token", refreshToken);
            response.put("token_type", "Bearer");
            response.put("otp_sent", otpSent);
            response.put("email_sent", emailSent);
            response.put("otp", otp); // For testing purposes
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Registration failed: " + e.getMessage());
            response.put("error_details", e.getClass().getSimpleName());
            
            // Log error (non-blocking)
            try {
                auditTrailService.logEvent("USER_REGISTRATION_ERROR", 
                    String.format("Registration failed for username: %s, error: %s", username, e.getMessage()));
            } catch (Exception logError) {
                System.err.println("Failed to log error: " + logError.getMessage());
            }
        }
        
        return response;
    }
    
    @Transactional
    public Map<String, Object> verifyPhoneNumber(String phone, String otp) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<UserProfile> profileOpt = userProfileRepository.findByPhone(phone);
            
            if (profileOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Phone number not found");
                return response;
            }
            
            UserProfile profile = profileOpt.get();
            
            if (profile.getIsVerified()) {
                response.put("success", false);
                response.put("message", "Phone number already verified");
                return response;
            }
            
            if (!profile.isOtpValid(otp)) {
                response.put("success", false);
                response.put("message", "Invalid or expired OTP");
                return response;
            }
            
            // Verify user and enable account
            profile.setIsVerified(true);
            profile.setOtpCode(null);
            profile.setOtpExpiry(null);
            profile.getUser().setEnabled(true);
            
            userProfileRepository.save(profile);
            userRepository.save(profile.getUser());
            
            // Create default KYC profile (Tier 1)
            KYCProfile kycProfile = new KYCProfile();
            kycProfile.setUser(profile.getUser());
            kycProfile.setDateOfBirth(LocalDate.now().minusYears(18)); // Default to 18 years ago
            kycProfile.setAddress("Not provided"); // Will be updated during KYC
            kycProfileRepository.save(kycProfile);
            
            // Get user's wallet to retrieve account number
            List<Wallet> wallets = walletRepository.findByUser(profile.getUser());
            String accountNumber = wallets.isEmpty() ? "N/A" : wallets.get(0).getAccountNumber();
            
            // Log verification event
            auditTrailService.logEvent("PHONE_VERIFICATION", 
                String.format("Phone verified for user: %s, Account: %s", 
                    profile.getUser().getUsername(), accountNumber));
            
            response.put("success", true);
            response.put("message", "Phone number verified successfully");
            response.put("account_number", accountNumber);
            response.put("kyc_level", "TIER_1");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Verification failed: " + e.getMessage());
            
            auditTrailService.logEvent("PHONE_VERIFICATION_ERROR", 
                String.format("Phone verification failed for: %s, error: %s", phone, e.getMessage()));
        }
        
        return response;
    }
    
    @Transactional
    public Map<String, Object> verifyPhoneWithToken(String verificationToken, String otp) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<UserProfile> profileOpt = userProfileRepository.findByPhoneVerificationToken(verificationToken);
            
            if (profileOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Invalid verification token");
                return response;
            }
            
            UserProfile profile = profileOpt.get();
            
            // Check if token is still valid
            if (!profile.isPhoneVerificationTokenValid(verificationToken)) {
                response.put("success", false);
                response.put("message", "Verification token has expired");
                return response;
            }
            
            if (profile.getIsVerified()) {
                response.put("success", false);
                response.put("message", "Phone number already verified");
                return response;
            }
            
            if (!profile.isOtpValid(otp)) {
                response.put("success", false);
                response.put("message", "Invalid or expired OTP");
                return response;
            }
            
            // Verify user and enable account
            profile.setIsVerified(true);
            profile.setOtpCode(null);
            profile.setOtpExpiry(null);
            profile.clearPhoneVerificationToken();
            profile.getUser().setEnabled(true);
            
            userProfileRepository.save(profile);
            userRepository.save(profile.getUser());
            
            // Create default KYC profile (Tier 1)
            KYCProfile kycProfile = new KYCProfile();
            kycProfile.setUser(profile.getUser());
            kycProfile.setDateOfBirth(LocalDate.now().minusYears(18));
            kycProfile.setAddress("Not provided");
            kycProfileRepository.save(kycProfile);
            
            // Get account number from wallet
            List<Wallet> wallets = walletRepository.findByUser(profile.getUser());
            String accountNumber = wallets.isEmpty() ? "N/A" : wallets.get(0).getAccountNumber();
            
            // Log verification event
            auditTrailService.logEvent("PHONE_VERIFICATION_TOKEN", 
                String.format("Phone verified with token for user: %s, Account: %s", 
                    profile.getUser().getUsername(), accountNumber));
            
            response.put("success", true);
            response.put("message", "Phone number verified successfully");
            response.put("account_number", accountNumber);
            response.put("kyc_level", "TIER_1");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Verification failed: " + e.getMessage());
            
            auditTrailService.logEvent("PHONE_VERIFICATION_TOKEN_ERROR", 
                String.format("Token-based phone verification failed for token: %s, error: %s", verificationToken, e.getMessage()));
        }
        
        return response;
    }
    
    @Transactional
    public Map<String, Object> verifyOtpWithJwt(String username, String otp) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<UserProfile> profileOpt = userProfileRepository.findByUserUsername(username);
            
            if (profileOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found");
                return response;
            }
            
            UserProfile profile = profileOpt.get();
            
            if (profile.getIsVerified()) {
                response.put("success", false);
                response.put("message", "Phone number already verified");
                return response;
            }
            
            if (!profile.isOtpValid(otp)) {
                response.put("success", false);
                response.put("message", "Invalid or expired OTP");
                return response;
            }
            
            // Verify user and enable account
            profile.setIsVerified(true);
            profile.setOtpCode(null);
            profile.setOtpExpiry(null);
            profile.clearPhoneVerificationToken();
            profile.getUser().setEnabled(true);
            
            userProfileRepository.save(profile);
            userRepository.save(profile.getUser());
            
            // Create default KYC profile (Tier 1)
            KYCProfile kycProfile = new KYCProfile();
            kycProfile.setUser(profile.getUser());
            kycProfile.setDateOfBirth(LocalDate.now().minusYears(18));
            kycProfile.setAddress("Not provided");
            kycProfileRepository.save(kycProfile);
            
            // Get account number from wallet
            List<Wallet> wallets = walletRepository.findByUser(profile.getUser());
            String accountNumber = wallets.isEmpty() ? "N/A" : wallets.get(0).getAccountNumber();
            
            // Log verification event
            auditTrailService.logEvent("PHONE_VERIFICATION_JWT", 
                String.format("Phone verified with JWT for user: %s, Account: %s", 
                    profile.getUser().getUsername(), accountNumber));
            
            response.put("success", true);
            response.put("message", "Phone number verified successfully");
            response.put("account_number", accountNumber);
            response.put("kyc_level", "TIER_1");
            response.put("user_enabled", true);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Verification failed: " + e.getMessage());
            
            auditTrailService.logEvent("PHONE_VERIFICATION_JWT_ERROR", 
                String.format("JWT-based phone verification failed for user: %s, error: %s", username, e.getMessage()));
        }
        
        return response;
    }
    
    @Transactional
    public Map<String, Object> verifyOtpWithJwtToken(String token, String otp) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Extract username from JWT token
            if (!jwtUtil.validateToken(token)) {
                response.put("success", false);
                response.put("message", "Invalid or expired JWT token");
                return response;
            }
            
            String username = jwtUtil.extractUsername(token);
            
            // Use the existing method with extracted username
            return verifyOtpWithJwt(username, otp);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Token verification failed: " + e.getMessage());
            return response;
        }
    }
    
    @Transactional
    public Map<String, Object> resendOtp(String phone) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<UserProfile> profileOpt = userProfileRepository.findByPhone(phone);
            
            if (profileOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Phone number not found");
                return response;
            }
            
            UserProfile profile = profileOpt.get();
            
            if (profile.getIsVerified()) {
                response.put("success", false);
                response.put("message", "Phone number already verified");
                return response;
            }
            
            // Generate new OTP
            String newOtp = emailOtpService.setOtp(profile);
            
            // Send OTP via SMS
            boolean otpSent = notificationService.sendOtpSms(phone, newOtp);
            
            // Also send verification email
            boolean emailSent = emailOtpService.sendOtpEmail(profile, newOtp);
            
            auditTrailService.logEvent("OTP_RESEND", 
                String.format("OTP resent for phone: %s", phone));
            
            response.put("success", true);
            response.put("message", "OTP sent successfully");
            response.put("otp_sent", otpSent);
            response.put("email_sent", emailSent);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to resend OTP: " + e.getMessage());
        }
        
        return response;
    }
    
    public Map<String, Object> getUserByAccountNumber(String accountNumber) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Find wallet by account number (check both primary and alternative)
            Optional<Wallet> walletOpt = walletRepository.findByAccountNumberOrAlternativeAccountNumber(accountNumber, accountNumber);
            
            if (walletOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Account not found");
                return response;
            }
            
            Wallet wallet = walletOpt.get();
            User user = wallet.getUser();
            UserProfile profile = userProfileRepository.findByUser(user).orElse(null);
            
            if (profile == null) {
                response.put("success", false);
                response.put("message", "User profile not found");
                return response;
            }
            
            Map<String, Object> userData = new HashMap<>();
            userData.put("username", user.getUsername());
            userData.put("email", user.getEmail());
            userData.put("phone", profile.getPhone());
            userData.put("account_number", wallet.getAccountNumber());
            userData.put("is_verified", profile.getIsVerified());
            userData.put("enabled", user.isEnabled());
            userData.put("created_at", profile.getCreatedAt());
            
            // Get KYC level if exists
            Optional<KYCProfile> kycOpt = kycProfileRepository.findByUserId(user.getId().getMostSignificantBits()); // Convert UUID to Long
            if (kycOpt.isPresent()) {
                KYCProfile kyc = kycOpt.get();
                userData.put("kyc_level", kyc.getKycLevel());
                userData.put("kyc_approved", kyc.getIsApproved());
                userData.put("daily_limit", kyc.getDailyTransactionLimit());
                userData.put("max_balance", kyc.getMaxBalanceLimit());
            }
            
            response.put("success", true);
            response.put("user", userData);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve user: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * Get user information by phone number (which is now the account number)
     */
    public Map<String, Object> getUserByPhoneNumber(String phoneNumber) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Extract phone digits to match account number format
            String phoneDigits = extractPhoneDigitsForLookup(phoneNumber);
            
            // Find wallet by account number (phone-based)
            Optional<Wallet> walletOpt = walletRepository.findByAccountNumber(phoneDigits);
            
            if (walletOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Phone number not registered");
                return response;
            }
            
            Wallet wallet = walletOpt.get();
            User user = wallet.getUser();
            UserProfile profile = userProfileRepository.findByUser(user).orElse(null);
            
            if (profile == null) {
                response.put("success", false);
                response.put("message", "User profile not found");
                return response;
            }
            
            Map<String, Object> userData = new HashMap<>();
            userData.put("username", user.getUsername());
            userData.put("email", user.getEmail());
            userData.put("phone", profile.getPhone());
            userData.put("account_number", wallet.getAccountNumber());
            userData.put("is_verified", profile.getIsVerified());
            userData.put("enabled", user.isEnabled());
            userData.put("created_at", profile.getCreatedAt());
            
            // Get KYC level if exists
            Optional<KYCProfile> kycOpt = kycProfileRepository.findByUserId(user.getId().getMostSignificantBits()); // Convert UUID to Long
            if (kycOpt.isPresent()) {
                KYCProfile kyc = kycOpt.get();
                userData.put("kyc_level", kyc.getKycLevel());
                userData.put("kyc_approved", kyc.getIsApproved());
                userData.put("daily_limit", kyc.getDailyTransactionLimit());
                userData.put("max_balance", kyc.getMaxBalanceLimit());
            }
            
            response.put("success", true);
            response.put("user", userData);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve user: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * Extract phone number digits for lookup (same logic as wallet service)
     */
    private String extractPhoneDigitsForLookup(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }
        
        // Remove all non-digit characters
        String digitsOnly = phoneNumber.replaceAll("\\D", "");
        
        // Handle Nigerian phone numbers
        if (digitsOnly.startsWith("234")) {
            // Remove country code +234 and use the remaining 10 digits
            digitsOnly = digitsOnly.substring(3);
        } else if (digitsOnly.startsWith("0")) {
            // Remove leading 0 for local format (e.g., 07038655955 -> 7038655955)
            digitsOnly = digitsOnly.substring(1);
        }
        
        // Ensure we have exactly 10 digits for account number
        if (digitsOnly.length() != 10) {
            throw new IllegalArgumentException("Invalid phone number format. Expected 10 digits after processing.");
        }
        
        return digitsOnly;
    }
    
    /**
     * Verify email with token
     * @param userProfileId User profile ID
     * @param token Email verification token
     * @return Verification result
     */
    @Transactional
    public Map<String, Object> verifyEmail(String userProfileId, String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long profileId = Long.parseLong(userProfileId);
            Optional<UserProfile> profileOpt = userProfileRepository.findById(profileId);
            
            if (profileOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Invalid verification link");
                return response;
            }
            
            UserProfile profile = profileOpt.get();
            
            if (!profile.isEmailVerificationTokenValid(token)) {
                response.put("success", false);
                response.put("message", "Invalid or expired verification token");
                return response;
            }
            
            profile.verifyEmail();
            userProfileRepository.save(profile);
            
            response.put("success", true);
            response.put("message", "Email verified successfully");
            
            auditTrailService.logEvent("EMAIL_VERIFICATION", 
                String.format("Email verified for user: %s", profile.getUser().getUsername()));
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Email verification failed: " + e.getMessage());
        }
        
        return response;
    }

    // --- Methods for UserRegistrationMonitorController ---
    public long getTotalRegistrationsCount() {
        return userRepository.count();
    }

    public long getVerifiedUsersCount() {
        return userRepository.countVerifiedUsers();
    }

    public long getPendingVerificationsCount() {
        return userRepository.countPendingVerifications();
    }

    public long getTodayRegistrationsCount() {
        LocalDate today = LocalDate.now();
        return userRepository.countByCreatedAtBetween(today.atStartOfDay(), today.plusDays(1).atStartOfDay());
    }

    public java.util.Map<String, Long> getDailyRegistrationsLast30Days() {
        java.util.Map<String, Long> result = new java.util.LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            long count = userRepository.countByCreatedAtBetween(date.atStartOfDay(), date.plusDays(1).atStartOfDay());
            result.put(date.toString(), count);
        }
        return result;
    }

    public java.util.Map<String, Long> getVerificationStatusDistribution() {
        java.util.Map<String, Long> map = new java.util.HashMap<>();
        map.put("verified", userRepository.countVerifiedUsers());
        map.put("pending", userRepository.countPendingVerifications());
        return map;
    }

    public java.util.List<User> getRecentRegistrations(int limit) {
        return userRepository.findTopByOrderByCreatedAtDesc(limit);
    }

    public java.util.List<Object> getPendingSupportCases() {
        // Stub: return empty list or implement as needed
        return java.util.Collections.emptyList();
    }

    public long getPendingSupportCount() {
        // Stub: return 0 or implement as needed
        return 0L;
    }

    public long getResolvedTodayCount() {
        // Stub: return 0 or implement as needed
        return 0L;
    }
}