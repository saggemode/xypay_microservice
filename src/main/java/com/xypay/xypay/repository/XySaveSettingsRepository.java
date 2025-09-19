package com.xypay.xypay.repository;

import com.xypay.xypay.domain.XySaveSettings;
import com.xypay.xypay.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface XySaveSettingsRepository extends JpaRepository<XySaveSettings, UUID> {
    
    Optional<XySaveSettings> findByUser(User user);
    
    Optional<XySaveSettings> findByUserId(UUID userId);
}
