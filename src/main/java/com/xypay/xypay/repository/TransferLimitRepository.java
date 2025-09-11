package com.xypay.xypay.repository;

import com.xypay.xypay.domain.TransferLimit;
import com.xypay.xypay.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransferLimitRepository extends JpaRepository<TransferLimit, Long> {
    
    List<TransferLimit> findByUserOrderByCreatedAtDesc(User user);
    
    List<TransferLimit> findByUserAndIsActiveTrueOrderByCreatedAtDesc(User user);
    
    List<TransferLimit> findByLimitCategoryAndIsActiveTrue(TransferLimit.LimitCategory limitCategory);
    
    List<TransferLimit> findByLimitTypeAndIsActiveTrue(TransferLimit.LimitType limitType);
    
    List<TransferLimit> findByUserAndLimitCategoryAndIsActiveTrue(User user, TransferLimit.LimitCategory limitCategory);
    
    List<TransferLimit> findByAutoResetEnabledTrueAndIsActiveTrue();
    
    Optional<TransferLimit> findByUserAndLimitTypeAndLimitCategoryAndIsActiveTrue(
        User user, TransferLimit.LimitType limitType, TransferLimit.LimitCategory limitCategory);
    
    List<TransferLimit> findByCreatedByOrderByCreatedAtDesc(User createdBy);
    
    List<TransferLimit> findByIsActiveTrue();
    
    List<TransferLimit> findByIsActiveFalse();
}
