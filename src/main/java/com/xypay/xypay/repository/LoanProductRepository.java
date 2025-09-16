package com.xypay.xypay.repository;

import com.xypay.xypay.domain.LoanProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoanProductRepository extends JpaRepository<LoanProduct, UUID> {
    
    Optional<LoanProduct> findByProductCode(String productCode);
    
    List<LoanProduct> findByIsActiveTrue();
    
    List<LoanProduct> findByLoanType(LoanProduct.LoanType loanType);
    
    List<LoanProduct> findByCurrencyCode(String currencyCode);
    
    List<LoanProduct> findByBankId(Long bankId);
    
    List<LoanProduct> findByShariaCompliant(Boolean shariaCompliant);
    
    List<LoanProduct> findByIslamicStructure(LoanProduct.IslamicStructure structure);
    
    @Query("SELECT lp FROM LoanProduct lp WHERE lp.minimumAmount <= :amount AND lp.maximumAmount >= :amount")
    List<LoanProduct> findByAmountRange(@Param("amount") BigDecimal amount);
    
    @Query("SELECT lp FROM LoanProduct lp WHERE lp.minimumTermMonths <= :term AND lp.maximumTermMonths >= :term")
    List<LoanProduct> findByTermRange(@Param("term") Integer term);
    
    List<LoanProduct> findByRiskCategory(LoanProduct.RiskCategory riskCategory);
    
    List<LoanProduct> findByBaselCompliantTrue();
    
    List<LoanProduct> findByIfrsCompliantTrue();
}
