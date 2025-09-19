package com.xypay.xypay.repository;

import com.xypay.xypay.config.ChargeConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

import java.util.List;
import java.util.Optional;


@Repository
public interface ChargeConfigurationRepository extends JpaRepository<ChargeConfiguration, UUID> {
    Optional<ChargeConfiguration> findByChargeName(String chargeName);
    List<ChargeConfiguration> findByIsActiveTrue();
    List<ChargeConfiguration> findByChargeTypeAndIsActiveTrue(String chargeType);
}