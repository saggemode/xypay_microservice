package com.xypay.xypay.repository;

import com.xypay.xypay.config.LoanProductConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface LoanProductConfigurationRepository extends JpaRepository<LoanProductConfiguration, Long> {
    Optional<LoanProductConfiguration> findByProductName(String productName);
    List<LoanProductConfiguration> findByIsActiveTrue();
}