package com.xypay.xypay.repository;

import com.xypay.xypay.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;



@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
}