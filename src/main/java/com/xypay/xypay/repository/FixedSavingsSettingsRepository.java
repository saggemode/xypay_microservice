package com.xypay.xypay.repository;

import com.xypay.xypay.domain.FixedSavingsSettings;
import com.xypay.xypay.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FixedSavingsSettingsRepository extends JpaRepository<FixedSavingsSettings, UUID> {
    
    List<FixedSavingsSettings> findByUser(User user);
}