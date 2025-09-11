package com.xypay.xypay.repository;

import com.xypay.xypay.domain.XySaveAccount;
import com.xypay.xypay.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface XySaveAccountRepository extends JpaRepository<XySaveAccount, Long> {
    Optional<XySaveAccount> findByUserId(Long userId);
    Optional<XySaveAccount> findByUser(User user);
}