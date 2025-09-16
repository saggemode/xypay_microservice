package com.xypay.xypay.repository;

import com.xypay.xypay.domain.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoanRepository extends JpaRepository<Loan, UUID> {
    
    List<Loan> findByCustomerId(Long customerId);
    
    List<Loan> findByStatus(Loan.LoanStatus status);
    
    List<Loan> findByStatusAndDaysPastDueGreaterThan(Loan.LoanStatus status, Integer daysPastDue);
    
    List<Loan> findByRiskRating(Loan.RiskRating riskRating);
    
    List<Loan> findByImpairmentStage(Loan.ImpairmentStage stage);
    
    List<Loan> findByBaselClassification(Loan.BaselClassification classification);
    
    List<Loan> findByLoanProductId(Long productId);
    
    List<Loan> findByBranchId(Long branchId);
    
    List<Loan> findByShariaCompliant(Boolean shariaCompliant);
    
    List<Loan> findByIslamicStructure(Loan.IslamicStructure structure);
    
    Optional<Loan> findByLoanNumber(String loanNumber);
    
    List<Loan> findByNextPaymentDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Loan> findByMaturityDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT l FROM Loan l WHERE l.totalOutstanding > :amount")
    List<Loan> findByTotalOutstandingGreaterThan(@Param("amount") BigDecimal amount);
    
    @Query("SELECT l FROM Loan l WHERE l.principalAmount BETWEEN :minAmount AND :maxAmount")
    List<Loan> findByPrincipalAmountBetween(@Param("minAmount") BigDecimal minAmount, 
                                          @Param("maxAmount") BigDecimal maxAmount);
    
    @Query("SELECT COUNT(l) FROM Loan l WHERE l.status = :status")
    Long countByStatus(@Param("status") Loan.LoanStatus status);
    
    @Query("SELECT SUM(l.principalAmount) FROM Loan l WHERE l.status = :status")
    BigDecimal sumPrincipalAmountByStatus(@Param("status") Loan.LoanStatus status);
    
    @Query("SELECT SUM(l.totalOutstanding) FROM Loan l WHERE l.status = 'ACTIVE'")
    BigDecimal getTotalOutstandingAmount();
    
    @Query("SELECT SUM(l.provisionAmount) FROM Loan l")
    BigDecimal getTotalProvisionAmount();
    
    @Query("SELECT l FROM Loan l WHERE l.approvalWorkflowId = :workflowId")
    Optional<Loan> findByApprovalWorkflowId(@Param("workflowId") Long workflowId);
}