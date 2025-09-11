package com.xypay.xypay.controller;

import com.xypay.xypay.service.TransferFeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/transfer-fee")
public class TransferFeeController {
    
    @Autowired
    private TransferFeeService transferFeeService;
    
    /**
     * Calculate transfer fees
     * @param amount The transfer amount
     * @param transferType The type of transfer (intra/inter)
     * @return ResponseEntity with fee calculation results
     */
    @GetMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculateTransferFee(
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "intra") String transferType) {
        
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("amount", amount);
            response.put("transferType", transferType);
            
            BigDecimal[] fees = transferFeeService.calculateTransferFees(amount, transferType);
            
            response.put("fee", fees[0]);
            response.put("vat", fees[1]);
            response.put("levy", fees[2]);
            
            BigDecimal total = fees[0].add(fees[1]).add(fees[2]);
            response.put("totalCharges", total);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to calculate transfer fees: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Get current charge control settings
     * @return ResponseEntity with charge control settings
     */
    @GetMapping("/charge-control")
    public ResponseEntity<Map<String, Object>> getChargeControl() {
        Map<String, Object> response = new HashMap<>();
        try {
            var chargeControl = transferFeeService.getChargeControl();
            response.put("levyActive", chargeControl.getLevyActive());
            response.put("vatActive", chargeControl.getVatActive());
            response.put("feeActive", chargeControl.getFeeActive());
            response.put("updatedAt", chargeControl.getUpdatedAt());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Failed to retrieve charge control settings: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get current VAT rate
     * @return ResponseEntity with VAT rate
     */
    @GetMapping("/vat-rate")
    public ResponseEntity<Map<String, Object>> getVatRate() {
        Map<String, Object> response = new HashMap<>();
        try {
            BigDecimal vatRate = transferFeeService.getActiveVatRate();
            response.put("vatRate", vatRate);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Failed to retrieve VAT rate: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}