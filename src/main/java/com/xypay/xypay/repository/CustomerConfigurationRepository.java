package com.xypay.xypay.repository;

import com.xypay.xypay.config.CustomerConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

import java.util.Optional;


@Repository
public interface CustomerConfigurationRepository extends JpaRepository<CustomerConfiguration, UUID> {
    Optional<CustomerConfiguration> findByCustomerType(String customerType);
}