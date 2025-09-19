package com.xypay.xypay.repository;

import com.xypay.xypay.config.ReportingConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

import java.util.List;
import java.util.Optional;


@Repository
public interface ReportingConfigurationRepository extends JpaRepository<ReportingConfiguration, UUID> {
    Optional<ReportingConfiguration> findByReportName(String reportName);
    List<ReportingConfiguration> findByReportTypeAndIsActiveTrue(String reportType);
    List<ReportingConfiguration> findByFrequencyAndIsActiveTrue(String frequency);
    List<ReportingConfiguration> findByIsActiveTrue();
}