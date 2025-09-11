package com.xypay.xypay.repository;

import com.xypay.xypay.domain.FraudDetection;
import com.xypay.xypay.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
@Repository
public interface FraudDetectionRepository extends JpaRepository<FraudDetection, Long> {

    List<FraudDetection> findByUserAndIsResolvedFalseOrderByCreatedAtDesc(User user);

    List<FraudDetection> findByUserAndFlagOrderByCreatedAtDesc(User user, String flag);

    Page<FraudDetection> findByIsResolvedFalseOrderByRiskScoreDescCreatedAtDesc(Pageable pageable);

    @Query("SELECT fd FROM FraudDetection fd WHERE fd.riskScore >= :minScore AND fd.isResolved = false")
    List<FraudDetection> findHighRiskUnresolved(@Param("minScore") Integer minScore);

    @Query("SELECT fd FROM FraudDetection fd WHERE fd.flag = 'CRITICAL' AND fd.isResolved = false")
    List<FraudDetection> findCriticalUnresolved();

    @Query("SELECT COUNT(fd) FROM FraudDetection fd WHERE fd.user = :user AND fd.createdAt >= :startDate")
    Long countByUserSince(@Param("user") User user, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT fd FROM FraudDetection fd WHERE fd.fraudType = :fraudType AND fd.createdAt >= :startDate")
    List<FraudDetection> findByFraudTypeSince(@Param("fraudType") String fraudType, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT AVG(fd.riskScore) FROM FraudDetection fd WHERE fd.user = :user")
    Double getAverageRiskScoreForUser(@Param("user") User user);
}
