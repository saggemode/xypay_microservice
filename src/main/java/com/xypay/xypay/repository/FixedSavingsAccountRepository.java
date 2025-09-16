package com.xypay.xypay.repository;

import com.xypay.xypay.domain.FixedSavingsAccount;
import com.xypay.xypay.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface FixedSavingsAccountRepository extends JpaRepository<FixedSavingsAccount, UUID> {
    
    List<FixedSavingsAccount> findByUser(User user);
    
    FixedSavingsAccount findByIdAndUser(UUID id, User user);
    
    List<FixedSavingsAccount> findByUserAndIsActiveTrue(User user);
    
    List<FixedSavingsAccount> findByUserAndIsMaturedTrueAndIsPaidOutFalse(User user);
    
    List<FixedSavingsAccount> findByIsMaturedFalseAndPaybackDateBefore(LocalDate date);
    
    List<FixedSavingsAccount> findByIsMaturedFalseAndPaybackDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT fs FROM FixedSavingsAccount fs WHERE fs.user = :user AND " +
           "(:query IS NULL OR fs.accountNumber LIKE CONCAT('%', :query, '%') OR fs.purposeDescription LIKE CONCAT('%', :query, '%')) AND " +
           "(:purpose IS NULL OR fs.purpose = :purpose) AND " +
           "(:source IS NULL OR fs.source = :source) AND " +
           "(:status = 'active' AND fs.isActive = true AND fs.isMatured = false OR " +
           " :status = 'matured' AND fs.isMatured = true OR " +
           " :status = 'paid_out' AND fs.isPaidOut = true OR " +
           " :status IS NULL)")
    List<FixedSavingsAccount> findByUserWithFilters(@Param("user") User user, 
                                                   @Param("query") String query,
                                                   @Param("purpose") String purpose,
                                                   @Param("source") String source,
                                                   @Param("status") String status);
}