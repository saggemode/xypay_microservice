package com.xypay.xypay.controller;

import com.xypay.xypay.domain.Customer;
import com.xypay.xypay.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    @PostMapping("/onboard")
    public ResponseEntity<Customer> onboard(@RequestParam String name, @RequestParam String legalId, @RequestParam String kycStatus) {
        return ResponseEntity.ok(customerService.onboardCustomer(name, legalId, kycStatus));
    }

    @PostMapping("/{customerId}/verify-kyc")
    public ResponseEntity<String> verifyKYC(@PathVariable Long customerId, @RequestParam String documentType) {
        return ResponseEntity.ok(customerService.verifyKYC(customerId, documentType));
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<Customer> getCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(customerService.getCustomer(customerId));
    }
}