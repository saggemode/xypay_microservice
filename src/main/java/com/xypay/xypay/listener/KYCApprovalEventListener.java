package com.xypay.xypay.listener;

import com.xypay.xypay.domain.KYCProfile;
import com.xypay.xypay.domain.StaffActivity;
import com.xypay.xypay.domain.StaffProfile;
import com.xypay.xypay.event.KYCApprovalEvent;
import com.xypay.xypay.repository.StaffActivityRepository;
import com.xypay.xypay.repository.StaffProfileRepository;
import com.xypay.xypay.service.WalletCreationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Event listener for KYC approval events.
 * Equivalent to Django's @receiver(post_save, sender=KYCProfile) signal handlers.
 */
@Component
public class KYCApprovalEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(KYCApprovalEventListener.class);
    
    @Autowired
    private WalletCreationService walletCreationService;
    
    @Autowired
    private StaffProfileRepository staffProfileRepository;
    
    @Autowired
    private StaffActivityRepository staffActivityRepository;
    
    /**
     * Handle KYC approval and create wallet automatically.
     * Equivalent to Django's create_wallet_on_kyc_approval function.
     */
    @EventListener
    @Async
    @Transactional
    public void handleKYCApprovalForWalletCreation(KYCApprovalEvent event) {
        KYCProfile kycProfile = event.getKycProfile();
        
        logger.info("KYC signal triggered - is_approved: {}, created: {}", 
            kycProfile.getIsApproved(), event.isNewlyCreated());
        
        // Auto-approve KYC if BVN data is present (matches Django logic)
        if (!kycProfile.getIsApproved() && kycProfile.getBvn() != null && !kycProfile.getBvn().isEmpty()) {
            logger.info("BVN data present, auto-approving KYC for user {}", kycProfile.getUser().getUsername());
            kycProfile.setIsApproved(true);
            // Note: In a real implementation, you'd save this through the service to trigger another event
        }
        
        // Create wallet when KYC is approved (new or updated)
        if (kycProfile.getIsApproved()) {
            logger.info("KYC is approved, checking for existing wallet for user {}", kycProfile.getUser().getUsername());
            
            try {
                walletCreationService.createWalletOnKYCApproval(kycProfile.getUser());
            } catch (Exception e) {
                logger.error("Error in wallet creation for user {}: {}", 
                    kycProfile.getUser().getUsername(), e.getMessage());
            }
        }
    }
    
    /**
     * Handle KYC approval and log staff activity.
     * Equivalent to Django's handle_kyc_approval function.
     */
    @EventListener
    @Async
    @Transactional
    public void handleKYCApprovalForStaffActivity(KYCApprovalEvent event) {
        KYCProfile kycProfile = event.getKycProfile();
        
        // Only log for updates (not new creations) and when approved
        if (!event.isNewlyCreated() && kycProfile.getIsApproved()) {
            try {
                Optional<StaffProfile> staffMemberOpt = staffProfileRepository.findFirstByCanApproveKycTrueAndIsActiveTrue();
                
                if (staffMemberOpt.isPresent()) {
                    StaffProfile staffMember = staffMemberOpt.get();
                    
                    StaffActivity activity = new StaffActivity(
                        staffMember,
                        StaffActivity.ActivityType.KYC_APPROVED,
                        String.format("KYC approved for user %s", kycProfile.getUser().getUsername())
                    );
                    
                    staffActivityRepository.save(activity);
                    
                    logger.info("Staff activity logged for KYC approval of user {}", 
                        kycProfile.getUser().getUsername());
                }
            } catch (Exception e) {
                logger.error("Error logging staff activity for KYC approval: {}", e.getMessage());
                // Don't throw exception as this is not critical
            }
        }
    }
}
