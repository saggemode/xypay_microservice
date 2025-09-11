package com.xypay.account.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service")
public interface CustomerServiceClient {
    
    @GetMapping("/api/customers/{customerId}")
    CustomerResponse getCustomer(@PathVariable Long customerId);
    
    @GetMapping("/api/customers/{customerId}/kyc")
    KYCResponse getCustomerKYC(@PathVariable Long customerId);
    
    class CustomerResponse {
        public Long id;
        public String firstName;
        public String lastName;
        public String email;
        public String phoneNumber;
        public Boolean isActive;
        public Boolean isVerified;
    }
    
    class KYCResponse {
        public Long id;
        public Long userId;
        public String kycLevel;
        public String status;
        public Boolean isVerified;
    }
}
