package com.xypay.xypay.repository;

import com.xypay.xypay.domain.ReconciliationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ReconciliationItemRepository extends JpaRepository<ReconciliationItem, Long> {
}