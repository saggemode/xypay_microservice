package com.xypay.xypay.repository;

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
public interface IslamicBankingContractRepository extends JpaRepository<IslamicBankingContract, UUID> {
    
    List<IslamicBankingContract> findByCustomerId(UUID customerId);
    
    List<IslamicBankingContract> findByBankId(UUID bankId);
    
    List<IslamicBankingContract> findByBankIdAndContractStatus(UUID bankId, IslamicBankingContract.ContractStatus status);
    
    List<IslamicBankingContract> findByContractStatus(IslamicBankingContract.ContractStatus status);
    
    List<IslamicBankingContract> findByIslamicProductId(UUID productId);
    
    List<IslamicBankingContract> findByRiskRating(IslamicBankingContract.RiskRating riskRating);
    
    List<IslamicBankingContract> findByIfrsStage(IslamicBankingContract.IfrsStage ifrsStage);
    
    List<IslamicBankingContract> findByShariaBoardApprovedTrue();
    
    List<IslamicBankingContract> findByMaturityDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<IslamicBankingContract> findByMaturityDateBeforeAndContractStatusIn(LocalDateTime date, 
                                                                            List<IslamicBankingContract.ContractStatus> statuses);
    
    List<IslamicBankingContract> findByNextPaymentDateBefore(LocalDateTime date);
    
    List<IslamicBankingContract> findByDaysPastDueGreaterThan(Integer days);
    
    @Query("SELECT ibc FROM IslamicBankingContract ibc WHERE ibc.principalAmount BETWEEN :minAmount AND :maxAmount")
    List<IslamicBankingContract> findByPrincipalAmountRange(@Param("minAmount") BigDecimal minAmount, 
                                                           @Param("maxAmount") BigDecimal maxAmount);
    
    @Query("SELECT COUNT(ibc) FROM IslamicBankingContract ibc WHERE ibc.contractStatus = :status")
    Long countByContractStatus(@Param("status") IslamicBankingContract.ContractStatus status);
    
    @Query("SELECT SUM(ibc.principalAmount) FROM IslamicBankingContract ibc WHERE ibc.contractStatus = 'ACTIVE'")
    BigDecimal getTotalActiveFinancing();
    
    @Query("SELECT SUM(ibc.outstandingPrincipal) FROM IslamicBankingContract ibc WHERE ibc.contractStatus = 'ACTIVE'")
    BigDecimal getTotalOutstandingPrincipal();
    
    @Query("SELECT SUM(ibc.provisionAmount) FROM IslamicBankingContract ibc WHERE ibc.contractStatus = 'ACTIVE'")
    BigDecimal getTotalProvisions();
    
    @Query("SELECT ibc FROM IslamicBankingContract ibc WHERE ibc.approvalWorkflowId = :workflowId")
    IslamicBankingContract findByApprovalWorkflowId(@Param("workflowId") UUID workflowId);
}
