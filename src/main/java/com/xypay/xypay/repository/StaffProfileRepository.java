package com.xypay.xypay.repository;

import com.xypay.xypay.domain.StaffProfile;
import com.xypay.xypay.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffProfileRepository extends JpaRepository<StaffProfile, Long> {
    
    Optional<StaffProfile> findByUser(User user);
    
    Optional<StaffProfile> findByEmployeeId(String employeeId);
    
    @Query("SELECT s FROM StaffProfile s WHERE s.canApproveKyc = true AND s.isActive = true")
    Optional<StaffProfile> findFirstByCanApproveKycTrueAndIsActiveTrue();
    
    boolean existsByEmployeeId(String employeeId);
}
