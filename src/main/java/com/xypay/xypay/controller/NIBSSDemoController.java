package com.xypay.xypay.controller;

import com.xypay.xypay.dto.ApiResponse;
import com.xypay.xypay.service.NIBSSClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/nibss-demo")
public class NIBSSDemoController {
    
    @Autowired
    private NIBSSClient nibssClient;
    
    /**
     * Demo endpoint showing various NIBSS client functionalities
     */
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> nibssDemo() {
        Map<String, Object> demoResults = new HashMap<>();
        
        // Test account validation
        List<Map<String, Object>> validationTests = new ArrayList<>();
        String[] testAccounts = {"0012345678", "0187654321", "0555555555", "12345"};
        
        for (String account : testAccounts) {
            Map<String, Object> validation = nibssClient.validateAccountNumber(account);
            Map<String, Object> testResult = new HashMap<>();
            testResult.put("accountNumber", account);
            testResult.put("result", validation);
            validationTests.add(testResult);
        }
        demoResults.put("accountValidations", validationTests);
        
        // Test interbank transfer
        Map<String, Object> transfer = nibssClient.sendInterbankTransfer(
                "0012345678", "058", "0187654321", 50000.0, "Test transfer");
        demoResults.put("sampleTransfer", transfer);
        
        // Test bill payment
        Map<String, Object> billPayment = nibssClient.payBill(
                "0012345678", "BIL001", 5000.0, null, "Electricity bill");
        demoResults.put("sampleBillPayment", billPayment);
        
        // Test BVN verification
        Map<String, Object> bvnVerification = nibssClient.verifyBVN("12345678901");
        demoResults.put("sampleBVNVerification", bvnVerification);
        
        // Test direct debit setup
        Map<String, Object> directDebit = nibssClient.setupDirectDebit(
                "0012345678", 10000.0, null);
        demoResults.put("sampleDirectDebitSetup", directDebit);
        
        return ResponseEntity.ok(ApiResponse.success(demoResults, "NIBSS demo completed successfully"));
    }
}