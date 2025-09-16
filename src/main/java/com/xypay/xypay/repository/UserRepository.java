package com.xypay.xypay.repository;

import com.xypay.xypay.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    
    // Registration monitoring methods
    @Query("SELECT COUNT(u) FROM User u JOIN u.profile p WHERE p.emailVerified = true AND p.isVerified = true")
    long countVerifiedUsers();
    
    @Query("SELECT COUNT(u) FROM User u JOIN u.profile p WHERE p.emailVerified = false OR p.isVerified = false")
    long countPendingVerifications();
    
    @Query("SELECT COUNT(u) FROM User u JOIN u.profile p WHERE p.emailVerified = false AND p.isVerified = true")
    long countEmailPendingUsers();
    
    @Query("SELECT COUNT(u) FROM User u JOIN u.profile p WHERE p.emailVerified = true AND p.isVerified = false")
    long countPhonePendingUsers();
    
    @Query("SELECT COUNT(u) FROM User u JOIN u.profile p WHERE p.emailVerified = false AND p.isVerified = false")
    long countBothPendingUsers();
    
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT u FROM User u ORDER BY u.createdAt DESC LIMIT :limit")
    List<User> findTopByOrderByCreatedAtDesc(int limit);
    
    @Query("SELECT u FROM User u JOIN u.profile p WHERE p.emailVerified = false OR p.isVerified = false")
    List<User> findUsersNeedingVerification();
    
    @Query("SELECT u FROM User u ORDER BY u.createdAt DESC")
    List<User> findRecentUsers();
    
    // User profile management methods
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.profile ORDER BY u.createdAt DESC")
    List<User> findAllWithProfiles();
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.profile WHERE u.id = :id")
    Optional<User> findByIdWithProfile(UUID id);
}
