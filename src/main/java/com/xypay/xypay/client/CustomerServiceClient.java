package com.xypay.xypay.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "customer-service")
public interface CustomerServiceClient {
    
    @GetMapping("/api/customers")
    List<Map<String, Object>> getAllCustomers();
    
    @GetMapping("/api/customers/{id}")
    Map<String, Object> getCustomerById(@PathVariable("id") Long id);
    
    @PostMapping("/api/customers")
    Map<String, Object> createCustomer(@RequestBody Map<String, Object> customer);
    
    @PutMapping("/api/customers/{id}")
    Map<String, Object> updateCustomer(@PathVariable("id") Long id, @RequestBody Map<String, Object> customer);
    
    @GetMapping("/api/customers/{id}/kyc")
    Map<String, Object> getCustomerKYC(@PathVariable("id") Long id);
    
    @PostMapping("/api/customers/{id}/kyc")
    Map<String, Object> createCustomerKYC(@PathVariable("id") Long id, @RequestBody Map<String, Object> kycData);
}
