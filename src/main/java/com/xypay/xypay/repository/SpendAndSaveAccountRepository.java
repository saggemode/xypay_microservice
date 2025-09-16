package com.xypay.xypay.repository;

import com.xypay.xypay.domain.SpendAndSaveAccount;
import com.xypay.xypay.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpendAndSaveAccountRepository extends JpaRepository<SpendAndSaveAccount, UUID> {
    Optional<SpendAndSaveAccount> findByUser(User user);
    Optional<SpendAndSaveAccount> findByAccountNumber(String accountNumber);
    boolean existsByAccountNumber(String accountNumber);
    List<SpendAndSaveAccount> findByIsActiveTrue();
    long countByIsActiveTrue();
}