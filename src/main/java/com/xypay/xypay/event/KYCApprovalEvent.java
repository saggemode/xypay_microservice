package com.xypay.xypay.event;

import com.xypay.xypay.domain.KYCProfile;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a KYC profile is approved or updated.
 * Equivalent to Django's post_save signal for KYCProfile.
 */
public class KYCApprovalEvent extends ApplicationEvent {
    
    private final KYCProfile kycProfile;
    private final boolean isNewlyCreated;
    private final boolean wasApproved;
    
    public KYCApprovalEvent(Object source, KYCProfile kycProfile, boolean isNewlyCreated, boolean wasApproved) {
        super(source);
        this.kycProfile = kycProfile;
        this.isNewlyCreated = isNewlyCreated;
        this.wasApproved = wasApproved;
    }
    
    public KYCProfile getKycProfile() {
        return kycProfile;
    }
    
    public boolean isNewlyCreated() {
        return isNewlyCreated;
    }
    
    public boolean wasApproved() {
        return wasApproved;
    }
    
    @Override
    public String toString() {
        return String.format("KYCApprovalEvent{user=%s, isApproved=%s, isNewlyCreated=%s, wasApproved=%s}", 
            kycProfile.getUser().getUsername(), 
            kycProfile.getIsApproved(),
            isNewlyCreated,
            wasApproved);
    }
}
