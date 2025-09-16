package com.xypay.xypay.repository;

import com.xypay.xypay.domain.ApprovalMatrix;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApprovalMatrixRepository extends JpaRepository<ApprovalMatrix, UUID> {
    Optional<ApprovalMatrix> findByMatrixCode(String matrixCode);
    List<ApprovalMatrix> findByTransactionType(String transactionType);
    List<ApprovalMatrix> findByIsActive(Boolean isActive);
    List<ApprovalMatrix> findByApprovalLevel(Integer approvalLevel);
    
    @Query("SELECT am FROM ApprovalMatrix am WHERE " +
           "am.transactionType = :transactionType AND " +
           "am.isActive = true AND " +
           "(:amount BETWEEN am.amountFrom AND am.amountTo OR (am.amountFrom IS NULL AND am.amountTo IS NULL)) AND " +
           "(am.currency = :currency OR am.currency IS NULL) AND " +
           "(am.customerType = :customerType OR am.customerType IS NULL) AND " +
           "(am.productType = :productType OR am.productType IS NULL) AND " +
           "(am.branchCode = :branchCode OR am.branchCode IS NULL) AND " +
           "(am.effectiveFrom <= CURRENT_TIMESTAMP) AND " +
           "(am.effectiveTo IS NULL OR am.effectiveTo >= CURRENT_TIMESTAMP) " +
           "ORDER BY am.approvalLevel")
    List<ApprovalMatrix> findApplicableMatrices(
        @Param("transactionType") String transactionType,
        @Param("amount") BigDecimal amount,
        @Param("currency") String currency,
        @Param("customerType") String customerType,
        @Param("productType") String productType,
        @Param("branchCode") String branchCode
    );
}
