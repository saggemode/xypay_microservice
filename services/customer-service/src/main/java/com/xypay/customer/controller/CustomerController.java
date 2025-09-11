package com.xypay.customer.controller;

import com.xypay.customer.domain.KYCProfile;
import com.xypay.customer.domain.User;
import com.xypay.customer.domain.UserProfile;
import com.xypay.customer.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    
    @Autowired
    private CustomerService customerService;
    
    // User Management Endpoints
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody Map<String, String> userData) {
        User user = customerService.createUser(
            userData.get("username"),
            userData.get("email"),
            userData.get("password"),
            userData.get("firstName"),
            userData.get("lastName"),
            userData.get("roles")
        );
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        Optional<User> user = customerService.getUserById(id);
        return user.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        Optional<User> user = customerService.getUserByUsername(username);
        return user.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = customerService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<User>> getActiveUsers() {
        List<User> users = customerService.getActiveUsers();
        return ResponseEntity.ok(users);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody Map<String, String> userData) {
        User user = customerService.updateUser(
            id,
            userData.get("firstName"),
            userData.get("lastName"),
            userData.get("email"),
            userData.get("roles")
        );
        return ResponseEntity.ok(user);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        customerService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
    
    // User Profile Endpoints
    @GetMapping("/{id}/profile")
    public ResponseEntity<UserProfile> getUserProfile(@PathVariable Long id) {
        try {
            UserProfile profile = customerService.getUserProfile(id);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}/profile")
    public ResponseEntity<UserProfile> updateUserProfile(@PathVariable Long id, @RequestBody Map<String, Object> profileData) {
        UserProfile profile = customerService.updateUserProfile(
            id,
            (String) profileData.get("phone"),
            (Boolean) profileData.get("notifyEmail"),
            (Boolean) profileData.get("notifySms"),
            (Boolean) profileData.get("notifyPush"),
            (Boolean) profileData.get("notifyInApp")
        );
        return ResponseEntity.ok(profile);
    }
    
    @PostMapping("/{id}/transaction-pin")
    public ResponseEntity<Void> setTransactionPin(@PathVariable Long id, @RequestBody Map<String, String> pinData) {
        customerService.setTransactionPin(id, pinData.get("pin"));
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/verify-transaction-pin")
    public ResponseEntity<Map<String, Boolean>> verifyTransactionPin(@PathVariable Long id, @RequestBody Map<String, String> pinData) {
        boolean isValid = customerService.verifyTransactionPin(id, pinData.get("pin"));
        return ResponseEntity.ok(Map.of("valid", isValid));
    }
    
    // OTP Endpoints
    @PostMapping("/{id}/generate-otp")
    public ResponseEntity<Map<String, String>> generateOTP(@PathVariable Long id) {
        String otp = customerService.generateOTP(id);
        return ResponseEntity.ok(Map.of("otp", otp));
    }
    
    @PostMapping("/{id}/verify-otp")
    public ResponseEntity<Map<String, Boolean>> verifyOTP(@PathVariable Long id, @RequestBody Map<String, String> otpData) {
        boolean isValid = customerService.verifyOTP(id, otpData.get("otp"));
        return ResponseEntity.ok(Map.of("valid", isValid));
    }
    
    // Email Verification Endpoints
    @PostMapping("/{id}/generate-email-verification")
    public ResponseEntity<Map<String, String>> generateEmailVerificationToken(@PathVariable Long id) {
        String token = customerService.generateEmailVerificationToken(id);
        return ResponseEntity.ok(Map.of("token", token));
    }
    
    @PostMapping("/{id}/verify-email")
    public ResponseEntity<Map<String, Boolean>> verifyEmail(@PathVariable Long id, @RequestBody Map<String, String> tokenData) {
        boolean isValid = customerService.verifyEmail(id, tokenData.get("token"));
        return ResponseEntity.ok(Map.of("valid", isValid));
    }
    
    // Phone Verification Endpoints
    @PostMapping("/{id}/generate-phone-verification")
    public ResponseEntity<Map<String, String>> generatePhoneVerificationToken(@PathVariable Long id) {
        String token = customerService.generatePhoneVerificationToken(id);
        return ResponseEntity.ok(Map.of("token", token));
    }
    
    @PostMapping("/{id}/verify-phone")
    public ResponseEntity<Map<String, Boolean>> verifyPhone(@PathVariable Long id, @RequestBody Map<String, String> tokenData) {
        boolean isValid = customerService.verifyPhone(id, tokenData.get("token"));
        return ResponseEntity.ok(Map.of("valid", isValid));
    }
    
    // KYC Endpoints
    @PostMapping("/{id}/kyc")
    public ResponseEntity<KYCProfile> createKYCProfile(@PathVariable Long id, @RequestBody Map<String, String> kycData) {
        KYCProfile kycProfile = customerService.createKYCProfile(
            id,
            kycData.get("bvn"),
            kycData.get("nin"),
            kycData.get("dateOfBirth"),
            kycData.get("address"),
            kycData.get("state"),
            kycData.get("lga"),
            kycData.get("area"),
            kycData.get("telephoneNumber")
        );
        return ResponseEntity.ok(kycProfile);
    }
    
    @GetMapping("/{id}/kyc")
    public ResponseEntity<KYCProfile> getKYCProfile(@PathVariable Long id) {
        Optional<KYCProfile> kycProfile = customerService.getKYCProfile(id);
        return kycProfile.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/kyc")
    public ResponseEntity<List<KYCProfile>> getAllKYCProfiles() {
        List<KYCProfile> profiles = customerService.getAllKYCProfiles();
        return ResponseEntity.ok(profiles);
    }
    
    @GetMapping("/kyc/pending")
    public ResponseEntity<List<KYCProfile>> getPendingKYCProfiles() {
        List<KYCProfile> profiles = customerService.getPendingKYCProfiles();
        return ResponseEntity.ok(profiles);
    }
    
    @GetMapping("/kyc/approved")
    public ResponseEntity<List<KYCProfile>> getApprovedKYCProfiles() {
        List<KYCProfile> profiles = customerService.getApprovedKYCProfiles();
        return ResponseEntity.ok(profiles);
    }
    
    @PostMapping("/kyc/{kycId}/approve")
    public ResponseEntity<KYCProfile> approveKYCProfile(@PathVariable Long kycId, @RequestBody Map<String, Long> approverData) {
        KYCProfile kycProfile = customerService.approveKYCProfile(kycId, approverData.get("approverId"));
        return ResponseEntity.ok(kycProfile);
    }
    
    @PostMapping("/kyc/{kycId}/reject")
    public ResponseEntity<KYCProfile> rejectKYCProfile(@PathVariable Long kycId, @RequestBody Map<String, String> reasonData) {
        KYCProfile kycProfile = customerService.rejectKYCProfile(kycId, reasonData.get("reason"));
        return ResponseEntity.ok(kycProfile);
    }
    
    @PostMapping("/{id}/kyc/upgrade")
    public ResponseEntity<KYCProfile> upgradeKYCProfile(@PathVariable Long id, @RequestBody Map<String, String> levelData) {
        KYCProfile.KYCLevel targetLevel = KYCProfile.KYCLevel.valueOf(levelData.get("level"));
        KYCProfile kycProfile = customerService.upgradeKYCProfile(id, targetLevel);
        return ResponseEntity.ok(kycProfile);
    }
    
    // Transaction Limits
    @GetMapping("/{id}/transaction-limits")
    public ResponseEntity<Map<String, Object>> getTransactionLimits(@PathVariable Long id) {
        Double dailyLimit = customerService.getDailyTransactionLimit(id);
        boolean canTransact = customerService.canTransactAmount(id, 1000.0); // Example amount
        
        return ResponseEntity.ok(Map.of(
            "dailyLimit", dailyLimit,
            "canTransact", canTransact
        ));
    }
    
    @PostMapping("/{id}/can-transact")
    public ResponseEntity<Map<String, Boolean>> canTransactAmount(@PathVariable Long id, @RequestBody Map<String, Double> amountData) {
        boolean canTransact = customerService.canTransactAmount(id, amountData.get("amount"));
        return ResponseEntity.ok(Map.of("canTransact", canTransact));
    }
}
