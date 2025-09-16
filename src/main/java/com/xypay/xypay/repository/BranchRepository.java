package com.xypay.xypay.repository;

import com.xypay.xypay.domain.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface BranchRepository extends JpaRepository<Branch, UUID> {
    List<Branch> findByIsActiveTrue();
    List<Branch> findByBankId(UUID bankId);
    List<Branch> findByBankIdAndIsActiveTrue(UUID bankId);
    Branch findByCode(String code);
}
