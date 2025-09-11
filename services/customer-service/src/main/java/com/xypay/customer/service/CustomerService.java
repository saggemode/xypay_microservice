package com.xypay.customer.service;

import com.xypay.customer.domain.KYCProfile;
import com.xypay.customer.domain.User;
import com.xypay.customer.domain.UserProfile;
import com.xypay.customer.repository.KYCProfileRepository;
import com.xypay.customer.repository.UserProfileRepository;
import com.xypay.customer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class CustomerService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Autowired
    private KYCProfileRepository kycProfileRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // User Management
    public User createUser(String username, String email, String password, String firstName, String lastName, String roles) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRoles(roles);
        user.setEnabled(true);
        
        User savedUser = userRepository.save(user);
        
        // Create user profile
        UserProfile profile = new UserProfile(savedUser);
        userProfileRepository.save(profile);
        
        return savedUser;
    }
    
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public List<User> getActiveUsers() {
        return userRepository.findActiveUsers();
    }
    
    public User updateUser(Long id, String firstName, String lastName, String email, String roles) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setRoles(roles);
        
        return userRepository.save(user);
    }
    
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    
    // User Profile Management
    public UserProfile getUserProfile(Long userId) {
        return userProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("User profile not found"));
    }
    
    public UserProfile updateUserProfile(Long userId, String phone, Boolean notifyEmail, 
                                       Boolean notifySms, Boolean notifyPush, Boolean notifyInApp) {
        UserProfile profile = getUserProfile(userId);
        
        profile.setPhone(phone);
        profile.setNotifyEmail(notifyEmail);
        profile.setNotifySms(notifySms);
        profile.setNotifyPush(notifyPush);
        profile.setNotifyInApp(notifyInApp);
        
        return userProfileRepository.save(profile);
    }
    
    public void setTransactionPin(Long userId, String pin) {
        UserProfile profile = getUserProfile(userId);
        profile.setTransactionPinHashed(pin);
        userProfileRepository.save(profile);
    }
    
    public boolean verifyTransactionPin(Long userId, String pin) {
        UserProfile profile = getUserProfile(userId);
        return profile.checkTransactionPin(pin);
    }
    
    // OTP Management
    public String generateOTP(Long userId) {
        UserProfile profile = getUserProfile(userId);
        
        String otp = String.format("%06d", new Random().nextInt(1000000));
        profile.setOtpCode(otp);
        profile.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        
        userProfileRepository.save(profile);
        return otp;
    }
    
    public boolean verifyOTP(Long userId, String otp) {
        UserProfile profile = getUserProfile(userId);
        boolean isValid = profile.isOtpValid(otp);
        
        if (isValid) {
            profile.clearOtp();
            userProfileRepository.save(profile);
        }
        
        return isValid;
    }
    
    // Email Verification
    public String generateEmailVerificationToken(Long userId) {
        UserProfile profile = getUserProfile(userId);
        profile.generateEmailVerificationToken();
        userProfileRepository.save(profile);
        return profile.getEmailVerificationToken();
    }
    
    public boolean verifyEmail(Long userId, String token) {
        UserProfile profile = getUserProfile(userId);
        boolean isValid = profile.isEmailVerificationTokenValid(token);
        
        if (isValid) {
            profile.verifyEmail();
            userProfileRepository.save(profile);
        }
        
        return isValid;
    }
    
    // Phone Verification
    public String generatePhoneVerificationToken(Long userId) {
        UserProfile profile = getUserProfile(userId);
        profile.generatePhoneVerificationToken();
        userProfileRepository.save(profile);
        return profile.getPhoneVerificationToken();
    }
    
    public boolean verifyPhone(Long userId, String token) {
        UserProfile profile = getUserProfile(userId);
        boolean isValid = profile.isPhoneVerificationTokenValid(token);
        
        if (isValid) {
            profile.setIsVerified(true);
            userProfileRepository.save(profile);
        }
        
        return isValid;
    }
    
    // KYC Management
    public KYCProfile createKYCProfile(Long userId, String bvn, String nin, 
                                     String dateOfBirth, String address, String state, 
                                     String lga, String area, String telephoneNumber) {
        User user = getUserById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        KYCProfile kycProfile = new KYCProfile();
        kycProfile.setUser(user);
        kycProfile.setBvn(bvn);
        kycProfile.setNin(nin);
        kycProfile.setDateOfBirth(java.time.LocalDate.parse(dateOfBirth));
        kycProfile.setAddress(address);
        kycProfile.setState(state);
        kycProfile.setLga(lga);
        kycProfile.setArea(area);
        kycProfile.setTelephoneNumber(telephoneNumber);
        
        return kycProfileRepository.save(kycProfile);
    }
    
    public Optional<KYCProfile> getKYCProfile(Long userId) {
        return kycProfileRepository.findByUserId(userId);
    }
    
    public List<KYCProfile> getAllKYCProfiles() {
        return kycProfileRepository.findAll();
    }
    
    public List<KYCProfile> getPendingKYCProfiles() {
        return kycProfileRepository.findPendingProfiles();
    }
    
    public List<KYCProfile> getApprovedKYCProfiles() {
        return kycProfileRepository.findApprovedProfiles();
    }
    
    public KYCProfile approveKYCProfile(Long kycId, Long approverId) {
        KYCProfile kycProfile = kycProfileRepository.findById(kycId)
            .orElseThrow(() -> new RuntimeException("KYC Profile not found"));
        
        User approver = getUserById(approverId)
            .orElseThrow(() -> new RuntimeException("Approver not found"));
        
        kycProfile.approve(approver);
        return kycProfileRepository.save(kycProfile);
    }
    
    public KYCProfile rejectKYCProfile(Long kycId, String reason) {
        KYCProfile kycProfile = kycProfileRepository.findById(kycId)
            .orElseThrow(() -> new RuntimeException("KYC Profile not found"));
        
        kycProfile.reject(reason);
        return kycProfileRepository.save(kycProfile);
    }
    
    public KYCProfile upgradeKYCProfile(Long userId, KYCProfile.KYCLevel targetLevel) {
        KYCProfile kycProfile = getKYCProfile(userId)
            .orElseThrow(() -> new RuntimeException("KYC Profile not found"));
        
        if (targetLevel == KYCProfile.KYCLevel.TIER_2) {
            kycProfile.upgradeToTier2();
        } else if (targetLevel == KYCProfile.KYCLevel.TIER_3) {
            kycProfile.upgradeToTier3();
        }
        
        return kycProfileRepository.save(kycProfile);
    }
    
    public boolean canTransactAmount(Long userId, Double amount) {
        KYCProfile kycProfile = getKYCProfile(userId).orElse(null);
        if (kycProfile == null) {
            return false;
        }
        
        return kycProfile.canTransactAmount(amount);
    }
    
    public Double getDailyTransactionLimit(Long userId) {
        KYCProfile kycProfile = getKYCProfile(userId).orElse(null);
        if (kycProfile == null) {
            return 0.0;
        }
        
        return kycProfile.getDailyTransactionLimit();
    }
}
