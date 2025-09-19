package com.xypay.xypay.repository;

import com.xypay.xypay.config.TransactionRuleConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

import java.util.List;
import java.util.Optional;


@Repository
public interface TransactionRuleConfigurationRepository extends JpaRepository<TransactionRuleConfiguration, UUID> {
    Optional<TransactionRuleConfiguration> findByRuleName(String ruleName);
    List<TransactionRuleConfiguration> findByIsActiveTrue();
    List<TransactionRuleConfiguration> findByRuleTypeAndIsActiveTrue(String ruleType);
}