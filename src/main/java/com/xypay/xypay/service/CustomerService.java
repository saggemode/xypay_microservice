package com.xypay.xypay.service;

import com.xypay.xypay.domain.Customer;
import com.xypay.xypay.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private TextEncryptor textEncryptor;
    @Autowired
    private AuditTrailService auditTrailService;
    @Autowired(required = false)
    private KafkaEventService kafkaEventService;

    public Customer createCustomer(String name, String legalId, String kycStatus) {
        Customer customer = new Customer();
        customer.setName(name);
        customer.setLegalId(textEncryptor.encrypt(legalId));
        customer.setKycStatus(kycStatus);
        customer.setCreatedAt(LocalDateTime.now());
        return customerRepository.save(customer);
    }
    public Customer getCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        customer.setLegalId(textEncryptor.decrypt(customer.getLegalId()));
        return customer;
    }
    public Customer onboardCustomer(String name, String legalId, String kycStatus) {
        Customer c = createCustomer(name, legalId, kycStatus);
        auditTrailService.logEvent("CUSTOMER_ONBOARD", "Onboarded customer: " + c.getId());
        if (kafkaEventService != null) {
            kafkaEventService.publishEvent("customers", String.valueOf(c.getId()), "Customer onboarded: " + c.getId());
        }
        return c;
    }
    public String verifyKYC(Long customerId, String documentType) {
        Customer customer = getCustomer(customerId);
        customer.setKycStatus("VERIFIED:" + documentType);
        customerRepository.save(customer);
        auditTrailService.logEvent("KYC_VERIFIED", "KYC verified for customer: " + customerId);
        if (kafkaEventService != null) {
            kafkaEventService.publishEvent("customers", String.valueOf(customerId), "KYC verified: " + customerId);
        }
        return "KYC verified for customer " + customerId + ".";
    }
}