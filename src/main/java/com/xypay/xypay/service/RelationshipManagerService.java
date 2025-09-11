package com.xypay.xypay.service;

import com.xypay.xypay.domain.Customer;
import com.xypay.xypay.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RelationshipManagerService {
    @Autowired
    private CustomerRepository customerRepository;

    public List<Customer> getPortfolio() {
        return customerRepository.findAll();
    }

    public String crmTools() {
        return "CRM tools accessed.";
    }

    public String productSales() {
        return "Product sales tools accessed.";
    }

    public String transactionInitiation() {
        return "Transaction initiation tools accessed.";
    }
}
