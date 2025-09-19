package com.xypay.xypay.repository;

import com.xypay.xypay.domain.XySaveInvestment;
import com.xypay.xypay.domain.XySaveAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface XySaveInvestmentRepository extends JpaRepository<XySaveInvestment, UUID> {
    
    List<XySaveInvestment> findByXysaveAccount(XySaveAccount account);
    
    List<XySaveInvestment> findByXysaveAccountAndIsActiveTrue(XySaveAccount account);
    
    List<XySaveInvestment> findByXysaveAccountOrderByCreatedAtDesc(XySaveAccount account);
    
    @Query("SELECT i FROM XySaveInvestment i WHERE i.xysaveAccount.user.id = :userId ORDER BY i.createdAt DESC")
    List<XySaveInvestment> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId);
    
    @Query("SELECT i FROM XySaveInvestment i WHERE i.xysaveAccount.user.id = :userId AND i.isActive = true")
    List<XySaveInvestment> findActiveInvestmentsByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT i FROM XySaveInvestment i WHERE i.maturityDate <= :date AND i.isActive = true")
    List<XySaveInvestment> findMaturedInvestments(@Param("date") LocalDate date);
    
    @Query("SELECT i FROM XySaveInvestment i WHERE i.xysaveAccount.user.id = :userId AND i.investmentType = :investmentType")
    List<XySaveInvestment> findByUserIdAndInvestmentType(@Param("userId") UUID userId, 
                                                        @Param("investmentType") XySaveInvestment.InvestmentType investmentType);
}