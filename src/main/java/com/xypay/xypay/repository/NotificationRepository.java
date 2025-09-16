package com.xypay.xypay.repository;

import com.xypay.xypay.domain.Notification;
import com.xypay.xypay.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientOrderByCreatedAtDesc(User user);
    List<Notification> findByRecipientAndSourceOrderByCreatedAtDesc(User user, String source);
    boolean existsByRecipientAndSourceAndExtraDataContaining(User user, String source, String key, String value);
}