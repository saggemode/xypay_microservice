package com.xypay.xypay.repository;

import com.xypay.xypay.domain.ReportExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ReportExecutionRepository extends JpaRepository<ReportExecution, UUID> {
    
    List<ReportExecution> findByReportIdOrderByCreatedAtDesc(UUID reportId);
    
    List<ReportExecution> findByExecutionStatus(String status);
    
    List<ReportExecution> findByExecutedBy(UUID userId);
    
    @Query("SELECT re FROM ReportExecution re WHERE re.executionStatus = 'RUNNING' AND re.startedAt < :cutoffTime")
    List<ReportExecution> findStuckExecutions(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    @Query("SELECT re FROM ReportExecution re WHERE re.createdAt BETWEEN :startDate AND :endDate ORDER BY re.createdAt DESC")
    List<ReportExecution> findExecutionsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(re) FROM ReportExecution re WHERE re.report.id = :reportId AND re.executionStatus = 'COMPLETED'")
    Long countSuccessfulExecutions(@Param("reportId") UUID reportId);
}
