package com.xypay.xypay.repository;

import com.xypay.xypay.domain.TransferFeeRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TransferFeeRuleRepository extends JpaRepository<TransferFeeRule, java.util.UUID> {
    
    List<TransferFeeRule> findByIsActiveTrueOrderByPriorityDesc();
    
    @Query("SELECT tfr FROM TransferFeeRule tfr WHERE tfr.isActive = true " +
           "AND tfr.minAmount <= :amount " +
           "AND (tfr.maxAmount = 0 OR tfr.maxAmount >= :amount) " +
           "AND (tfr.bankType = 'both' OR tfr.bankType = :transferType) " +
           "AND (tfr.kycLevel IS NULL OR tfr.kycLevel = :kycLevel) " +
           "ORDER BY tfr.priority DESC")
    List<TransferFeeRule> findApplicableRules(@Param("amount") BigDecimal amount, 
                                             @Param("transferType") String transferType, 
                                             @Param("kycLevel") String kycLevel);
    
    List<TransferFeeRule> findByBankTypeAndIsActiveTrue(String bankType);
    
    List<TransferFeeRule> findByKycLevelAndIsActiveTrue(String kycLevel);
}
