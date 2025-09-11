package com.xypay.xypay.repository;

import com.xypay.xypay.domain.VATCharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface VATChargeRepository extends JpaRepository<VATCharge, Long> {
    
    @Query("SELECT v FROM VATCharge v WHERE v.active = true ORDER BY v.updatedAt DESC")
    Optional<VATCharge> findFirstByActiveTrueOrderByUpdatedAtDesc();
    
    List<VATCharge> findByActiveTrue();
}