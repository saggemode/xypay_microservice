package com.xypay.analytics.repository;

import com.xypay.analytics.domain.CustomerSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerSegmentRepository extends JpaRepository<CustomerSegment, Long> {
    
    Optional<CustomerSegment> findByCustomerIdAndIsActiveTrue(Long customerId);
    
    List<CustomerSegment> findByCustomerIdOrderBySegmentDateDesc(Long customerId);
    
    List<CustomerSegment> findBySegmentType(String segmentType);
    
    List<CustomerSegment> findByIsActiveTrue();
    
    @Query("SELECT cs FROM CustomerSegment cs WHERE cs.customerValue >= :minValue AND cs.customerValue <= :maxValue")
    List<CustomerSegment> findByCustomerValueRange(@Param("minValue") BigDecimal minValue, 
                                                  @Param("maxValue") BigDecimal maxValue);
    
    @Query("SELECT cs FROM CustomerSegment cs WHERE cs.behaviorScore >= :minScore AND cs.behaviorScore <= :maxScore")
    List<CustomerSegment> findByBehaviorScoreRange(@Param("minScore") BigDecimal minScore, 
                                                  @Param("maxScore") BigDecimal maxScore);
    
    @Query("SELECT cs FROM CustomerSegment cs WHERE cs.segmentDate >= :startDate AND cs.segmentDate <= :endDate")
    List<CustomerSegment> findBySegmentDateRange(@Param("startDate") LocalDateTime startDate, 
                                                @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(cs) FROM CustomerSegment cs WHERE cs.segmentType = :segmentType")
    Long countBySegmentType(@Param("segmentType") String segmentType);
    
    @Query("SELECT AVG(cs.customerValue) FROM CustomerSegment cs WHERE cs.segmentType = :segmentType")
    BigDecimal getAverageCustomerValueBySegmentType(@Param("segmentType") String segmentType);
    
    @Query("SELECT cs FROM CustomerSegment cs WHERE cs.customerId IN :customerIds AND cs.isActive = true")
    List<CustomerSegment> findByCustomerIdsAndIsActiveTrue(@Param("customerIds") List<Long> customerIds);
}
