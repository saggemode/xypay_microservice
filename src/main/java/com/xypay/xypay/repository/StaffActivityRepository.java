package com.xypay.xypay.repository;

import com.xypay.xypay.domain.StaffActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface StaffActivityRepository extends JpaRepository<StaffActivity, UUID> {
    
    List<StaffActivity> findByStaffOrderByTimestampDesc(UUID staffId);
    
    List<StaffActivity> findByActivityTypeOrderByTimestampDesc(StaffActivity.ActivityType activityType);
    
    List<StaffActivity> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT sa FROM StaffActivity sa WHERE sa.staff.id = :staffId AND sa.timestamp >= :startDate ORDER BY sa.timestamp DESC")
    List<StaffActivity> findByStaffAndTimestampAfter(@Param("staffId") UUID staffId, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT sa FROM StaffActivity sa WHERE sa.activityType = :activityType AND sa.timestamp >= :startDate ORDER BY sa.timestamp DESC")
    List<StaffActivity> findByActivityTypeAndTimestampAfter(@Param("activityType") StaffActivity.ActivityType activityType, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(sa) FROM StaffActivity sa WHERE sa.staff.id = :staffId AND sa.activityType = :activityType AND sa.timestamp >= :startDate")
    Long countByStaffAndActivityTypeAndTimestampAfter(@Param("staffId") UUID staffId, @Param("activityType") StaffActivity.ActivityType activityType, @Param("startDate") LocalDateTime startDate);
}