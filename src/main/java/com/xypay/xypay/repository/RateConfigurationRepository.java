package com.xypay.xypay.repository;

import com.xypay.xypay.domain.RateConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RateConfigurationRepository extends JpaRepository<RateConfiguration, UUID> {
    Optional<RateConfiguration> findByRateCode(String rateCode);
    List<RateConfiguration> findByRateType(String rateType);
    List<RateConfiguration> findByProductType(String productType);
    List<RateConfiguration> findByCustomerSegment(String customerSegment);
    List<RateConfiguration> findByIsActive(Boolean isActive);
    List<RateConfiguration> findByBenchmarkRate(String benchmarkRate);
    List<RateConfiguration> findByProductTypeAndCustomerSegmentAndIsActive(String productType, String customerSegment, Boolean isActive);
    
    @Query("SELECT rc FROM RateConfiguration rc WHERE " +
           "rc.isActive = true AND " +
           "(rc.productType = :productType OR rc.productType IS NULL) AND " +
           "(rc.customerSegment = :customerSegment OR rc.customerSegment IS NULL) AND " +
           "(rc.rateType = :rateType OR rc.rateType IS NULL) AND " +
           "(rc.effectiveFrom <= CURRENT_TIMESTAMP) AND " +
           "(rc.effectiveTo IS NULL OR rc.effectiveTo >= CURRENT_TIMESTAMP) " +
           "ORDER BY rc.productType DESC, rc.customerSegment DESC, rc.rateType DESC")
    List<RateConfiguration> findApplicableRates(
        @Param("productType") String productType,
        @Param("customerSegment") String customerSegment,
        @Param("rateType") String rateType
    );
}
