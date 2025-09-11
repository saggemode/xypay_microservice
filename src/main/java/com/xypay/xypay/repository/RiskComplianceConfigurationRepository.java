package com.xypay.xypay.repository;

import com.xypay.xypay.config.RiskComplianceConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface RiskComplianceConfigurationRepository extends JpaRepository<RiskComplianceConfiguration, Long> {
    Optional<RiskComplianceConfiguration> findByConfigurationName(String configurationName);
    List<RiskComplianceConfiguration> findByConfigTypeAndIsActiveTrue(String configType);
    List<RiskComplianceConfiguration> findByIsActiveTrue();
}