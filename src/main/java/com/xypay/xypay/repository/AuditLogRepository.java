package com.xypay.xypay.repository;

import com.xypay.xypay.domain.AuditLog;
import com.xypay.xypay.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUserAndActionAndTimestampAfter(User user, String action, LocalDateTime timestamp);
    
    List<AuditLog> findByTimestampBefore(LocalDateTime timestamp);
    
    @Query("SELECT a FROM AuditLog a WHERE a.user = :user AND a.action = :action ORDER BY a.timestamp DESC")
    List<AuditLog> findRecentByUserAndAction(@Param("user") User user, @Param("action") String action);
    
    List<AuditLog> findByActionAndIpAddressAndTimestampAfter(String action, String ipAddress, LocalDateTime timestamp);
    
    @Modifying
    @Query("UPDATE AuditLog a SET a.user = null WHERE a.user.id = :userId")
    void nullifyUserReferences(@Param("userId") Long userId);
    
    @Modifying
    @Query("DELETE FROM AuditLog a WHERE a.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}