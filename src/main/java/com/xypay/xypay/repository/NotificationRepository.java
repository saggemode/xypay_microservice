package com.xypay.xypay.repository;

import com.xypay.xypay.domain.Notification;
import com.xypay.xypay.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.UUID;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByRecipientOrderByCreatedAtDesc(User user);
    List<Notification> findByRecipientAndSourceOrderByCreatedAtDesc(User user, String source);

    @Query(value = "select exists (select 1 from notifications n where n.recipient_id = :#{#user.id} and n.source = :source and n.extra_data ->> :key ILIKE concat('%', :value, '%'))", nativeQuery = true)
    boolean existsByRecipientSourceAndExtraDataKeyValueLike(@Param("user") User user,
                                                            @Param("source") String source,
                                                            @Param("key") String key,
                                                            @Param("value") String value);
}