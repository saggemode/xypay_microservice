package com.xypay.xypay.repository;

import com.xypay.xypay.domain.StaffRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRoleRepository extends JpaRepository<StaffRole, java.util.UUID> {
    
    List<StaffRole> findByOrderByLevelAsc();
    
    Optional<StaffRole> findByName(StaffRole.RoleType name);
    
    List<StaffRole> findByLevelGreaterThanEqual(Integer level);
    
    List<StaffRole> findByCanApproveKycTrue();
    
    List<StaffRole> findByCanManageStaffTrue();
}
