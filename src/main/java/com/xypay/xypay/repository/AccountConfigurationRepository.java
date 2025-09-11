package com.xypay.xypay.repository;

import com.xypay.xypay.config.AccountConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface AccountConfigurationRepository extends JpaRepository<AccountConfiguration, Long> {
    Optional<AccountConfiguration> findByAccountType(String accountType);
    List<AccountConfiguration> findByIsActiveTrue();
}