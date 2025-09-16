package com.xypay.xypay.repository;

import com.xypay.xypay.domain.RealTimeProcessor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RealTimeProcessorRepository extends JpaRepository<RealTimeProcessor, UUID> {
    
    List<RealTimeProcessor> findByProcessorTypeAndIsActiveTrueOrderByCurrentLoadAsc(RealTimeProcessor.ProcessorType type);
    
    List<RealTimeProcessor> findByIsActiveTrue();
    
    List<RealTimeProcessor> findByStatus(RealTimeProcessor.ProcessorStatus status);
    
    List<RealTimeProcessor> findByCircuitState(RealTimeProcessor.CircuitState state);
    
    List<RealTimeProcessor> findBySupports24x7True();
    
    @Query("SELECT rtp FROM RealTimeProcessor rtp WHERE rtp.currentLoad < rtp.maxThroughputPerSecond * 0.8")
    List<RealTimeProcessor> findAvailableProcessors();
    
    @Query("SELECT rtp FROM RealTimeProcessor rtp WHERE rtp.lastHealthCheck < :threshold")
    List<RealTimeProcessor> findProcessorsNeedingHealthCheck(@Param("threshold") LocalDateTime threshold);
    
    @Query("SELECT rtp FROM RealTimeProcessor rtp WHERE rtp.errorRate > :threshold")
    List<RealTimeProcessor> findProcessorsWithHighErrorRate(@Param("threshold") String threshold);
    
    @Query("SELECT COUNT(rtp) FROM RealTimeProcessor rtp WHERE rtp.status = :status")
    Long countByStatus(@Param("status") RealTimeProcessor.ProcessorStatus status);
}
