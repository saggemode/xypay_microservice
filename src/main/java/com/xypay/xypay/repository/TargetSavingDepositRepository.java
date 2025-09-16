package com.xypay.xypay.repository;

import com.xypay.xypay.domain.TargetSaving;
import com.xypay.xypay.domain.TargetSavingDeposit;
import com.xypay.xypay.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TargetSavingDepositRepository extends JpaRepository<TargetSavingDeposit, UUID> {
    List<TargetSavingDeposit> findByTargetSavingOrderByDepositDateDesc(TargetSaving targetSaving, int limit);
    List<TargetSavingDeposit> findByTargetSaving(TargetSaving targetSaving);
    Page<TargetSavingDeposit> findByTargetSavingOrderByDepositDateDesc(TargetSaving targetSaving, Pageable pageable);
    List<TargetSavingDeposit> findByTargetSavingUserOrderByDepositDateDesc(User user);
    Page<TargetSavingDeposit> findByTargetSavingUserOrderByDepositDateDesc(User user, Pageable pageable);
}
