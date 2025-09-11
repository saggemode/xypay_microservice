package com.xypay.xypay.admin;

import com.xypay.xypay.domain.RateConfiguration;
import com.xypay.xypay.service.RateConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/rate-configurator")
public class RateConfiguratorController {
    
    @Autowired
    private RateConfigurationService rateConfigurationService;
    
    /**
     * Create rate configuration
     */
    @PostMapping("/rate-config")
    public ResponseEntity<Map<String, Object>> createRateConfig(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = rateConfigurationService.createRateConfiguration(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get rate configuration by code
     */
    @GetMapping("/rate-config/{rateCode}")
    public ResponseEntity<RateConfiguration> getRateConfig(@PathVariable String rateCode) {
        RateConfiguration config = rateConfigurationService.getRateConfiguration(rateCode);
        return config != null ? ResponseEntity.ok(config) : ResponseEntity.notFound().build();
    }
    
    /**
     * Get all rate configurations
     */
    @GetMapping("/rate-config")
    public ResponseEntity<List<RateConfiguration>> getAllRateConfigs() {
        List<RateConfiguration> configs = rateConfigurationService.getAllRateConfigurations();
        return ResponseEntity.ok(configs);
    }
    
    /**
     * Calculate rate for given parameters
     */
    @PostMapping("/calculate-rate")
    public ResponseEntity<Map<String, Object>> calculateRate(@RequestBody Map<String, Object> parameters) {
        Map<String, Object> calculation = rateConfigurationService.calculateRate(parameters);
        return ResponseEntity.ok(calculation);
    }
    
    /**
     * Get effective rate for product and customer
     */
    @GetMapping("/effective-rate/{productType}/{customerSegment}")
    public ResponseEntity<Map<String, Object>> getEffectiveRate(@PathVariable String productType, @PathVariable String customerSegment) {
        Map<String, Object> rate = rateConfigurationService.getEffectiveRate(productType, customerSegment);
        return ResponseEntity.ok(rate);
    }
    
    /**
     * Update rate configuration
     */
    @PutMapping("/rate-config/{id}")
    public ResponseEntity<Map<String, Object>> updateRateConfig(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Map<String, Object> response = rateConfigurationService.updateRateConfiguration(id, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete rate configuration
     */
    @DeleteMapping("/rate-config/{id}")
    public ResponseEntity<Map<String, Object>> deleteRateConfig(@PathVariable Long id) {
        Map<String, Object> response = rateConfigurationService.deleteRateConfiguration(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Bulk update rates based on benchmark changes
     */
    @PostMapping("/bulk-update")
    public ResponseEntity<Map<String, Object>> bulkUpdateRates(@RequestBody Map<String, Object> updateData) {
        Map<String, Object> result = rateConfigurationService.bulkUpdateRates(updateData);
        return ResponseEntity.ok(result);
    }
}
