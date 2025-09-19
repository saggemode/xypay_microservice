package com.xypay.xypay.repository;

import com.xypay.xypay.domain.KYCProfile;
import com.xypay.xypay.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface KYCProfileRepository extends JpaRepository<KYCProfile, UUID> {
    
    Optional<KYCProfile> findByUser(User user);
    
    Optional<KYCProfile> findByUserId(UUID userId);
    
    Optional<KYCProfile> findByBvn(String bvn);
    
    Optional<KYCProfile> findByNin(String nin);
    
    List<KYCProfile> findByIsApproved(Boolean isApproved);
    
    List<KYCProfile> findByKycLevel(KYCProfile.KYCLevel kycLevel);
    
    List<KYCProfile> findByUpgradeRequested(Boolean upgradeRequested);
    
    @Query("SELECT k FROM KYCProfile k WHERE k.isApproved = false AND k.rejectionReason IS NULL")
    List<KYCProfile> findPendingApproval();
    
    @Query("SELECT k FROM KYCProfile k WHERE k.upgradeRequested = true AND k.isApproved = true")
    List<KYCProfile> findPendingUpgrades();
    
    @Query("SELECT COUNT(k) FROM KYCProfile k WHERE k.kycLevel = :level")
    Long countByKycLevel(@Param("level") KYCProfile.KYCLevel level);
    
    boolean existsByBvn(String bvn);
    
    boolean existsByNin(String nin);
}