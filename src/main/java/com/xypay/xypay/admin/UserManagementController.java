package com.xypay.xypay.admin;

import com.xypay.xypay.domain.User;
import com.xypay.xypay.domain.UserProfile;
import com.xypay.xypay.domain.KYCProfile;
import com.xypay.xypay.domain.UserSession;
import com.xypay.xypay.domain.Wallet;
import com.xypay.xypay.domain.BankTransfer;
import com.xypay.xypay.repository.AuditLogRepository;
import com.xypay.xypay.repository.UserRepository;
import com.xypay.xypay.repository.UserProfileRepository;
import com.xypay.xypay.repository.KYCProfileRepository;
import com.xypay.xypay.repository.UserSessionRepository;
import com.xypay.xypay.repository.WalletRepository;
import com.xypay.xypay.repository.BankTransferRepository;
import com.xypay.xypay.service.UserCascadeDeleteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class UserManagementController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserManagementController.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Autowired
    private KYCProfileRepository kycProfileRepository;
    
    @Autowired
    private UserSessionRepository userSessionRepository;
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private BankTransferRepository bankTransferRepository;
    
    @Autowired
    private UserCascadeDeleteService userCascadeDeleteService;
    
    @Autowired
    private com.xypay.xypay.service.BankTransferEventPublisher eventPublisher;
    
    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "admin-users";
    }
    
    @GetMapping("/users/new")
    public String newUserForm(Model model) {
        model.addAttribute("user", new User());
        return "admin-user-form";
    }
    
    @PostMapping("/users")
    public String createUser(@ModelAttribute User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "redirect:/admin/users";
    }
    
    @GetMapping("/users/{id}/edit")
    public String editUserForm(@PathVariable UUID id, Model model) {
        User user = userRepository.findById(id).orElseThrow();
        model.addAttribute("user", user);
        return "admin-user-form";
    }
    
    @PostMapping("/users/{id}")
    public String updateUser(@PathVariable UUID id, @ModelAttribute User user) {
        User existingUser = userRepository.findById(id).orElseThrow();
        existingUser.setUsername(user.getUsername());
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setEmail(user.getEmail());
        existingUser.setRoles(user.getRoles());
        existingUser.setEnabled(user.isEnabled());
        userRepository.save(existingUser);
        return "redirect:/admin/users";
    }
    
    @GetMapping("/user-profiles")
    public String listUserProfiles(Model model) {
        List<User> users = userRepository.findAllWithProfiles();
        model.addAttribute("users", users);
        model.addAttribute("totalProfiles", users.size());
        return "admin/user-profiles";
    }
    
    @GetMapping("/user-profiles/{id}")
    public String viewUserProfile(@PathVariable UUID id, Model model) {
        User user = userRepository.findByIdWithProfile(id).orElseThrow();
        model.addAttribute("user", user);
        return "admin/user-profile-detail";
    }
    
    @GetMapping("/user-profiles/{id}/edit")
    public String editUserProfileForm(@PathVariable UUID id, Model model) {
        User user = userRepository.findByIdWithProfile(id).orElseThrow();
        model.addAttribute("user", user);
        return "admin/user-profile-edit";
    }
    
    @PostMapping("/user-profiles/{id}/edit")
    public String updateUserProfile(@PathVariable UUID id, 
                                  @RequestParam(required = false) String email,
                                  @RequestParam(required = false) String phone,
                                  @RequestParam(required = false) Boolean isVerified,
                                  @RequestParam(required = false) String otpCode,
                                  @RequestParam(required = false) String otpExpiryDate,
                                  @RequestParam(required = false) String otpExpiryTime,
                                  @RequestParam(required = false) Boolean notifyEmail,
                                  @RequestParam(required = false) Boolean notifySms,
                                  @RequestParam(required = false) Boolean notifyPush,
                                  @RequestParam(required = false) Boolean notifyInApp) {
        User user = userRepository.findByIdWithProfile(id).orElseThrow();
        
        // Update user email if provided
        if (email != null && !email.trim().isEmpty()) {
            user.setEmail(email.trim());
        }
        
        // Update user profile if it exists, or create a new one
        UserProfile profile = user.getProfile();
        if (profile == null) {
            // Create new profile
            profile = new UserProfile();
            profile.setUser(user);
            user.setProfile(profile);
        }
        
        // Update profile fields
        if (phone != null) profile.setPhone(phone);
        if (isVerified != null) profile.setIsVerified(isVerified);
        if (otpCode != null) profile.setOtpCode(otpCode);
        if (notifyEmail != null) profile.setNotifyEmail(notifyEmail);
        if (notifySms != null) profile.setNotifySms(notifySms);
        if (notifyPush != null) profile.setNotifyPush(notifyPush);
        if (notifyInApp != null) profile.setNotifyInApp(notifyInApp);
        
        // Handle OTP expiry date/time
        if (otpExpiryDate != null && otpExpiryTime != null && !otpExpiryDate.isEmpty() && !otpExpiryTime.isEmpty()) {
            try {
                java.time.LocalDateTime expiryDateTime = java.time.LocalDateTime.parse(otpExpiryDate + "T" + otpExpiryTime);
                profile.setOtpExpiry(expiryDateTime);
            } catch (Exception e) {
                // Handle parsing error if needed
            }
        }
        
        // Save the profile
        userProfileRepository.save(profile);
        userRepository.save(user);
        
        return "redirect:/admin/user-profiles";
    }
    
    @PostMapping("/user-profiles/{id}/delete")
    @Transactional
    public String deleteUserProfile(@PathVariable UUID id, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            // Use the cascade delete service to properly clean up all related data
            boolean deleted = userCascadeDeleteService.deleteUserAndAllRelatedData(id);
            if (deleted) {
                logger.info("Successfully deleted user profile and all related data for user ID: {}", id);
                redirectAttributes.addFlashAttribute("successMessage", "User and all related data have been successfully deleted.");
            } else {
                logger.warn("Failed to delete user profile for user ID: {}", id);
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete user. User may not exist or has pending transactions.");
            }
        } catch (Exception e) {
            logger.error("Error deleting user profile for user ID {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while deleting the user: " + e.getMessage());
        }
        return "redirect:/admin/user-profiles";
    }
    
    @PostMapping("/users/{id}/delete")
    @Transactional
    public String deleteUser(@PathVariable UUID id) {
        // First nullify audit log references to preserve audit trail
        auditLogRepository.nullifyUserReferences(id);
        
        // Then delete the user
        userRepository.deleteById(id);
        return "redirect:/admin/users";
    }
    
    // KYC Profiles Management
    @GetMapping("/kyc-profiles")
    public String listKYCProfiles(Model model) {
        List<KYCProfile> kycProfiles = kycProfileRepository.findAll();
        model.addAttribute("kycProfiles", kycProfiles);
        model.addAttribute("totalProfiles", kycProfiles.size());
        return "admin/kyc-profiles";
    }

    @GetMapping("/kyc-profiles/new")
    public String newKYCProfileForm(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        model.addAttribute("kycProfile", new KYCProfile());
        return "admin/kyc-profile-new";
    }

    @PostMapping("/kyc-profiles")
    public String createKYCProfile(@RequestParam UUID userId,
                                 @RequestParam(required = false) String bvn,
                                 @RequestParam(required = false) String nin,
                                 @RequestParam(required = false) String dateOfBirth,
                                 @RequestParam(required = false) String state,
                                 @RequestParam(required = false) String lga,
                                 @RequestParam(required = false) String area,
                                 @RequestParam(required = false) String gender,
                                 @RequestParam(required = false) String address,
                                 @RequestParam(required = false) String telephoneNumber,
                                 @RequestParam(required = false) String kycLevel,
                                 @RequestParam(required = false) String passportPhoto,
                                 @RequestParam(required = false) String selfie,
                                 @RequestParam(required = false) String idDocument,
                                 @RequestParam(required = false) String govtIdType,
                                 @RequestParam(required = false) String govtIdDocument,
                                 @RequestParam(required = false) String proofOfAddress,
                                 @RequestParam(required = false) Boolean isApproved,
                                 org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(userId).orElseThrow();

        KYCProfile profile = new KYCProfile();
        profile.setUser(user);
        if (bvn != null) profile.setBvn(bvn);
        if (nin != null) profile.setNin(nin);
        if (dateOfBirth != null && !dateOfBirth.isEmpty()) {
            try { profile.setDateOfBirth(java.time.LocalDate.parse(dateOfBirth)); } catch (Exception ignored) {}
        }
        if (state != null) profile.setState(state);
        if (lga != null) profile.setLga(lga);
        if (gender != null && !gender.isEmpty()) {
            try { profile.setGender(KYCProfile.Gender.valueOf(gender)); } catch (Exception ignored) {}
        }
        if (address != null) profile.setAddress(address);
        if (area != null) profile.setArea(area);
        if (telephoneNumber != null) profile.setTelephoneNumber(telephoneNumber);
        if (kycLevel != null && !kycLevel.isEmpty()) {
            try { profile.setKycLevel(KYCProfile.KYCLevel.valueOf(kycLevel)); } catch (Exception ignored) {}
        }
        if (isApproved != null) profile.setIsApproved(isApproved);

        if (passportPhoto != null) profile.setPassportPhoto(passportPhoto);
        if (selfie != null) profile.setSelfie(selfie);
        if (idDocument != null) profile.setIdDocument(idDocument);
        if (govtIdType != null && !govtIdType.isEmpty()) {
            try { profile.setGovtIdType(KYCProfile.GovtIdType.valueOf(govtIdType)); } catch (Exception ignored) {}
        }
        if (govtIdDocument != null) profile.setGovtIdDocument(govtIdDocument);
        if (proofOfAddress != null) profile.setProofOfAddress(proofOfAddress);

        kycProfileRepository.save(profile);
        redirectAttributes.addFlashAttribute("success", "KYC profile created for user: " + user.getUsername());
        return "redirect:/admin/kyc-profiles";
    }
    
    @GetMapping("/kyc-profiles/{id}")
    public String viewKYCProfile(@PathVariable UUID id, Model model) {
        KYCProfile kycProfile = kycProfileRepository.findById(id).orElseThrow();
        model.addAttribute("kycProfile", kycProfile);
        return "admin/kyc-profile-detail";
    }
    
    @GetMapping("/kyc-profiles/{id}/edit")
    public String editKYCProfileForm(@PathVariable UUID id, Model model) {
        KYCProfile kycProfile = kycProfileRepository.findById(id).orElseThrow();
        model.addAttribute("kycProfile", kycProfile);
        return "admin/kyc-profile-edit";
    }
    
    @PostMapping("/kyc-profiles/{id}/edit")
    public String updateKYCProfile(@PathVariable UUID id,
                                 @RequestParam(required = false) String bvn,
                                 @RequestParam(required = false) String nin,
                                 @RequestParam(required = false) String dateOfBirth,
                                 @RequestParam(required = false) String state,
                                 @RequestParam(required = false) String lga,
                                 @RequestParam(required = false) String gender,
                                 @RequestParam(required = false) String address,
                                 @RequestParam(required = false) String telephoneNumber,
                                 @RequestParam(required = false) String kycLevel,
                                 @RequestParam(required = false) Boolean isApproved) {
        KYCProfile kycProfile = kycProfileRepository.findById(id).orElseThrow();
        
        // Update KYC profile fields
        if (bvn != null) kycProfile.setBvn(bvn);
        if (nin != null) kycProfile.setNin(nin);
        if (dateOfBirth != null && !dateOfBirth.isEmpty()) {
            kycProfile.setDateOfBirth(java.time.LocalDate.parse(dateOfBirth));
        }
        if (state != null) kycProfile.setState(state);
        if (lga != null) kycProfile.setLga(lga);
        if (gender != null) {
            kycProfile.setGender(KYCProfile.Gender.valueOf(gender));
        }
        if (address != null) kycProfile.setAddress(address);
        if (telephoneNumber != null) kycProfile.setTelephoneNumber(telephoneNumber);
        if (kycLevel != null) {
            kycProfile.setKycLevel(KYCProfile.KYCLevel.valueOf(kycLevel));
        }
        if (isApproved != null) kycProfile.setIsApproved(isApproved);
        
        kycProfileRepository.save(kycProfile);
        return "redirect:/admin/kyc-profiles";
    }
    
    @PostMapping("/kyc-profiles/{id}/delete")
    public String deleteKYCProfile(@PathVariable UUID id) {
        kycProfileRepository.deleteById(id);
        return "redirect:/admin/kyc-profiles";
    }
    
    @PostMapping("/kyc-profiles/{id}/approve")
    public String approveKYCProfile(@PathVariable UUID id) {
        KYCProfile kycProfile = kycProfileRepository.findById(id).orElseThrow();
        kycProfile.setIsApproved(true);
        kycProfile.setApprovedAt(java.time.LocalDateTime.now());
        kycProfileRepository.save(kycProfile);
        return "redirect:/admin/kyc-profiles";
    }
    
    @PostMapping("/kyc-profiles/{id}/reject")
    public String rejectKYCProfile(@PathVariable UUID id, @RequestParam String rejectionReason) {
        KYCProfile kycProfile = kycProfileRepository.findById(id).orElseThrow();
        kycProfile.setIsApproved(false);
        kycProfile.setRejectionReason(rejectionReason);
        kycProfileRepository.save(kycProfile);
        return "redirect:/admin/kyc-profiles";
    }
    
    // Bulk Operations for KYC Profiles
    @PostMapping("/kyc-profiles/bulk-delete")
    public String bulkDeleteKYCProfiles(@RequestParam("selectedIds") List<UUID> selectedIds) {
        kycProfileRepository.deleteAllById(selectedIds);
        return "redirect:/admin/kyc-profiles";
    }
    
    @PostMapping("/kyc-profiles/bulk-approve")
    public String bulkApproveKYCProfiles(@RequestParam("selectedIds") List<UUID> selectedIds) {
        List<KYCProfile> profiles = kycProfileRepository.findAllById(selectedIds);
        for (KYCProfile profile : profiles) {
            profile.setIsApproved(true);
            profile.setApprovedAt(java.time.LocalDateTime.now());
        }
        kycProfileRepository.saveAll(profiles);
        return "redirect:/admin/kyc-profiles";
    }
    
    @PostMapping("/kyc-profiles/bulk-reject")
    public String bulkRejectKYCProfiles(@RequestParam("selectedIds") List<UUID> selectedIds, 
                                      @RequestParam String rejectionReason) {
        List<KYCProfile> profiles = kycProfileRepository.findAllById(selectedIds);
        for (KYCProfile profile : profiles) {
            profile.setIsApproved(false);
            profile.setRejectionReason(rejectionReason);
        }
        kycProfileRepository.saveAll(profiles);
        return "redirect:/admin/kyc-profiles";
    }
    
    @PostMapping("/kyc-profiles/bulk-upgrade-tier2")
    public String bulkUpgradeToTier2(@RequestParam("selectedIds") List<UUID> selectedIds) {
        List<KYCProfile> profiles = kycProfileRepository.findAllById(selectedIds);
        for (KYCProfile profile : profiles) {
            if (profile.getIsApproved() && profile.getKycLevel() == KYCProfile.KYCLevel.TIER_1) {
                profile.setKycLevel(KYCProfile.KYCLevel.TIER_2);
            }
        }
        kycProfileRepository.saveAll(profiles);
        return "redirect:/admin/kyc-profiles";
    }
    
    @PostMapping("/kyc-profiles/bulk-upgrade-tier3")
    public String bulkUpgradeToTier3(@RequestParam("selectedIds") List<UUID> selectedIds) {
        List<KYCProfile> profiles = kycProfileRepository.findAllById(selectedIds);
        for (KYCProfile profile : profiles) {
            if (profile.getIsApproved() && profile.getKycLevel() == KYCProfile.KYCLevel.TIER_2) {
                profile.setKycLevel(KYCProfile.KYCLevel.TIER_3);
            }
        }
        kycProfileRepository.saveAll(profiles);
        return "redirect:/admin/kyc-profiles";
    }
    
    @PostMapping("/kyc-profiles/bulk-downgrade-tier1")
    public String bulkDowngradeToTier1(@RequestParam("selectedIds") List<UUID> selectedIds) {
        List<KYCProfile> profiles = kycProfileRepository.findAllById(selectedIds);
        for (KYCProfile profile : profiles) {
            if (profile.getIsApproved()) {
                profile.setKycLevel(KYCProfile.KYCLevel.TIER_1);
            }
        }
        kycProfileRepository.saveAll(profiles);
        return "redirect:/admin/kyc-profiles";
    }
    
    @GetMapping("/kyc-profiles/export-csv")
    public ResponseEntity<String> exportKYCProfilesCSV(@RequestParam("selectedIds") List<UUID> selectedIds) {
        List<KYCProfile> profiles = kycProfileRepository.findAllById(selectedIds);
        
        StringBuilder csv = new StringBuilder();
        csv.append("ID,User,BVN,NIN,KYC Level,Date of Birth,State,LGA,Gender,Is Approved,Created At\n");
        
        for (KYCProfile profile : profiles) {
            csv.append(profile.getId()).append(",");
            csv.append(profile.getUser() != null ? profile.getUser().getUsername() : "N/A").append(",");
            csv.append(profile.getBvn() != null ? profile.getBvn() : "").append(",");
            csv.append(profile.getNin() != null ? profile.getNin() : "").append(",");
            csv.append(profile.getKycLevel() != null ? profile.getKycLevel().getDisplayName() : "").append(",");
            csv.append(profile.getDateOfBirth() != null ? profile.getDateOfBirth().toString() : "").append(",");
            csv.append(profile.getState() != null ? profile.getState() : "").append(",");
            csv.append(profile.getLga() != null ? profile.getLga() : "").append(",");
            csv.append(profile.getGender() != null ? profile.getGender().getDisplayName() : "").append(",");
            csv.append(profile.getIsApproved() ? "Yes" : "No").append(",");
            csv.append(profile.getCreatedAt() != null ? profile.getCreatedAt().toString() : "").append("\n");
        }
        
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=kyc_profiles.csv")
                .body(csv.toString());
    }
    
    @GetMapping("/kyc-profiles/tier-requirements")
    public ResponseEntity<Map<String, Object>> getTierRequirements() {
        Map<String, Object> requirements = new HashMap<>();
        requirements.put("TIER_1", Map.of(
            "name", "Tier 1",
            "requirements", List.of("Valid phone number", "Email verification", "Basic personal information"),
            "limits", Map.of("daily_transaction", "₦50,000", "monthly_transaction", "₦500,000")
        ));
        requirements.put("TIER_2", Map.of(
            "name", "Tier 2", 
            "requirements", List.of("Tier 1 requirements", "BVN verification", "Government ID", "Address proof"),
            "limits", Map.of("daily_transaction", "₦200,000", "monthly_transaction", "₦2,000,000")
        ));
        requirements.put("TIER_3", Map.of(
            "name", "Tier 3",
            "requirements", List.of("Tier 2 requirements", "NIN verification", "Enhanced due diligence", "Additional documentation"),
            "limits", Map.of("daily_transaction", "₦1,000,000", "monthly_transaction", "₦10,000,000")
        ));
        return ResponseEntity.ok(requirements);
    }
    
    @GetMapping("/kyc-profiles/check-eligibility")
    public ResponseEntity<Map<String, Object>> checkUpgradeEligibility(@RequestParam("selectedIds") List<UUID> selectedIds) {
        List<KYCProfile> profiles = kycProfileRepository.findAllById(selectedIds);
        Map<String, Object> results = new HashMap<>();
        
        for (KYCProfile profile : profiles) {
            Map<String, Object> eligibility = new HashMap<>();
            eligibility.put("currentTier", profile.getKycLevel().getDisplayName());
            eligibility.put("isApproved", profile.getIsApproved());
            eligibility.put("canUpgrade", profile.getIsApproved() && 
                (profile.getKycLevel() == KYCProfile.KYCLevel.TIER_1 || profile.getKycLevel() == KYCProfile.KYCLevel.TIER_2));
            eligibility.put("missingRequirements", getMissingRequirements(profile));
            
            results.put(profile.getId().toString(), eligibility);
        }
        
        return ResponseEntity.ok(results);
    }
    
    private List<String> getMissingRequirements(KYCProfile profile) {
        List<String> missing = new ArrayList<>();
        
        if (profile.getBvn() == null || profile.getBvn().isEmpty()) {
            missing.add("BVN");
        }
        if (profile.getNin() == null || profile.getNin().isEmpty()) {
            missing.add("NIN");
        }
        if (profile.getAddress() == null || profile.getAddress().isEmpty()) {
            missing.add("Address");
        }
        if (profile.getState() == null || profile.getState().isEmpty()) {
            missing.add("State");
        }
        if (profile.getLga() == null || profile.getLga().isEmpty()) {
            missing.add("LGA");
        }
        
        return missing;
    }
    
    // User Session Management
    @GetMapping("/user-sessions")
    public String listUserSessions(Model model,
                                 @RequestParam(required = false) String search,
                                 @RequestParam(required = false) String isActive,
                                 @RequestParam(required = false) String ipAddress,
                                 @RequestParam(required = false) String createdFrom,
                                 @RequestParam(required = false) String createdTo) {
        
        List<UserSession> sessions = userSessionRepository.findAll();
        
        // If no sessions exist, create some sample data for demonstration
        if (sessions.isEmpty()) {
            List<User> users = userRepository.findAll();
            if (!users.isEmpty()) {
                User sampleUser = users.get(0);
                
                // Create sample sessions
                UserSession session1 = new UserSession(sampleUser, "sample_session_1", "127.0.0.1", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                session1.setCreatedAt(java.time.LocalDateTime.now().minusHours(2));
                session1.setLastActivity(java.time.LocalDateTime.now().minusMinutes(30));
                userSessionRepository.save(session1);
                
                UserSession session2 = new UserSession(sampleUser, "sample_session_2", "192.168.1.100", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36");
                session2.setCreatedAt(java.time.LocalDateTime.now().minusDays(1));
                session2.setLastActivity(java.time.LocalDateTime.now().minusHours(5));
                userSessionRepository.save(session2);
                
                UserSession session3 = new UserSession(sampleUser, "sample_session_3", "10.0.0.1", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36");
                session3.setCreatedAt(java.time.LocalDateTime.now().minusDays(3));
                session3.setLastActivity(java.time.LocalDateTime.now().minusDays(2));
                session3.setIsActive(false);
                userSessionRepository.save(session3);
                
                // Refresh the sessions list
                sessions = userSessionRepository.findAll();
            }
        }
        
        // Apply filters
        if (search != null && !search.trim().isEmpty()) {
            sessions = sessions.stream()
                .filter(session -> 
                    session.getUser().getUsername().toLowerCase().contains(search.toLowerCase()) ||
                    session.getIpAddress().contains(search) ||
                    (session.getUserAgent() != null && session.getUserAgent().toLowerCase().contains(search.toLowerCase()))
                )
                .collect(java.util.stream.Collectors.toList());
        }
        
        if (isActive != null && !isActive.isEmpty()) {
            boolean active = Boolean.parseBoolean(isActive);
            sessions = sessions.stream()
                .filter(session -> session.getIsActive().equals(active))
                .collect(java.util.stream.Collectors.toList());
        }
        
        if (ipAddress != null && !ipAddress.trim().isEmpty()) {
            sessions = sessions.stream()
                .filter(session -> session.getIpAddress().contains(ipAddress))
                .collect(java.util.stream.Collectors.toList());
        }
        
        // Sort by last activity descending
        sessions.sort((a, b) -> b.getLastActivity().compareTo(a.getLastActivity()));
        
        // Calculate session durations
        sessions.forEach(session -> {
            if (session.getCreatedAt() != null && session.getLastActivity() != null) {
                long hours = java.time.Duration.between(session.getCreatedAt(), session.getLastActivity()).toHours();
                long minutes = java.time.Duration.between(session.getCreatedAt(), session.getLastActivity()).toMinutesPart();
                session.setSessionDuration(String.format("%d.%d hours", hours, minutes));
            }
        });
        
        model.addAttribute("sessions", sessions);
        model.addAttribute("totalSessions", sessions.size());
        model.addAttribute("search", search);
        model.addAttribute("isActive", isActive);
        model.addAttribute("ipAddress", ipAddress);
        model.addAttribute("createdFrom", createdFrom);
        model.addAttribute("createdTo", createdTo);
        
        return "admin/user-sessions";
    }
    
    @GetMapping("/user-sessions/{id}")
    public String viewUserSession(@PathVariable UUID id, Model model) {
        UserSession session = userSessionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User session not found"));
        
        model.addAttribute("session", session);
        return "admin/user-session-detail";
    }
    
    @PostMapping("/user-sessions/{id}/deactivate")
    public String deactivateUserSession(@PathVariable UUID id) {
        UserSession session = userSessionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User session not found"));
        
        session.deactivate();
        userSessionRepository.save(session);
        
        return "redirect:/admin/user-sessions";
    }
    
    @PostMapping("/user-sessions/bulk-deactivate")
    public String bulkDeactivateUserSessions(@RequestParam("selectedIds") List<UUID> selectedIds) {
        List<UserSession> sessions = userSessionRepository.findAllById(selectedIds);
        for (UserSession session : sessions) {
            session.deactivate();
        }
        userSessionRepository.saveAll(sessions);
        
        return "redirect:/admin/user-sessions";
    }
    
    @PostMapping("/user-sessions/bulk-delete")
    public String bulkDeleteUserSessions(@RequestParam("selectedIds") List<UUID> selectedIds) {
        userSessionRepository.deleteAllById(selectedIds);
        return "redirect:/admin/user-sessions";
    }
    
    // Wallet Management
    @GetMapping("/wallets")
    public String listWallets(Model model,
                            @RequestParam(required = false) String search,
                            @RequestParam(required = false) String currency,
                            @RequestParam(required = false) String createdFrom,
                            @RequestParam(required = false) String createdTo) {
        
        List<Wallet> wallets = walletRepository.findAllWithUser();
        
        // If no wallets exist, create some sample data for demonstration
        if (wallets.isEmpty()) {
            List<User> users = userRepository.findAll();
            if (!users.isEmpty()) {
                User sampleUser1 = users.get(0);
                User sampleUser2 = users.size() > 1 ? users.get(1) : sampleUser1;
                
                // Create sample wallets
                Wallet wallet1 = new Wallet();
                wallet1.setUser(sampleUser1);
                wallet1.setAccountNumber("7038655955");
                wallet1.setAlternativeAccountNumber("8774879296");
                wallet1.setBalance(new java.math.BigDecimal("10000.00"));
                wallet1.setCurrency("NGN");
                wallet1.setPhoneAlias("+2347038655955");
                wallet1.setCreatedAt(java.time.LocalDateTime.now().minusDays(1));
                walletRepository.save(wallet1);
                
                Wallet wallet2 = new Wallet();
                wallet2.setUser(sampleUser2);
                wallet2.setAccountNumber("7038655950");
                wallet2.setAlternativeAccountNumber("7671976761");
                wallet2.setBalance(new java.math.BigDecimal("156009.86"));
                wallet2.setCurrency("NGN");
                wallet2.setPhoneAlias("+2347038655950");
                wallet2.setCreatedAt(java.time.LocalDateTime.now().minusDays(2));
                walletRepository.save(wallet2);
                
                // Refresh the wallets list
                wallets = walletRepository.findAllWithUser();
            }
        }
        
        // Apply filters
        if (search != null && !search.trim().isEmpty()) {
            wallets = wallets.stream()
                .filter(wallet -> 
                    wallet.getUser().getUsername().toLowerCase().contains(search.toLowerCase()) ||
                    wallet.getAccountNumber().contains(search) ||
                    wallet.getAlternativeAccountNumber().contains(search) ||
                    (wallet.getPhoneAlias() != null && wallet.getPhoneAlias().contains(search))
                )
                .collect(java.util.stream.Collectors.toList());
        }
        
        if (currency != null && !currency.isEmpty()) {
            wallets = wallets.stream()
                .filter(wallet -> wallet.getCurrency().equals(currency))
                .collect(java.util.stream.Collectors.toList());
        }
        
        // Sort by created date descending
        wallets.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        
        model.addAttribute("wallets", wallets);
        model.addAttribute("totalWallets", wallets.size());
        model.addAttribute("search", search);
        model.addAttribute("currency", currency);
        model.addAttribute("createdFrom", createdFrom);
        model.addAttribute("createdTo", createdTo);
        
        return "admin/wallets";
    }
    
    @GetMapping("/wallets/{id}")
    public String viewWallet(@PathVariable UUID id, Model model) {
        Wallet wallet = walletRepository.findByIdWithUser(id)
            .orElseThrow(() -> new RuntimeException("Wallet not found"));
        
        model.addAttribute("wallet", wallet);
        return "admin/wallet-detail";
    }
    
    @GetMapping("/wallets/add")
    public String addWalletForm(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        model.addAttribute("wallet", new Wallet());
        return "admin/wallet-add";
    }
    
    @PostMapping("/wallets/add")
    public String addWallet(@ModelAttribute Wallet wallet,
                          @RequestParam UUID userId,
                          @RequestParam String currency) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        wallet.setUser(user);
        wallet.setCurrency(currency);
        wallet.setBalance(java.math.BigDecimal.ZERO);
        
        // Generate account numbers if not provided
        if (wallet.getAccountNumber() == null || wallet.getAccountNumber().isEmpty()) {
            wallet.setAccountNumber(generateAccountNumber());
        }
        if (wallet.getAlternativeAccountNumber() == null || wallet.getAlternativeAccountNumber().isEmpty()) {
            wallet.setAlternativeAccountNumber(generateAlternativeAccountNumber());
        }
        
        walletRepository.save(wallet);
        return "redirect:/admin/wallets";
    }
    
    @PostMapping("/wallets/{id}/delete")
    public String deleteWallet(@PathVariable UUID id) {
        walletRepository.deleteById(id);
        return "redirect:/admin/wallets";
    }
    
    @PostMapping("/wallets/bulk-delete")
    public String bulkDeleteWallets(@RequestParam("selectedIds") List<UUID> selectedIds) {
        walletRepository.deleteAllById(selectedIds);
        return "redirect:/admin/wallets";
    }
    
    @GetMapping("/wallets/{id}/edit")
    public String editWalletForm(@PathVariable UUID id, Model model) {
        Wallet wallet = walletRepository.findByIdWithUser(id)
            .orElseThrow(() -> new RuntimeException("Wallet not found"));
        
        model.addAttribute("wallet", wallet);
        return "admin/wallet-edit";
    }
    
    @PostMapping("/wallets/{id}/edit")
    public String updateWallet(@PathVariable UUID id,
                             @RequestParam String accountNumber,
                             @RequestParam String alternativeAccountNumber,
                             @RequestParam(required = false) String phoneAlias,
                             @RequestParam java.math.BigDecimal balance,
                             @RequestParam String currency) {
        Wallet wallet = walletRepository.findByIdWithUser(id)
            .orElseThrow(() -> new RuntimeException("Wallet not found"));
        
        // Update wallet fields
        wallet.setAccountNumber(accountNumber);
        wallet.setAlternativeAccountNumber(alternativeAccountNumber);
        wallet.setPhoneAlias(phoneAlias);
        wallet.setBalance(balance);
        wallet.setCurrency(currency);
        
        walletRepository.save(wallet);
        return "redirect:/admin/wallets/" + id;
    }
    
    private String generateAccountNumber() {
        return String.format("%010d", System.currentTimeMillis() % 10000000000L);
    }
    
    private String generateAlternativeAccountNumber() {
        return String.format("%010d", (System.currentTimeMillis() + 1000000000L) % 10000000000L);
    }
    
    // Bank Transfer Management
    @GetMapping("/bank-transfers")
    public String listBankTransfers(Model model,
                                  @RequestParam(required = false) String search,
                                  @RequestParam(required = false) String status,
                                  @RequestParam(required = false) String transferType,
                                  @RequestParam(required = false) String bankName,
                                  @RequestParam(required = false) String accountNumber) {
        
        List<BankTransfer> transfers = bankTransferRepository.findAll();
        
        // If no transfers exist, create some sample data for demonstration
        if (transfers.isEmpty()) {
            List<User> users = userRepository.findAll();
            if (!users.isEmpty()) {
                User sampleUser = users.get(0);
                
                // Create sample transfers
                BankTransfer transfer1 = new BankTransfer();
                transfer1.setUser(sampleUser);
                transfer1.setBankName("Access Bank");
                transfer1.setBankCode("044");
                transfer1.setAccountNumber("1234567890");
                transfer1.setAmount(new java.math.BigDecimal("10000.00"));
                transfer1.setFee(new java.math.BigDecimal("50.00"));
                transfer1.setVat(new java.math.BigDecimal("7.50"));
                transfer1.setLevy(new java.math.BigDecimal("0.00"));
                transfer1.setReference("TRF-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                transfer1.setStatus("successful");
                transfer1.setTransferType("inter");
                transfer1.setDescription("Transfer to family");
                transfer1.setCreatedAt(java.time.LocalDateTime.now().minusHours(2));
                bankTransferRepository.save(transfer1);
                
                BankTransfer transfer2 = new BankTransfer();
                transfer2.setUser(sampleUser);
                transfer2.setBankName("First Bank");
                transfer2.setBankCode("011");
                transfer2.setAccountNumber("9876543210");
                transfer2.setAmount(new java.math.BigDecimal("5000.00"));
                transfer2.setFee(new java.math.BigDecimal("25.00"));
                transfer2.setVat(new java.math.BigDecimal("3.75"));
                transfer2.setLevy(new java.math.BigDecimal("0.00"));
                transfer2.setReference("TRF-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                transfer2.setStatus("pending");
                transfer2.setTransferType("intra");
                transfer2.setDescription("Business payment");
                transfer2.setCreatedAt(java.time.LocalDateTime.now().minusMinutes(30));
                bankTransferRepository.save(transfer2);
                
                // Refresh the transfers list
                transfers = bankTransferRepository.findAll();
            }
        }
        
        // Apply filters
        if (search != null && !search.trim().isEmpty()) {
            transfers = transfers.stream()
                .filter(transfer -> 
                    transfer.getReference().toLowerCase().contains(search.toLowerCase()) ||
                    (transfer.getDescription() != null && transfer.getDescription().toLowerCase().contains(search.toLowerCase())) ||
                    transfer.getAccountNumber().contains(search)
                )
                .collect(java.util.stream.Collectors.toList());
        }
        
        if (status != null && !status.isEmpty()) {
            transfers = transfers.stream()
                .filter(transfer -> transfer.getStatus().equals(status))
                .collect(java.util.stream.Collectors.toList());
        }
        
        if (transferType != null && !transferType.isEmpty()) {
            transfers = transfers.stream()
                .filter(transfer -> transfer.getTransferType().equals(transferType))
                .collect(java.util.stream.Collectors.toList());
        }
        
        if (bankName != null && !bankName.trim().isEmpty()) {
            transfers = transfers.stream()
                .filter(transfer -> transfer.getBankName().toLowerCase().contains(bankName.toLowerCase()))
                .collect(java.util.stream.Collectors.toList());
        }
        
        if (accountNumber != null && !accountNumber.trim().isEmpty()) {
            transfers = transfers.stream()
                .filter(transfer -> transfer.getAccountNumber().contains(accountNumber))
                .collect(java.util.stream.Collectors.toList());
        }
        
        // Sort by created date descending
        transfers.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        
        model.addAttribute("transfers", transfers);
        model.addAttribute("totalTransfers", transfers.size());
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("transferType", transferType);
        model.addAttribute("bankName", bankName);
        model.addAttribute("accountNumber", accountNumber);
        
        return "admin/bank-transfers";
    }
    
    @GetMapping("/bank-transfers/{id}")
    public String viewBankTransfer(@PathVariable UUID id, Model model) {
        BankTransfer transfer = bankTransferRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Bank transfer not found"));
        
        model.addAttribute("transfer", transfer);
        return "admin/bank-transfer-detail";
    }
    
    @GetMapping("/bank-transfers/{id}/edit")
    public String editBankTransferForm(@PathVariable UUID id, Model model) {
        BankTransfer transfer = bankTransferRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Bank transfer not found"));
        
        model.addAttribute("transfer", transfer);
        return "admin/bank-transfer-edit";
    }
    
    @PostMapping("/bank-transfers/{id}/edit")
    public String updateBankTransfer(@PathVariable UUID id,
                                   @RequestParam String bankName,
                                   @RequestParam String bankCode,
                                   @RequestParam String accountNumber,
                                   @RequestParam java.math.BigDecimal amount,
                                   @RequestParam(required = false) String description,
                                   @RequestParam String status) {
        BankTransfer transfer = bankTransferRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Bank transfer not found"));
        
        // Update transfer fields
        transfer.setBankName(bankName);
        transfer.setBankCode(bankCode);
        transfer.setAccountNumber(accountNumber);
        transfer.setAmount(amount);
        transfer.setDescription(description);
        transfer.setStatus(status);
        
        bankTransferRepository.save(transfer);
        return "redirect:/admin/bank-transfers/" + id;
    }
    
    @PostMapping("/bank-transfers/{id}/process")
    public String processBankTransfer(@PathVariable UUID id) {
        BankTransfer transfer = bankTransferRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Bank transfer not found"));
        
        if ("pending".equals(transfer.getStatus())) {
            transfer.setStatus("processing");
            transfer.setProcessingStartedAt(java.time.LocalDateTime.now());
            bankTransferRepository.save(transfer);
        }
        
        return "redirect:/admin/bank-transfers/" + id;
    }
    
    @PostMapping("/bank-transfers/{id}/retry")
    public String retryBankTransfer(@PathVariable UUID id) {
        BankTransfer transfer = bankTransferRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Bank transfer not found"));
        
        if ("failed".equals(transfer.getStatus())) {
            transfer.setStatus("pending");
            transfer.setRetryCount(transfer.getRetryCount() + 1);
            transfer.setLastRetryAt(java.time.LocalDateTime.now());
            bankTransferRepository.save(transfer);
        }
        
        return "redirect:/admin/bank-transfers/" + id;
    }

    // Bulk actions for bank transfers
    @PostMapping("/bank-transfers/bulk")
    public Object bulkBankTransferAction(@RequestParam("action") String action,
                                         @RequestParam(value = "selectedIds", required = false) String selectedIdsCsv,
                                         org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (selectedIdsCsv == null || selectedIdsCsv.isBlank()) {
            return ResponseEntity.badRequest().body("No items selected");
        }
        List<UUID> ids = java.util.Arrays.stream(selectedIdsCsv.split(","))
                .filter(s -> !s.isBlank())
                .map(UUID::fromString)
                .toList();
        java.util.List<com.xypay.xypay.domain.BankTransfer> transfers = bankTransferRepository.findAllById(ids);
        switch (action) {
            case "delete" -> {
                bankTransferRepository.deleteAll(transfers);
                redirectAttributes.addFlashAttribute("success", "Deleted " + transfers.size() + " transfers");
                return "redirect:/admin/bank-transfers";
            }
            case "mark-completed" -> {
                transfers.forEach(t -> t.setStatus("completed"));
                bankTransferRepository.saveAll(transfers);
                redirectAttributes.addFlashAttribute("success", "Marked completed: " + transfers.size());
                return "redirect:/admin/bank-transfers";
            }
            case "mark-failed" -> {
                transfers.forEach(t -> t.setStatus("failed"));
                bankTransferRepository.saveAll(transfers);
                redirectAttributes.addFlashAttribute("success", "Marked failed: " + transfers.size());
                return "redirect:/admin/bank-transfers";
            }
            case "export-csv" -> {
                StringBuilder csv = new StringBuilder();
                csv.append("ID,Reference,User,Bank,Account,Amount,Status,Type,Created\n");
                for (var t : transfers) {
                    csv.append(t.getId()).append(',')
                       .append(safe(t.getReference())).append(',')
                       .append(t.getUser()!=null?safe(t.getUser().getUsername()):"").append(',')
                       .append(safe(t.getBankName())).append(',')
                       .append(safe(t.getAccountNumber())).append(',')
                       .append(t.getAmount()).append(',')
                       .append(safe(t.getStatus())).append(',')
                       .append(safe(t.getTransferType())).append(',')
                       .append(t.getCreatedAt()!=null?t.getCreatedAt().toString():"").append('\n');
                }
                return ResponseEntity.ok()
                        .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bank_transfers.csv")
                        .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "text/csv")
                        .body(csv.toString());
            }
            case "export-xlsx" -> {
                try (var wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
                    var sheet = wb.createSheet("Bank Transfers");
                    int r = 0;
                    var header = sheet.createRow(r++);
                    String[] cols = {"ID","Reference","User","Bank","Account","Amount","Status","Type","Created"};
                    for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);
                    for (var t : transfers) {
                        var row = sheet.createRow(r++);
                        row.createCell(0).setCellValue(t.getId().toString());
                        row.createCell(1).setCellValue(nvl(t.getReference()));
                        row.createCell(2).setCellValue(t.getUser()!=null?nvl(t.getUser().getUsername()):"");
                        row.createCell(3).setCellValue(nvl(t.getBankName()));
                        row.createCell(4).setCellValue(nvl(t.getAccountNumber()));
                        row.createCell(5).setCellValue(t.getAmount()!=null?t.getAmount().doubleValue():0);
                        row.createCell(6).setCellValue(nvl(t.getStatus()));
                        row.createCell(7).setCellValue(nvl(t.getTransferType()));
                        row.createCell(8).setCellValue(t.getCreatedAt()!=null?t.getCreatedAt().toString():"");
                    }
                    java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
                    wb.write(out);
                    return ResponseEntity.ok()
                            .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bank_transfers.xlsx")
                            .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                            .body(out.toByteArray());
                } catch (Exception e) {
                    return ResponseEntity.internalServerError().body("Failed to export: " + e.getMessage());
                }
            }
            default -> {
                redirectAttributes.addFlashAttribute("error", "Unknown action: " + action);
                return "redirect:/admin/bank-transfers";
            }
        }
    }

    private static String safe(String v){ return v==null?"":v.replace(","," "); }
    private static String nvl(String v){ return v==null?"":v; }
    // Bank Transfer Creation Methods
    @GetMapping("/bank-transfers/new")
    public String newBankTransferForm(Model model) {
        // Get all users for the dropdown
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        model.addAttribute("bankTransfer", new BankTransfer());
        return "admin/bank-transfer-form";
    }

    @PostMapping("/bank-transfers/new")
    public String createBankTransfer(@ModelAttribute BankTransfer bankTransfer,
                                   @RequestParam UUID userId,
                                   @RequestParam String bankName,
                                   @RequestParam String accountNumber,
                                   @RequestParam java.math.BigDecimal amount,
                                   @RequestParam(required = false) java.math.BigDecimal fee,
                                   @RequestParam(required = false) java.math.BigDecimal vat,
                                   @RequestParam(required = false) java.math.BigDecimal levy,
                                   @RequestParam String transferType,
                                   @RequestParam(required = false) String status,
                                   @RequestParam(required = false) String description,
                                   @RequestParam(required = false) String nibssReference,
                                   @RequestParam(value = "action", required = false) String action,
                                   org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        
        // Set the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        bankTransfer.setUser(user);

        // Pre-validate receiver account and self-transfer using both primary and alternative account numbers
        java.util.Optional<Wallet> senderWalletOpt = walletRepository.findByUser(user).stream().findFirst();
        java.util.Optional<Wallet> receiverWalletOpt = walletRepository.findByAccountNumberOrAlternativeAccountNumber(accountNumber, accountNumber);

        if (senderWalletOpt.isPresent()) {
            Wallet senderWallet = senderWalletOpt.get();
            boolean isSelf = accountNumber.equals(senderWallet.getAccountNumber()) ||
                             accountNumber.equals(senderWallet.getAlternativeAccountNumber());
            if (isSelf) {
                redirectAttributes.addFlashAttribute("error", "Self-transfer is not allowed. Please enter a different recipient account.");
                return "redirect:/admin/bank-transfers/new";
            }
        }

        if (receiverWalletOpt.isEmpty()) {
            // No internal wallet matches; if this is intra-bank, block with message. If inter-bank, allow and it will go to processing.
            if ("intra".equalsIgnoreCase(transferType)) {
                redirectAttributes.addFlashAttribute("error", "Recipient account not found. For intra-bank transfers, the recipient must exist in the system.");
                return "redirect:/admin/bank-transfers/new";
            }
        }
        
        // Set other properties
        bankTransfer.setBankName(bankName);
        bankTransfer.setAccountNumber(accountNumber);
        bankTransfer.setAmount(amount);
        bankTransfer.setFee(fee != null ? fee : java.math.BigDecimal.ZERO);
        bankTransfer.setVat(vat != null ? vat : java.math.BigDecimal.ZERO);
        bankTransfer.setLevy(levy != null ? levy : java.math.BigDecimal.ZERO);
        bankTransfer.setTransferType(transferType);
        bankTransfer.setDescription(description);
        bankTransfer.setNibssReference(nibssReference);
        if (status != null && !status.isBlank()) {
            bankTransfer.setStatus(status);
        } else {
            bankTransfer.setStatus("pending");
        }
        bankTransfer.setCreatedAt(java.time.LocalDateTime.now());
        bankTransfer.setUpdatedAt(java.time.LocalDateTime.now());
        
        // Generate reference if not provided
        if (bankTransfer.getReference() == null || bankTransfer.getReference().isEmpty()) {
            bankTransfer.setReference("BT" + System.currentTimeMillis());
        }
        
        bankTransfer = bankTransferRepository.save(bankTransfer);
        
        // Trigger the signal system to process the transfer
        eventPublisher.publishTransferCreatedEvent(bankTransfer);
        
        if ("save_and_add".equalsIgnoreCase(action)) {
            redirectAttributes.addFlashAttribute("success", "Bank transfer created: " + bankTransfer.getReference());
            return "redirect:/admin/bank-transfers/new";
        }
        if ("save_and_continue".equalsIgnoreCase(action)) {
            redirectAttributes.addFlashAttribute("success", "Bank transfer created: " + bankTransfer.getReference());
            return "redirect:/admin/bank-transfers/" + bankTransfer.getId() + "/edit";
        }
        redirectAttributes.addFlashAttribute("success", "Bank transfer created: " + bankTransfer.getReference());
        return "redirect:/admin/bank-transfers";
    }
}
