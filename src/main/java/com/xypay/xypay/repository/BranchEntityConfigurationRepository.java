package com.xypay.xypay.repository;

import com.xypay.xypay.config.BranchEntityConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

import java.util.List;
import java.util.Optional;


@Repository
public interface BranchEntityConfigurationRepository extends JpaRepository<BranchEntityConfiguration, UUID> {
    Optional<BranchEntityConfiguration> findByBranchCode(String branchCode);
    List<BranchEntityConfiguration> findByEntityTypeAndIsActiveTrue(String entityType);
    List<BranchEntityConfiguration> findByCountryCodeAndIsActiveTrue(String countryCode);
    List<BranchEntityConfiguration> findByIsActiveTrue();
}