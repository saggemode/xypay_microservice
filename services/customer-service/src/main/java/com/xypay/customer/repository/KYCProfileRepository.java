package com.xypay.customer.repository;

import com.xypay.customer.domain.KYCProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KYCProfileRepository extends JpaRepository<KYCProfile, Long> {
    
    Optional<KYCProfile> findByUserId(Long userId);
    
    Optional<KYCProfile> findByBvn(String bvn);
    
    Optional<KYCProfile> findByNin(String nin);
    
    @Query("SELECT k FROM KYCProfile k WHERE k.isApproved = true")
    List<KYCProfile> findApprovedProfiles();
    
    @Query("SELECT k FROM KYCProfile k WHERE k.isApproved = false")
    List<KYCProfile> findPendingProfiles();
    
    @Query("SELECT k FROM KYCProfile k WHERE k.kycLevel = :level")
    List<KYCProfile> findByKycLevel(@Param("level") KYCProfile.KYCLevel level);
    
    @Query("SELECT k FROM KYCProfile k WHERE k.upgradeRequested = true")
    List<KYCProfile> findUpgradeRequests();
    
    @Query("SELECT k FROM KYCProfile k WHERE k.state = :state")
    List<KYCProfile> findByState(@Param("state") String state);
    
    @Query("SELECT k FROM KYCProfile k WHERE k.lga = :lga")
    List<KYCProfile> findByLga(@Param("lga") String lga);
}
