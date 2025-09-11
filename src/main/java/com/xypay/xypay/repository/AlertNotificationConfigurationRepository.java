package com.xypay.xypay.repository;

import com.xypay.xypay.domain.AlertNotificationConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface AlertNotificationConfigurationRepository extends JpaRepository<AlertNotificationConfiguration, Long> {
    Optional<AlertNotificationConfiguration> findByAlertName(String alertName);
    List<AlertNotificationConfiguration> findByAlertTypeAndIsActiveTrue(String alertType);
    List<AlertNotificationConfiguration> findByIsActiveTrue();
}