package com.xypay.xypay.repository;

import com.xypay.xypay.domain.TargetSaving;
import com.xypay.xypay.domain.TargetSavingCategory;
import com.xypay.xypay.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TargetSavingRepository extends JpaRepository<TargetSaving, UUID> {
    List<TargetSaving> findByUser(User user);
    List<TargetSaving> findByUserAndIsActiveTrue(User user);
    List<TargetSaving> findByUserAndIsCompletedTrue(User user);
    List<TargetSaving> findByUserAndIsActiveTrueAndIsCompletedFalse(User user);
    Optional<TargetSaving> findByIdAndUser(UUID id, User user);
    boolean existsByAccountNumber(String accountNumber);
    
    // Additional query methods
    List<TargetSaving> findByUserAndCategory(User user, TargetSavingCategory category);
    List<TargetSaving> findByUserAndIsActiveTrueAndEndDateBefore(User user, LocalDate date);
    List<TargetSaving> findByUserAndIsActiveTrueAndEndDateBetween(User user, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT ts FROM TargetSaving ts WHERE ts.user = :user AND " +
           "(:query IS NULL OR ts.name LIKE CONCAT('%', :query, '%') OR ts.accountNumber LIKE CONCAT('%', :query, '%')) AND " +
           "(:category IS NULL OR ts.category = :category) AND " +
           "(:isActive IS NULL OR ts.isActive = :isActive) AND " +
           "(:isCompleted IS NULL OR ts.isCompleted = :isCompleted)")
    List<TargetSaving> findByUserWithFilters(@Param("user") User user, 
                                           @Param("query") String query,
                                           @Param("category") TargetSavingCategory category,
                                           @Param("isActive") Boolean isActive,
                                           @Param("isCompleted") Boolean isCompleted);
}
