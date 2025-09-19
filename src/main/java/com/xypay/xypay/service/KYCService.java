package com.xypay.xypay.service;

import com.xypay.xypay.domain.KYCProfile;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.event.KYCApprovalEvent;
import com.xypay.xypay.repository.KYCProfileRepository;
import com.xypay.xypay.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.UUID;

@Service
public class KYCService {
    
    @Autowired
    private KYCProfileRepository kycProfileRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AuditTrailService auditTrailService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    /**
     * Create KYC profile (matches Django KYCProfileSerializer.create exactly)
     */
    @Transactional
    public Map<String, Object> createKYCProfile(UUID userId, Map<String, Object> kycData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found");
                return response;
            }
            
            User user = userOpt.get();
            
            // Prevent duplicate KYCProfile (matches Django validation)
            if (kycProfileRepository.findByUser(user).isPresent()) {
                response.put("success", false);
                response.put("message", "You already have a KYC profile.");
                return response;
            }
            
            // Validate BVN and NIN (matches Django validation exactly)
            String bvn = (String) kycData.get("bvn");
            String nin = (String) kycData.get("nin");
            
            // Either BVN or NIN is required
            if ((bvn == null || bvn.isEmpty()) && (nin == null || nin.isEmpty())) {
                response.put("success", false);
                response.put("message", "Either BVN or NIN is required.");
                return response;
            }
            
            // Validate BVN
            if (bvn != null && !bvn.isEmpty()) {
                if (!bvn.matches("\\d{11}")) {
                    response.put("success", false);
                    response.put("message", "BVN must be 11 digits.");
                    return response;
                }
                if (kycProfileRepository.existsByBvn(bvn)) {
                    response.put("success", false);
                    response.put("message", "This BVN is already in use.");
                    return response;
                }
            }
            
            // Validate NIN
            if (nin != null && !nin.isEmpty()) {
                if (!nin.matches("\\d{11}")) {
                    response.put("success", false);
                    response.put("message", "NIN must be 11 digits.");
                    return response;
                }
                if (kycProfileRepository.existsByNin(nin)) {
                    response.put("success", false);
                    response.put("message", "This NIN is already in use.");
                    return response;
                }
            }
            
            // Create new KYC profile
            KYCProfile kycProfile = new KYCProfile();
            kycProfile.setUser(user);
            
            // Auto-approve and set tier 1 (matches Django create method exactly)
            kycProfile.setKycLevel(KYCProfile.KYCLevel.TIER_1);
            kycProfile.setIsApproved(true);
            
            // Set all the KYC fields
            if (bvn != null && !bvn.isEmpty()) {
                kycProfile.setBvn(bvn);
            }
            
            if (nin != null && !nin.isEmpty()) {
                kycProfile.setNin(nin);
            }
            
            if (kycData.containsKey("dateOfBirth")) {
                kycProfile.setDateOfBirth(LocalDate.parse((String) kycData.get("dateOfBirth")));
            }
            
            if (kycData.containsKey("address")) {
                kycProfile.setAddress((String) kycData.get("address"));
            }
            
            if (kycData.containsKey("state")) {
                kycProfile.setState((String) kycData.get("state"));
            }
            
            if (kycData.containsKey("lga")) {
                kycProfile.setLga((String) kycData.get("lga"));
            }
            
            if (kycData.containsKey("area")) {
                kycProfile.setArea((String) kycData.get("area"));
            }
            
            if (kycData.containsKey("gender")) {
                String genderStr = (String) kycData.get("gender");
                if (genderStr != null) {
                    kycProfile.setGender(KYCProfile.Gender.valueOf(genderStr.toUpperCase()));
                }
            }
            
            if (kycData.containsKey("telephoneNumber")) {
                kycProfile.setTelephoneNumber((String) kycData.get("telephoneNumber"));
            }
            
            // Document uploads (in production, these would be file paths)
            if (kycData.containsKey("passportPhoto")) {
                kycProfile.setPassportPhoto((String) kycData.get("passportPhoto"));
            }
            
            if (kycData.containsKey("selfie")) {
                kycProfile.setSelfie((String) kycData.get("selfie"));
            }
            
            if (kycData.containsKey("idDocument")) {
                kycProfile.setIdDocument((String) kycData.get("idDocument"));
            }
            
            if (kycData.containsKey("govtIdType")) {
                String govtIdTypeStr = (String) kycData.get("govtIdType");
                if (govtIdTypeStr != null) {
                    kycProfile.setGovtIdType(KYCProfile.GovtIdType.valueOf(govtIdTypeStr.toUpperCase()));
                }
            }
            
            if (kycData.containsKey("govtIdDocument")) {
                kycProfile.setGovtIdDocument((String) kycData.get("govtIdDocument"));
            }
            
