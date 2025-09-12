package com.xypay.xypay.controller;

import com.xypay.xypay.service.ChargeControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/api")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERUSER')")
public class ChargeControlApiController {

    @Autowired
    private ChargeControlService chargeControlService;

    /**
     * Get current charge control settings
     */
    @GetMapping("/charge-controls")
    public ResponseEntity<Map<String, Object>> getChargeControls() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", chargeControlService.getCurrentChargeControls());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to retrieve charge controls: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Update charge control settings
     */
    @PostMapping("/charge-controls")
    public ResponseEntity<Map<String, Object>> updateChargeControls(@RequestBody Map<String, Object> request) {
        try {
            Boolean levyActive = (Boolean) request.get("levyActive");
            Boolean vatActive = (Boolean) request.get("vatActive");
            Boolean feeActive = (Boolean) request.get("feeActive");

            chargeControlService.updateChargeControls(levyActive, vatActive, feeActive);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Charge controls updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update charge controls: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get current charge rates
     */
    @GetMapping("/charge-rates")
    public ResponseEntity<Map<String, Object>> getChargeRates() {
        try {
            Map<String, Object> response = new HashMap<>();
            Map<String, Object> rates = new HashMap<>();
            
            rates.put("vatRate", chargeControlService.getCurrentVATRate());
            rates.put("transferFee", chargeControlService.getCurrentTransferFee());
            rates.put("cbnLevy", chargeControlService.getCurrentCBNLevy());
            rates.put("tieredFees", chargeControlService.getTieredFees());
            
            response.put("success", true);
            response.put("data", rates);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to retrieve charge rates: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Update charge rates
     */
    @PostMapping("/charge-rates")
    public ResponseEntity<Map<String, Object>> updateChargeRates(@RequestBody Map<String, Object> request) {
        try {
            if (request.containsKey("vatRate")) {
                BigDecimal vatRate = new BigDecimal(request.get("vatRate").toString());
                chargeControlService.updateVATRate(vatRate);
            }

            if (request.containsKey("transferFee")) {
                BigDecimal transferFee = new BigDecimal(request.get("transferFee").toString());
                chargeControlService.updateTransferFee(transferFee);
            }

            if (request.containsKey("cbnLevy")) {
                BigDecimal cbnLevy = new BigDecimal(request.get("cbnLevy").toString());
                chargeControlService.updateCBNLevy(cbnLevy);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Charge rates updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update charge rates: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Update tiered fees
     */
    @PostMapping("/tiered-fees")
    public ResponseEntity<Map<String, Object>> updateTieredFees(@RequestBody Map<String, Object> request) {
        try {
            BigDecimal tier1 = new BigDecimal(request.get("tier1").toString());
            BigDecimal tier2 = new BigDecimal(request.get("tier2").toString());
            BigDecimal tier3 = new BigDecimal(request.get("tier3").toString());

            chargeControlService.updateTieredFees(tier1, tier2, tier3);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tiered fees updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update tiered fees: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Calculate charges for a transaction amount
     */
    @PostMapping("/calculate-charges")
    public ResponseEntity<Map<String, Object>> calculateCharges(@RequestBody Map<String, Object> request) {
        try {
            BigDecimal transactionAmount = new BigDecimal(request.get("amount").toString());
            Map<String, BigDecimal> charges = chargeControlService.calculateCharges(transactionAmount);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", charges);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to calculate charges: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get charge analytics
     */
    @GetMapping("/charge-analytics")
    public ResponseEntity<Map<String, Object>> getChargeAnalytics() {
        try {
            Map<String, Object> analytics = chargeControlService.getChargeAnalytics();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", analytics);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to retrieve analytics: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get all charge settings
     */
    @GetMapping("/charge-settings")
    public ResponseEntity<Map<String, Object>> getAllChargeSettings() {
        try {
            Map<String, Object> settings = chargeControlService.getAllChargeSettings();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", settings);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to retrieve charge settings: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
