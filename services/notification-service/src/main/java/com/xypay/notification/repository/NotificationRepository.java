package com.xypay.notification.repository;

import com.xypay.notification.domain.Notification;
import com.xypay.notification.domain.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<Notification> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, NotificationStatus status);
    
    List<Notification> findByUserIdAndStatus(Long userId, NotificationStatus status);
    
    long countByUserId(Long userId);
    
    long countByUserIdAndStatus(Long userId, NotificationStatus status);
    
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.status = :status")
    Page<Notification> findByUserIdAndStatus(@Param("userId") Long userId, 
                                           @Param("status") NotificationStatus status, 
                                           Pageable pageable);
}
