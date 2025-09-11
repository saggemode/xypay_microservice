package com.xypay.xypay.repository;

import com.xypay.xypay.domain.ScreenConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScreenConfigurationRepository extends JpaRepository<ScreenConfiguration, Long> {
    Optional<ScreenConfiguration> findByScreenCode(String screenCode);
    List<ScreenConfiguration> findByScreenType(String screenType);
    List<ScreenConfiguration> findByIsActive(Boolean isActive);
    List<ScreenConfiguration> findByApprovalStatus(String approvalStatus);
}
