package com.xypay.xypay.repository;

import com.xypay.xypay.domain.IslamicProfitDistribution;
import com.xypay.xypay.domain.IslamicBankingContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface IslamicProfitDistributionRepository extends JpaRepository<IslamicProfitDistribution, UUID> {
    
    List<IslamicProfitDistribution> findByIslamicContract(IslamicBankingContract islamicContract);
    
    List<IslamicProfitDistribution> findByDistributionStatus(IslamicProfitDistribution.DistributionStatus status);
    
    List<IslamicProfitDistribution> findByDistributionDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<IslamicProfitDistribution> findByPeriodStartDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<IslamicProfitDistribution> findByShariaComplianceVerifiedTrue();
    
    @Query("SELECT ipd FROM IslamicProfitDistribution ipd WHERE ipd.netProfit BETWEEN :minProfit AND :maxProfit")
    List<IslamicProfitDistribution> findByNetProfitRange(@Param("minProfit") BigDecimal minProfit, 
                                                        @Param("maxProfit") BigDecimal maxProfit);
    
    @Query("SELECT SUM(ipd.customerProfitAmount) FROM IslamicProfitDistribution ipd WHERE ipd.distributionStatus = 'PAID'")
    BigDecimal getTotalCustomerProfitPaid();
    
    @Query("SELECT SUM(ipd.bankProfitAmount) FROM IslamicProfitDistribution ipd WHERE ipd.distributionStatus = 'PAID'")
    BigDecimal getTotalBankProfitEarned();
    
    @Query("SELECT COUNT(ipd) FROM IslamicProfitDistribution ipd WHERE ipd.distributionStatus = :status")
    Long countByDistributionStatus(@Param("status") IslamicProfitDistribution.DistributionStatus status);
}
