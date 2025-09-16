package com.xypay.xypay.repository;

import com.xypay.xypay.domain.XySaveAccount;
import com.xypay.xypay.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface XySaveAccountRepository extends JpaRepository<XySaveAccount, UUID> {
    Optional<XySaveAccount> findByUserId(UUID userId);
    Optional<XySaveAccount> findByUser(User user);
}