            if (kycData.containsKey("proofOfAddress")) {
                kycProfile.setProofOfAddress((String) kycData.get("proofOfAddress"));
            }
            
            kycProfile = kycProfileRepository.save(kycProfile);
            
            // Publish KYC approval event (equivalent to Django post_save signal)
            eventPublisher.publishEvent(new KYCApprovalEvent(this, kycProfile, true, true));
            
            auditTrailService.logEvent("KYC_PROFILE_CREATED", 
                String.format("KYC profile created and auto-approved for user: %s", user.getUsername()));
            
            response.put("success", true);
            response.put("message", "KYC profile created and approved successfully");
            response.put("kyc_level", kycProfile.getKycLevel().getDisplayName());
            response.put("is_approved", kycProfile.getIsApproved());
            response.put("daily_transaction_limit", kycProfile.getDailyTransactionLimit());
            response.put("max_balance_limit", kycProfile.getMaxBalanceLimit());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to create KYC profile: " + e.getMessage());
            
            auditTrailService.logEvent("KYC_CREATION_ERROR", 
                String.format("KYC creation failed for user ID: %s, error: %s", userId, e.getMessage()));
        }
        
        return response;
    }
    
    /**
     * Update existing KYC profile
     */
    @Transactional
    public Map<String, Object> updateKYCProfile(UUID userId, Map<String, Object> kycData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found");
                return response;
            }
            
            User user = userOpt.get();
            Optional<KYCProfile> kycOpt = kycProfileRepository.findByUser(user);
            
            if (kycOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "KYC profile not found. Create one first.");
                return response;
            }
            
            KYCProfile kycProfile = kycOpt.get();
            
            // Update fields (with validation for BVN/NIN uniqueness)
            if (kycData.containsKey("bvn")) {
                String newBvn = (String) kycData.get("bvn");
                if (newBvn != null && !newBvn.isEmpty()) {
                    if (!newBvn.matches("\\d{11}")) {
                        response.put("success", false);
                        response.put("message", "BVN must be 11 digits.");
                        return response;
                    }
                    if (kycProfileRepository.existsByBvn(newBvn) && !newBvn.equals(kycProfile.getBvn())) {
                        response.put("success", false);
                        response.put("message", "This BVN is already in use.");
                        return response;
                    }
                    kycProfile.setBvn(newBvn);
                }
            }
            
            if (kycData.containsKey("nin")) {
                String newNin = (String) kycData.get("nin");
                if (newNin != null && !newNin.isEmpty()) {
                    if (!newNin.matches("\\d{11}")) {
                        response.put("success", false);
                        response.put("message", "NIN must be 11 digits.");
                        return response;
                    }
                    if (kycProfileRepository.existsByNin(newNin) && !newNin.equals(kycProfile.getNin())) {
                        response.put("success", false);
                        response.put("message", "This NIN is already in use.");
                        return response;
                    }
                    kycProfile.setNin(newNin);
                }
            }
            
            // Update other fields
            if (kycData.containsKey("dateOfBirth")) {
                kycProfile.setDateOfBirth(LocalDate.parse((String) kycData.get("dateOfBirth")));
            }
            
            if (kycData.containsKey("address")) {
                kycProfile.setAddress((String) kycData.get("address"));
            }
            
            if (kycData.containsKey("state")) {
                kycProfile.setState((String) kycData.get("state"));
            }
            
            if (kycData.containsKey("lga")) {
                kycProfile.setLga((String) kycData.get("lga"));
            }
            
            if (kycData.containsKey("area")) {
                kycProfile.setArea((String) kycData.get("area"));
            }
            
            if (kycData.containsKey("gender")) {
                String genderStr = (String) kycData.get("gender");
                if (genderStr != null) {
                    kycProfile.setGender(KYCProfile.Gender.valueOf(genderStr.toUpperCase()));
                }
            }
            
            if (kycData.containsKey("telephoneNumber")) {
                kycProfile.setTelephoneNumber((String) kycData.get("telephoneNumber"));
            }
            
            // Document uploads
            if (kycData.containsKey("passportPhoto")) {
                kycProfile.setPassportPhoto((String) kycData.get("passportPhoto"));
            }
            
            if (kycData.containsKey("selfie")) {
                kycProfile.setSelfie((String) kycData.get("selfie"));
            }
            
            if (kycData.containsKey("idDocument")) {
                kycProfile.setIdDocument((String) kycData.get("idDocument"));
            }
            
            if (kycData.containsKey("govtIdType")) {
                String govtIdTypeStr = (String) kycData.get("govtIdType");
                if (govtIdTypeStr != null) {
                    kycProfile.setGovtIdType(KYCProfile.GovtIdType.valueOf(govtIdTypeStr.toUpperCase()));
                }
            }
            
            if (kycData.containsKey("govtIdDocument")) {
                kycProfile.setGovtIdDocument((String) kycData.get("govtIdDocument"));
            }
            
            if (kycData.containsKey("proofOfAddress")) {
                kycProfile.setProofOfAddress((String) kycData.get("proofOfAddress"));
            }
            
            kycProfile = kycProfileRepository.save(kycProfile);
            
            // Publish KYC approval event if approved
            if (kycProfile.getIsApproved()) {
                eventPublisher.publishEvent(new KYCApprovalEvent(this, kycProfile, false, true));
            }
            
            auditTrailService.logEvent("KYC_UPDATE", 
                String.format("KYC profile updated for user: %s", user.getUsername()));
            
            response.put("success", true);
            response.put("message", "KYC profile updated successfully");
            response.put("kyc_level", kycProfile.getKycLevel().getDisplayName());
            response.put("is_approved", kycProfile.getIsApproved());
            response.put("daily_transaction_limit", kycProfile.getDailyTransactionLimit());
            response.put("max_balance_limit", kycProfile.getMaxBalanceLimit());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update KYC profile: " + e.getMessage());
            
            auditTrailService.logEvent("KYC_UPDATE_ERROR", 
                String.format("KYC update failed for user ID: %s, error: %s", userId, e.getMessage()));
        }
        
        return response;
    }
    
    /**
     * Request KYC tier upgrade
     */
    @Transactional
    public Map<String, Object> requestTierUpgrade(UUID userId, String targetTier) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found");
                return response;
            }
            
            User user = userOpt.get();
            Optional<KYCProfile> kycOpt = kycProfileRepository.findByUser(user);
            
            if (kycOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "KYC profile not found");
                return response;
            }
            
            KYCProfile kycProfile = kycOpt.get();
            KYCProfile.KYCLevel targetLevel = KYCProfile.KYCLevel.valueOf(targetTier.toUpperCase());
            
            // Check upgrade eligibility
            boolean canUpgrade = false;
            String message = "";
            
            if (targetLevel == KYCProfile.KYCLevel.TIER_2) {
                canUpgrade = kycProfile.canUpgradeToTier2();
                message = canUpgrade ? "Eligible for Tier 2 upgrade" : "Not eligible for Tier 2 upgrade";
            } else if (targetLevel == KYCProfile.KYCLevel.TIER_3) {
                canUpgrade = kycProfile.canUpgradeToTier3();
                message = canUpgrade ? "Eligible for Tier 3 upgrade" : "Not eligible for Tier 3 upgrade";
            }
            
            if (!canUpgrade) {
                response.put("success", false);
                response.put("message", message);
                response.put("requirements", kycProfile.getUpgradeRequirements(targetLevel));
                return response;
            }
            
            // Mark upgrade as requested
            kycProfile.setUpgradeRequested(true);
            kycProfile.setUpgradeRequestDate(LocalDateTime.now());
            kycProfileRepository.save(kycProfile);
            
            auditTrailService.logEvent("KYC_UPGRADE_REQUEST", 
                String.format("Tier upgrade requested: %s -> %s for user: %s", 
                    kycProfile.getKycLevel().getDisplayName(), targetLevel.getDisplayName(), user.getUsername()));
            
            response.put("success", true);
            response.put("message", "Tier upgrade request submitted successfully");
            response.put("current_tier", kycProfile.getKycLevel().getDisplayName());
            response.put("requested_tier", targetLevel.getDisplayName());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to request tier upgrade: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Get KYC profile by user ID (matches Django KYCProfileDetailSerializer)
     */
    public Map<String, Object> getKYCProfile(UUID userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found");
                return response;
            }
            
            User user = userOpt.get();
            Optional<KYCProfile> kycOpt = kycProfileRepository.findByUser(user);
            
            if (kycOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "KYC profile not found");
                return response;
            }
            
            KYCProfile kycProfile = kycOpt.get();
            Map<String, Object> kycData = new HashMap<>();
            
            // Match Django KYCProfileDetailSerializer fields
            kycData.put("id", kycProfile.getId().toString());
            kycData.put("user", user.getUsername()); // StringRelatedField
            kycData.put("bvn", kycProfile.getBvn());
            kycData.put("nin", kycProfile.getNin());
            kycData.put("date_of_birth", kycProfile.getDateOfBirth());
            kycData.put("state", kycProfile.getState());
            kycData.put("gender", kycProfile.getGender() != null ? kycProfile.getGender().getValue() : null);
            kycData.put("lga", kycProfile.getLga());
            kycData.put("area", kycProfile.getArea());
            kycData.put("address", kycProfile.getAddress());
            kycData.put("telephone_number", kycProfile.getTelephoneNumber());
            kycData.put("passport_photo", kycProfile.getPassportPhoto());
            kycData.put("selfie", kycProfile.getSelfie());
            kycData.put("id_document", kycProfile.getIdDocument());
            kycData.put("govt_id_type", kycProfile.getGovtIdType() != null ? kycProfile.getGovtIdType().getValue() : null);
            kycData.put("govt_id_document", kycProfile.getGovtIdDocument());
            kycData.put("proof_of_address", kycProfile.getProofOfAddress());
            kycData.put("kyc_level", kycProfile.getKycLevel().getValue());
            kycData.put("is_approved", kycProfile.getIsApproved());
            kycData.put("created_at", kycProfile.getCreatedAt());
            kycData.put("updated_at", kycProfile.getUpdatedAt());
            
            // SerializerMethodFields
            kycData.put("daily_transaction_limit", kycProfile.getDailyTransactionLimit());
            kycData.put("max_balance_limit", kycProfile.getMaxBalanceLimit());
            
            kycData.put("upgrade_requested", kycProfile.getUpgradeRequested());
            
            response.put("success", true);
            response.put("kyc_profile", kycData);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get KYC profile: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Get all pending KYC approvals (Admin function)
     */
    public List<Map<String, Object>> getPendingApprovals() {
        List<Map<String, Object>> pendingList = new ArrayList<>();
        
        try {
            List<KYCProfile> pendingProfiles = kycProfileRepository.findPendingApproval();
            
            for (KYCProfile profile : pendingProfiles) {
                Map<String, Object> profileData = new HashMap<>();
                profileData.put("id", profile.getId().toString());
                profileData.put("username", profile.getUser().getUsername());
                profileData.put("email", profile.getUser().getEmail());
                profileData.put("kyc_level", profile.getKycLevel().getDisplayName());
                profileData.put("created_at", profile.getCreatedAt());
                profileData.put("has_bvn", profile.getBvn() != null);
                profileData.put("has_nin", profile.getNin() != null);
                profileData.put("has_documents", profile.getIdDocument() != null);
                
                pendingList.add(profileData);
            }
            
        } catch (Exception e) {
            // Log error but return empty list
            auditTrailService.logEvent("KYC_PENDING_ERROR", 
                "Failed to get pending KYC approvals: " + e.getMessage());
        }
        
        return pendingList;
    }
    
    /**
     * Approve KYC profile (Admin function)
     */
    @Transactional
    public Map<String, Object> approveKYC(String kycProfileId, UUID approverId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<KYCProfile> kycOpt = kycProfileRepository.findById(java.util.UUID.fromString(kycProfileId));
            Optional<User> approverOpt = userRepository.findById(approverId);
            
            if (kycOpt.isEmpty() || approverOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "KYC profile or approver not found");
                return response;
            }
            
            KYCProfile kycProfile = kycOpt.get();
            User approver = approverOpt.get();
            
            kycProfile.approve(approver);
            kycProfile = kycProfileRepository.save(kycProfile);
            
            // Publish KYC approval event
            eventPublisher.publishEvent(new KYCApprovalEvent(this, kycProfile, false, true));
            
            // Send notification to user
            notificationService.notifyUser(kycProfile.getUser().getUsername(), 
                "Your KYC profile has been approved. You can now access full banking services.");
            
            auditTrailService.logEvent("KYC_APPROVAL", 
                String.format("KYC approved for user: %s by: %s", 
                    kycProfile.getUser().getUsername(), approver.getUsername()));
            
            response.put("success", true);
            response.put("message", "KYC profile approved successfully");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to approve KYC: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Reject KYC profile (Admin function)
     */
    @Transactional
    public Map<String, Object> rejectKYC(String kycProfileId, String reason) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<KYCProfile> kycOpt = kycProfileRepository.findById(java.util.UUID.fromString(kycProfileId));
            
            if (kycOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "KYC profile not found");
                return response;
            }
            
            KYCProfile kycProfile = kycOpt.get();
            kycProfile.reject(reason);
            kycProfileRepository.save(kycProfile);
            
            // Send notification to user
            notificationService.notifyUser(kycProfile.getUser().getUsername(), 
                "Your KYC profile has been rejected. Reason: " + reason);
            
            auditTrailService.logEvent("KYC_REJECTION", 
                String.format("KYC rejected for user: %s, reason: %s", 
                    kycProfile.getUser().getUsername(), reason));
            
            response.put("success", true);
            response.put("message", "KYC profile rejected");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to reject KYC: " + e.getMessage());
        }
        
        return response;
    }
}