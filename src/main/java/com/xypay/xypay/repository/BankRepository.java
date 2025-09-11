package com.xypay.xypay.repository;

import com.xypay.xypay.domain.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankRepository extends JpaRepository<Bank, Long> {
    
    Optional<Bank> findByCode(String code);
    
    Optional<Bank> findBySwiftCode(String swiftCode);
    
    List<Bank> findByIsActiveTrue();
    
    List<Bank> findByBankType(Bank.BankType bankType);
    
    List<Bank> findByCountryCode(String countryCode);
    
    List<Bank> findByBaselCompliantTrue();
    
    List<Bank> findByIfrsCompliantTrue();
    
    List<Bank> findByIslamicBankingEnabledTrue();
    
    @Query("SELECT b FROM Bank b WHERE b.name LIKE %:name%")
    List<Bank> findByNameContaining(@Param("name") String name);
    
    @Query("SELECT COUNT(b) FROM Bank b WHERE b.isActive = true")
    Long countActiveBanks();
}
