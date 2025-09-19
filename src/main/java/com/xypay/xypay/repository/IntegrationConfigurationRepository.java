package com.xypay.xypay.repository;

import com.xypay.xypay.domain.IntegrationConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

import java.util.List;
import java.util.Optional;


@Repository
public interface IntegrationConfigurationRepository extends JpaRepository<IntegrationConfiguration, UUID> {
    Optional<IntegrationConfiguration> findByIntegrationName(String integrationName);
    List<IntegrationConfiguration> findByIntegrationTypeAndIsActiveTrue(String integrationType);
    List<IntegrationConfiguration> findByIsActiveTrue();
}