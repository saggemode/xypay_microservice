package com.xypay.xypay.repository;

import com.xypay.xypay.domain.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    List<Branch> findByIsActiveTrue();
    List<Branch> findByBankId(Long bankId);
    List<Branch> findByBankIdAndIsActiveTrue(Long bankId);
    Branch findByCode(String code);
}
