package com.xypay.xypay.repository;

import com.xypay.xypay.domain.PaymentIntent;
import com.xypay.xypay.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, UUID> {
    
    List<PaymentIntent> findByUserOrderByCreatedAtDesc(User user);
    
    List<PaymentIntent> findByMerchantIdOrderByCreatedAtDesc(String merchantId);
    
    List<PaymentIntent> findByStatusOrderByCreatedAtDesc(String status);
    
    List<PaymentIntent> findByOrderId(String orderId);
    
    List<PaymentIntent> findByReference(String reference);
    
    @Query("SELECT pi FROM PaymentIntent pi WHERE pi.status = 'pending' AND pi.createdAt < :cutoffTime")
    List<PaymentIntent> findExpiredPendingIntents(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    @Query("SELECT pi FROM PaymentIntent pi WHERE pi.user = :user AND pi.status = :status")
    List<PaymentIntent> findByUserAndStatus(@Param("user") User user, @Param("status") String status);
    
    @Query("SELECT COUNT(pi) FROM PaymentIntent pi WHERE pi.merchantId = :merchantId AND pi.status = 'succeeded'")
    Long countSuccessfulPaymentsByMerchant(@Param("merchantId") String merchantId);
}
