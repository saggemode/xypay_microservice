package com.xypay.xypay.repository;

import com.xypay.xypay.domain.TargetSaving;
import com.xypay.xypay.domain.TargetSavingWithdrawal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TargetSavingWithdrawalRepository extends JpaRepository<TargetSavingWithdrawal, UUID> {
    List<TargetSavingWithdrawal> findByTargetSavingOrderByWithdrawalDateDesc(TargetSaving targetSaving);
    List<TargetSavingWithdrawal> findByTargetSaving(TargetSaving targetSaving);
}
