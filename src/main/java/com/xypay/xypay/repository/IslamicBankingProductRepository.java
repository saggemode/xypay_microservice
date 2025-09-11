package com.xypay.xypay.repository;

import com.xypay.xypay.domain.IslamicBankingProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface IslamicBankingProductRepository extends JpaRepository<IslamicBankingProduct, Long> {
    
    Optional<IslamicBankingProduct> findByProductCode(String productCode);
    
    List<IslamicBankingProduct> findByBankId(Long bankId);
    
    List<IslamicBankingProduct> findByBankIdAndIsActiveTrue(Long bankId);
    
    List<IslamicBankingProduct> findByProductCategory(IslamicBankingProduct.ProductCategory productCategory);
    
    List<IslamicBankingProduct> findByIslamicStructure(IslamicBankingProduct.IslamicStructure islamicStructure);
    
    List<IslamicBankingProduct> findByShariaBoardApprovedTrue();
    
    List<IslamicBankingProduct> findByCurrencyCode(String currencyCode);
    
    List<IslamicBankingProduct> findByIsActiveTrue();
    
    @Query("SELECT ibp FROM IslamicBankingProduct ibp WHERE ibp.productName LIKE CONCAT('%', :name, '%')")
    List<IslamicBankingProduct> findByProductNameContaining(@Param("name") String name);
    
    @Query("SELECT ibp FROM IslamicBankingProduct ibp WHERE ibp.minimumAmount <= :amount AND ibp.maximumAmount >= :amount")
    List<IslamicBankingProduct> findByAmountRange(@Param("amount") BigDecimal amount);
    
    @Query("SELECT COUNT(ibp) FROM IslamicBankingProduct ibp WHERE ibp.isActive = true")
    Long countActiveProducts();
    
    @Query("SELECT ibp FROM IslamicBankingProduct ibp WHERE ibp.bank.id = :bankId AND ibp.productCategory = :category AND ibp.isActive = true")
    List<IslamicBankingProduct> findByBankIdAndCategoryAndActive(@Param("bankId") Long bankId, 
                                                               @Param("category") IslamicBankingProduct.ProductCategory category);
}