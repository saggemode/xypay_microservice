package com.xypay.xypay.controller;

import com.xypay.xypay.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test/validation")
public class ValidationTestController {

    /**
     * Test validation for PaymentIntentDTO
     */
    @PostMapping("/payment-intent")
    public ResponseEntity<Map<String, Object>> testPaymentIntentValidation(
            @Valid @RequestBody PaymentIntentDTO paymentIntentDTO) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "PaymentIntent validation passed");
        response.put("data", paymentIntentDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Test validation for PaymentIntentCreateDTO
     */
    @PostMapping("/payment-intent-create")
    public ResponseEntity<Map<String, Object>> testPaymentIntentCreateValidation(
            @Valid @RequestBody PaymentIntentCreateDTO paymentIntentCreateDTO) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "PaymentIntentCreate validation passed");
        response.put("data", paymentIntentCreateDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Test validation for PayoutRequestDTO
     */
    @PostMapping("/payout-request")
    public ResponseEntity<Map<String, Object>> testPayoutRequestValidation(
            @Valid @RequestBody PayoutRequestDTO payoutRequestDTO) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "PayoutRequest validation passed");
        response.put("data", payoutRequestDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Test validation for MerchantSettlementAccountDTO
     */
    @PostMapping("/merchant-settlement-account")
    public ResponseEntity<Map<String, Object>> testMerchantSettlementAccountValidation(
            @Valid @RequestBody MerchantSettlementAccountDTO merchantSettlementAccountDTO) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "MerchantSettlementAccount validation passed");
        response.put("data", merchantSettlementAccountDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Test validation for VirtualCardDTO
     */
    @PostMapping("/virtual-card")
    public ResponseEntity<Map<String, Object>> testVirtualCardValidation(
            @Valid @RequestBody VirtualCardDTO virtualCardDTO) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "VirtualCard validation passed");
        response.put("data", virtualCardDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Test validation for BankDTO
     */
    @PostMapping("/bank")
    public ResponseEntity<Map<String, Object>> testBankValidation(
            @Valid @RequestBody BankDTO bankDTO) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Bank validation passed");
        response.put("data", bankDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Test validation for LargeTransactionShieldSettingsDTO
     */
    @PostMapping("/large-transaction-shield")
    public ResponseEntity<Map<String, Object>> testLargeTransactionShieldValidation(
            @Valid @RequestBody LargeTransactionShieldSettingsDTO settingsDTO) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "LargeTransactionShieldSettings validation passed");
        response.put("data", settingsDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Test validation for LocationGuardSettingsDTO
     */
    @PostMapping("/location-guard")
    public ResponseEntity<Map<String, Object>> testLocationGuardValidation(
            @Valid @RequestBody LocationGuardSettingsDTO settingsDTO) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "LocationGuardSettings validation passed");
        response.put("data", settingsDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Test validation for NightGuardSettingsDTO
     */
    @PostMapping("/night-guard")
    public ResponseEntity<Map<String, Object>> testNightGuardValidation(
            @Valid @RequestBody NightGuardSettingsDTO settingsDTO) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "NightGuardSettings validation passed");
        response.put("data", settingsDTO);
        return ResponseEntity.ok(response);
    }
}
