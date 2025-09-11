package com.xypay.xypay.repository;

import com.xypay.xypay.domain.GLAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface GLAccountRepository extends JpaRepository<GLAccount, Long> {
}