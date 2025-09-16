package com.xypay.xypay.repository;

import com.xypay.xypay.domain.SpendAndSaveSettings;
import com.xypay.xypay.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpendAndSaveSettingsRepository extends JpaRepository<SpendAndSaveSettings, UUID> {
    Optional<SpendAndSaveSettings> findByUser(User user);
}
