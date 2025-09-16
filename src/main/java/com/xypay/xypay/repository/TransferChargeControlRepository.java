package com.xypay.xypay.repository;

import com.xypay.xypay.domain.TransferChargeControl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransferChargeControlRepository extends JpaRepository<TransferChargeControl, UUID> {
    
    @Query("SELECT t FROM TransferChargeControl t ORDER BY t.updatedAt DESC")
    Optional<TransferChargeControl> findFirstByOrderByUpdatedAtDesc();
}