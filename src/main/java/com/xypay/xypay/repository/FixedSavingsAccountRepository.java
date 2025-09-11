package com.xypay.xypay.repository;

import com.xypay.xypay.domain.FixedSavingsAccount;
import com.xypay.xypay.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FixedSavingsAccountRepository extends JpaRepository<FixedSavingsAccount, Long> {
    List<FixedSavingsAccount> findByUser(User user);
    List<FixedSavingsAccount> findByUserAndIsActiveTrue(User user);
    List<FixedSavingsAccount> findByUserAndIsMaturedTrueAndIsPaidOutFalse(User user);
    FixedSavingsAccount findByIdAndUser(Long id, User user);
}