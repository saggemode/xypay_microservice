package com.xypay.xypay.service;

import com.xypay.xypay.domain.User;
import com.xypay.xypay.domain.UserProfile;
import com.xypay.xypay.domain.KYCProfile;
import com.xypay.xypay.repository.UserRepository;
import com.xypay.xypay.repository.UserProfileRepository;
import com.xypay.xypay.repository.KYCProfileRepository;
import com.xypay.xypay.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for user verification and account management.
 * Equivalent to Django's user verification views and utilities.
 */
@Service
@Transactional
public class UserVerificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserVerificationService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Autowired
    private KYCProfileRepository kycProfileRepository;
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private EmailOtpService emailOtpService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Create a new user with profile.
     * Equivalent to Django's user creation in CustomRegisterSerializer.
     */
    public User createUser(String username, String email, String password, String phone) {
        try {
            // Check if user already exists
            Optional<User> existingUser = userRepository.findByUsername(username);
            if (existingUser.isPresent()) {
                User user = existingUser.get();
                if (!isFullyVerified(user)) {
                    // Update existing incomplete user
                    user.setEmail(email);
                    user = userRepository.save(user);
                    
                    UserProfile profile = user.getProfile();
                    if (profile == null) {
                        profile = new UserProfile(user);
                        profile = userProfileRepository.save(profile);
                        user.setProfile(profile);
                    }
                    profile.setPhone(phone);
                    userProfileRepository.save(profile);
                    
                    // Generate and send OTP
                    String otp = emailOtpService.setOtp(profile);
                    emailOtpService.sendOtpEmail(profile, otp);
                    notificationService.sendOtpSms(phone, otp);
                    
                    logger.info("Updated existing incomplete user: {}", username);
                    return user;
                }
                throw new IllegalArgumentException("User already exists and is fully verified");
            }
            
            // Create new user
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setEnabled(true);
            user.setRoles("USER");
            user = userRepository.save(user);
            
            // Create user profile
            UserProfile profile = new UserProfile(user);
            profile.setPhone(phone);
            profile = userProfileRepository.save(profile);
            user.setProfile(profile);
            
            // Generate and send OTP
            String otp = emailOtpService.setOtp(profile);
            emailOtpService.sendOtpEmail(profile, otp);
            notificationService.sendOtpSms(phone, otp);
            
            logger.info("Created new user: {}", username);
            return user;
            
        } catch (Exception e) {
            logger.error("Failed to create user {}: {}", username, e.getMessage());
            throw new RuntimeException("Failed to create user", e);
        }
    }
    
    /**
     * Verify user with OTP.
     * Equivalent to Django's verify view.
     */
    public boolean verifyUser(User user, String otp) {
        try {
            UserProfile profile = user.getProfile();
            if (profile == null) {
                profile = new UserProfile(user);
                profile = userProfileRepository.save(profile);
                user.setProfile(profile);
            }
            
            if (!profile.isOtpValid(otp)) {
                return false;
            }
            
            // Clear OTP after successful verification
            profile.clearOtp();
            
            // Mark user as verified
            profile.setIsVerified(true);
            userProfileRepository.save(profile);
            
            logger.info("User {} verified successfully", user.getUsername());
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to verify user {}: {}", user.getUsername(), e.getMessage());
            return false;
        }
    }
    
    /**
     * Request new OTP for user verification.
     * Equivalent to Django's request_verification view.
     */
    public boolean requestVerification(User user) {
        try {
            UserProfile profile = user.getProfile();
            if (profile == null) {
                throw new IllegalStateException("User profile not found");
            }
            
            String otp = emailOtpService.setOtp(profile);
            boolean emailSent = emailOtpService.sendOtpEmail(profile, otp);
            boolean smsSent = notificationService.sendOtpSms(profile.getPhone(), otp);
            
            logger.info("Verification requested for user {}", user.getUsername());
            return emailSent || smsSent;
            
        } catch (Exception e) {
            logger.error("Failed to request verification for user {}: {}", user.getUsername(), e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if user is fully verified.
     * Equivalent to Django's is_fully_verified method.
     */
    public boolean isFullyVerified(User user) {
        try {
            UserProfile profile = user.getProfile();
            if (profile == null || !profile.getIsVerified()) {
                return false;
            }
            
            Optional<KYCProfile> kycOpt = kycProfileRepository.findByUser(user);
            KYCProfile kyc = kycOpt.orElse(null);
            boolean hasKyc = kyc != null && kyc.getIsApproved() && (kyc.getBvn() != null || kyc.getNin() != null);
            
            boolean hasWallet = !walletRepository.findByUser(user).isEmpty();
            
            return hasKyc && hasWallet;
            
        } catch (Exception e) {
            logger.error("Error checking verification status for user {}: {}", user.getUsername(), e.getMessage());
            return false;
        }
    }
    
    /**
     * Resume registration for existing user.
     * Equivalent to Django's resume_registration view.
     */
    public UserRegistrationStatus getRegistrationStatus(String identifier) {
        try {
            User user = findUserByIdentifier(identifier);
            if (user == null) {
                return null;
            }
            
            UserProfile profile = user.getProfile();
            Optional<KYCProfile> kycOpt = kycProfileRepository.findByUser(user);
            KYCProfile kyc = kycOpt.orElse(null);
            boolean hasWallet = !walletRepository.findByUser(user).isEmpty();
            
            UserRegistrationStatus status = new UserRegistrationStatus();
            status.setUsername(user.getUsername());
            status.setEmail(user.getEmail());
            status.setPhone(profile != null ? profile.getPhone() : null);
            status.setVerified(profile != null ? profile.getIsVerified() : false);
            status.setHasKyc(kyc != null && (kyc.getBvn() != null || kyc.getNin() != null));
            status.setHasWallet(hasWallet);
            
            if (kyc != null) {
                UserRegistrationStatus.KycStatus kycStatus = new UserRegistrationStatus.KycStatus();
                kycStatus.setBvn(kyc.getBvn());
                kycStatus.setNin(kyc.getNin());
                kycStatus.setApproved(kyc.getIsApproved());
                status.setKyc(kycStatus);
            }
            
            return status;
            
        } catch (Exception e) {
            logger.error("Error getting registration status for {}: {}", identifier, e.getMessage());
            return null;
        }
    }
    
    /**
     * Find user by identifier (username, email, or phone).
     */
    private User findUserByIdentifier(String identifier) {
        if (identifier.contains("@")) {
            return userRepository.findByEmail(identifier).orElse(null);
        } else if (identifier.matches("\\d{10,}")) {
            Optional<UserProfile> profileOpt = userProfileRepository.findByPhone(identifier);
            UserProfile profile = profileOpt.orElse(null);
            return profile != null ? profile.getUser() : null;
        } else {
            return userRepository.findByUsername(identifier).orElse(null);
        }
    }
    
    /**
     * Delete unverified users after OTP expiry.
     * Equivalent to Django's user deletion on OTP expiry.
     */
    public int cleanupUnverifiedUsers() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusHours(24); // 24 hours grace period
            // Note: This method would need to be implemented in UserRepository
            // For now, return 0 as a placeholder
            int deleted = 0; // userRepository.deleteUnverifiedUsersOlderThan(cutoff);
            
            if (deleted > 0) {
                logger.info("Deleted {} unverified users older than {}", deleted, cutoff);
            }
            
            return deleted;
            
        } catch (Exception e) {
            logger.error("Error cleaning up unverified users: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * Registration status response class.
     */
    public static class UserRegistrationStatus {
        private String username;
        private String email;
        private String phone;
        private boolean verified;
        private boolean hasKyc;
        private boolean hasWallet;
        private KycStatus kyc;
        
        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        
        public boolean isVerified() { return verified; }
        public void setVerified(boolean verified) { this.verified = verified; }
        
        public boolean isHasKyc() { return hasKyc; }
        public void setHasKyc(boolean hasKyc) { this.hasKyc = hasKyc; }
        
        public boolean isHasWallet() { return hasWallet; }
        public void setHasWallet(boolean hasWallet) { this.hasWallet = hasWallet; }
        
        public KycStatus getKyc() { return kyc; }
        public void setKyc(KycStatus kyc) { this.kyc = kyc; }
        
        public static class KycStatus {
            private String bvn;
            private String nin;
            private boolean approved;
            
            public String getBvn() { return bvn; }
            public void setBvn(String bvn) { this.bvn = bvn; }
            
            public String getNin() { return nin; }
            public void setNin(String nin) { this.nin = nin; }
            
            public boolean isApproved() { return approved; }
            public void setApproved(boolean approved) { this.approved = approved; }
        }
    }
}
