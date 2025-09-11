package com.xypay.customer.repository;

import com.xypay.customer.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    
    Optional<UserProfile> findByUserId(Long userId);
    
    Optional<UserProfile> findByPhone(String phone);
    
    @Query("SELECT up FROM UserProfile up WHERE up.emailVerified = true")
    List<UserProfile> findVerifiedProfiles();
    
    @Query("SELECT up FROM UserProfile up WHERE up.isVerified = true")
    List<UserProfile> findFullyVerifiedProfiles();
    
    @Query("SELECT up FROM UserProfile up WHERE up.otpCode = :otpCode AND up.otpExpiry > :now")
    Optional<UserProfile> findValidOtp(@Param("otpCode") String otpCode, @Param("now") LocalDateTime now);
    
    @Query("SELECT up FROM UserProfile up WHERE up.emailVerificationToken = :token AND up.emailVerificationTokenExpiry > :now")
    Optional<UserProfile> findValidEmailToken(@Param("token") String token, @Param("now") LocalDateTime now);
    
    @Query("SELECT up FROM UserProfile up WHERE up.phoneVerificationToken = :token AND up.phoneVerificationTokenExpiry > :now")
    Optional<UserProfile> findValidPhoneToken(@Param("token") String token, @Param("now") LocalDateTime now);
}
