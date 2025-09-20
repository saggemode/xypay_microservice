package com.xypay.account.repository;

import com.xypay.account.domain.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankRepository extends JpaRepository<Bank, Long> {
    
    Optional<Bank> findByCode(String code);
    
    List<Bank> findByActive(Boolean active);
    
    @Query("SELECT b FROM Bank b WHERE b.code = :code AND b.active = true")
    Optional<Bank> findActiveByCode(@Param("code") String code);
    
    @Query("SELECT b FROM Bank b WHERE b.name LIKE %:name% AND b.active = true")
    List<Bank> findActiveByNameContaining(@Param("name") String name);
}
