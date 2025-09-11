package com.xypay.xypay.repository;

import com.xypay.xypay.domain.User;
import com.xypay.xypay.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    List<Wallet> findByUser(User user);
    Optional<Wallet> findByAccountNumber(String accountNumber);
    Optional<Wallet> findByAccountNumberOrAlternativeAccountNumber(String accountNumber, String alternativeAccountNumber);
    
    @Query("SELECT w FROM Wallet w JOIN FETCH w.user ORDER BY w.createdAt DESC")
    List<Wallet> findAllWithUser();
    
    @Query("SELECT w FROM Wallet w JOIN FETCH w.user WHERE w.id = :id")
    Optional<Wallet> findByIdWithUser(Long id);
}