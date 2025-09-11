package com.xypay.xypay.repository;

import com.xypay.xypay.domain.UserProfile;
import com.xypay.xypay.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    
    Optional<UserProfile> findByPhone(String phone);
    
    Optional<UserProfile> findByUser(User user);
    
    Optional<UserProfile> findByUserId(Long userId);
    
    Optional<UserProfile> findByUserEmail(String email);
    
    Optional<UserProfile> findByPhoneVerificationToken(String phoneVerificationToken);
    
    Optional<UserProfile> findByUserUsername(String username);
    
    boolean existsByPhone(String phone);
    
    List<UserProfile> findByIsVerifiedFalseAndOtpExpiryBefore(LocalDateTime expiryTime);
    
    @Modifying
    @Transactional
    @Query("UPDATE UserProfile u SET u.otpCode = NULL, u.otpExpiry = NULL WHERE u.otpExpiry < :now")
    int clearExpiredOtps(@Param("now") LocalDateTime now);
}