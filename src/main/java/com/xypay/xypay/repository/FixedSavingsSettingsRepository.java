package com.xypay.xypay.repository;

import com.xypay.xypay.domain.FixedSavingsSettings;
import com.xypay.xypay.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface FixedSavingsSettingsRepository extends JpaRepository<FixedSavingsSettings, Long> {
    FixedSavingsSettings findByUser(User user);
}