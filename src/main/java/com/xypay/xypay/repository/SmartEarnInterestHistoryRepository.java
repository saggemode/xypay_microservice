package com.xypay.xypay.repository;

import com.xypay.xypay.domain.SmartEarnAccount;
import com.xypay.xypay.domain.SmartEarnInterestHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SmartEarnInterestHistoryRepository extends JpaRepository<SmartEarnInterestHistory, UUID> {
    
    /**
     * Find interest history by SmartEarn account
     */
    List<SmartEarnInterestHistory> findBySmartEarnAccountOrderByInterestDateDesc(SmartEarnAccount smartEarnAccount);
    
    /**
     * Find interest history by account and date
     */
    Optional<SmartEarnInterestHistory> findBySmartEarnAccountAndInterestDate(SmartEarnAccount smartEarnAccount, LocalDate interestDate);
    
    /**
     * Find interest history by date range
     */
    @Query("SELECT sih FROM SmartEarnInterestHistory sih WHERE sih.smartEarnAccount = :account AND sih.interestDate BETWEEN :startDate AND :endDate ORDER BY sih.interestDate DESC")
    List<SmartEarnInterestHistory> findInterestHistoryByDateRange(@Param("account") SmartEarnAccount account,
                                                                 @Param("startDate") LocalDate startDate,
                                                                 @Param("endDate") LocalDate endDate);
    
    /**
     * Find uncredited interest history
     */
    @Query("SELECT sih FROM SmartEarnInterestHistory sih WHERE sih.smartEarnAccount = :account AND sih.isCredited = false ORDER BY sih.interestDate ASC")
    List<SmartEarnInterestHistory> findUncreditedInterestHistory(@Param("account") SmartEarnAccount account);
    
    /**
     * Find interest history that needs to be credited
     */
    @Query("SELECT sih FROM SmartEarnInterestHistory sih WHERE sih.isCredited = false AND sih.interestDate <= :currentDate ORDER BY sih.interestDate ASC")
    List<SmartEarnInterestHistory> findInterestHistoryReadyForCrediting(@Param("currentDate") LocalDate currentDate);
    
    /**
     * Get total interest earned for an account
     */
    @Query("SELECT COALESCE(SUM(sih.interestEarned), 0) FROM SmartEarnInterestHistory sih WHERE sih.smartEarnAccount = :account")
    BigDecimal getTotalInterestEarned(@Param("account") SmartEarnAccount account);
    
    /**
     * Get total credited interest for an account
     */
    @Query("SELECT COALESCE(SUM(sih.interestEarned), 0) FROM SmartEarnInterestHistory sih WHERE sih.smartEarnAccount = :account AND sih.isCredited = true")
    BigDecimal getTotalCreditedInterest(@Param("account") SmartEarnAccount account);
    
    /**
     * Get total uncredited interest for an account
     */
    @Query("SELECT COALESCE(SUM(sih.interestEarned), 0) FROM SmartEarnInterestHistory sih WHERE sih.smartEarnAccount = :account AND sih.isCredited = false")
    BigDecimal getTotalUncreditedInterest(@Param("account") SmartEarnAccount account);
    
    /**
     * Find interest history by date
     */
    List<SmartEarnInterestHistory> findByInterestDateOrderByCreatedAtAsc(LocalDate interestDate);
    
    /**
     * Find latest interest history for an account
     */
    @Query("SELECT sih FROM SmartEarnInterestHistory sih WHERE sih.smartEarnAccount = :account ORDER BY sih.interestDate DESC")
    List<SmartEarnInterestHistory> findLatestInterestHistory(@Param("account") SmartEarnAccount account);
    
    /**
     * Check if interest history exists for date
     */
    boolean existsBySmartEarnAccountAndInterestDate(SmartEarnAccount smartEarnAccount, LocalDate interestDate);
    
    /**
     * Count interest history records for an account
     */
    long countBySmartEarnAccount(SmartEarnAccount smartEarnAccount);
    
    /**
     * Find interest history by credited status
     */
    List<SmartEarnInterestHistory> findByIsCreditedOrderByInterestDateDesc(Boolean isCredited);
    
    /**
     * Find interest history by date range with pagination
     */
    Page<SmartEarnInterestHistory> findByInterestDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
}
