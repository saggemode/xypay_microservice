package com.xypay.xypay.repository;

import com.xypay.xypay.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
    
    List<Report> findByReportTypeAndIsActiveTrue(String reportType);
    
    List<Report> findByReportCategoryAndIsActiveTrue(String reportCategory);
    
    List<Report> findByIsScheduledTrueAndIsActiveTrue();
    
    Optional<Report> findByReportNameAndIsActiveTrue(String reportName);
    
    List<Report> findByCreatedBy(Long userId);
    
    @Query("SELECT r FROM Report r WHERE r.isActive = true ORDER BY r.reportType, r.reportName")
    List<Report> findAllActiveReports();
    
    @Query("SELECT r FROM Report r WHERE r.reportType = :type AND r.reportCategory = :category AND r.isActive = true")
    List<Report> findByTypeAndCategory(@Param("type") String reportType, @Param("category") String reportCategory);
}
