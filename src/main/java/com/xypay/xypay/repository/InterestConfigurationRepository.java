package com.xypay.xypay.repository;

import com.xypay.xypay.config.InterestConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

import java.util.List;
import java.util.Optional;


@Repository
public interface InterestConfigurationRepository extends JpaRepository<InterestConfiguration, UUID> {
    Optional<InterestConfiguration> findByConfigurationName(String configurationName);
    List<InterestConfiguration> findByIsActiveTrue();
}