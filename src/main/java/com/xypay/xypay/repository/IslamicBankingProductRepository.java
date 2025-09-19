package com.xypay.xypay.repository;

import com.xypay.xypay.domain.IslamicBankingProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IslamicBankingProductRepository extends JpaRepository<IslamicBankingProduct, UUID> {
    
    Optional<IslamicBankingProduct> findByProductCode(String productCode);
    
    List<IslamicBankingProduct> findByBankId(UUID bankId);
    
    List<IslamicBankingProduct> findByBankIdAndIsActiveTrue(UUID bankId);
    
    List<IslamicBankingProduct> findByProductCategory(IslamicBankingProduct.ProductCategory productCategory);
    
    List<IslamicBankingProduct> findByIslamicStructure(IslamicBankingProduct.IslamicStructure islamicStructure);
    
    List<IslamicBankingProduct> findByRiskCategory(IslamicBankingProduct.RiskCategory riskCategory);
    
    List<IslamicBankingProduct> findByMinimumAmountLessThanEqual(BigDecimal amount);
    
    List<IslamicBankingProduct> findByIsActiveTrue();
    
    @Query("SELECT p FROM IslamicBankingProduct p WHERE p.bank.id = :bankId AND p.productName LIKE %:searchTerm%")
    List<IslamicBankingProduct> findByBankIdAndProductNameContaining(@Param("bankId") UUID bankId, @Param("searchTerm") String searchTerm);
    
    @Query("SELECT COUNT(p) FROM IslamicBankingProduct p WHERE p.bank.id = :bankId AND p.isActive = true")
    Long countActiveByBankId(@Param("bankId") UUID bankId);
    
    @Query("SELECT p FROM IslamicBankingProduct p WHERE p.bank.id = :bankId AND p.productCategory = :category AND p.shariaBoardApproved = true")
    List<IslamicBankingProduct> findByBankIdAndProductCategoryAndShariaCompliant(@Param("bankId") UUID bankId, @Param("category") IslamicBankingProduct.ProductCategory category);
}