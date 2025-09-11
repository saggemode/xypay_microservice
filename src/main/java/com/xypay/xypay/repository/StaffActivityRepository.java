package com.xypay.xypay.repository;

import com.xypay.xypay.domain.StaffActivity;
import com.xypay.xypay.domain.StaffProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface StaffActivityRepository extends JpaRepository<StaffActivity, Long> {
    
    List<StaffActivity> findByStaffOrderByCreatedAtDesc(StaffProfile staff);
    
    List<StaffActivity> findByActivityTypeOrderByCreatedAtDesc(String activityType);
    
    @Query("SELECT sa FROM StaffActivity sa WHERE sa.createdAt BETWEEN :startDate AND :endDate ORDER BY sa.createdAt DESC")
    List<StaffActivity> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT sa FROM StaffActivity sa WHERE sa.staff = :staff AND sa.activityType = :activityType ORDER BY sa.createdAt DESC")
    List<StaffActivity> findByStaffAndActivityType(@Param("staff") StaffProfile staff, @Param("activityType") String activityType);
}
