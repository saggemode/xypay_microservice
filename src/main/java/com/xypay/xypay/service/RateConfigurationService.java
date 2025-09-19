package com.xypay.xypay.service;

import com.xypay.xypay.domain.RateConfiguration;
import com.xypay.xypay.repository.RateConfigurationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class RateConfigurationService {
    
    @Autowired
    private RateConfigurationRepository rateConfigurationRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public Map<String, Object> createRateConfiguration(Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            RateConfiguration config = new RateConfiguration();
            config.setRateCode((String) request.get("rateCode"));
            config.setRateName((String) request.get("rateName"));
            config.setRateType((String) request.get("rateType"));
            config.setProductType((String) request.get("productType"));
            config.setCustomerSegment((String) request.get("customerSegment"));
            
            if (request.get("baseRate") != null) {
                config.setBaseRate(new BigDecimal(request.get("baseRate").toString()));
            }
            if (request.get("spread") != null) {
                config.setSpread(new BigDecimal(request.get("spread").toString()));
            }
            if (request.get("minimumRate") != null) {
                config.setMinimumRate(new BigDecimal(request.get("minimumRate").toString()));
            }
            if (request.get("maximumRate") != null) {
                config.setMaximumRate(new BigDecimal(request.get("maximumRate").toString()));
            }
            
            config.setCalculationMethod((String) request.get("calculationMethod"));
            config.setCompoundingFrequency((String) request.get("compoundingFrequency"));
            config.setRateBasis((String) request.get("rateBasis"));
            config.setTierStructure(objectMapper.writeValueAsString(request.get("tierStructure")));
            config.setPricingFormula((String) request.get("pricingFormula"));
            config.setBenchmarkRate((String) request.get("benchmarkRate"));
            config.setReviewFrequency((String) request.get("reviewFrequency"));
            config.setAutoAdjustment((Boolean) request.getOrDefault("autoAdjustment", false));
            
            if (request.get("adjustmentThreshold") != null) {
                config.setAdjustmentThreshold(new BigDecimal(request.get("adjustmentThreshold").toString()));
            }
            
            config.setEffectiveFrom(LocalDateTime.now());
            
            config = rateConfigurationRepository.save(config);
            
            response.put("success", true);
            response.put("message", "Rate configuration created successfully");
            response.put("rateId", config.getId());
            response.put("rateCode", config.getRateCode());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to create rate configuration: " + e.getMessage());
        }
        
        return response;
    }
    
    public RateConfiguration getRateConfiguration(String rateCode) {
        return rateConfigurationRepository.findByRateCode(rateCode).orElse(null);
    }
    
    public List<RateConfiguration> getAllRateConfigurations() {
        return rateConfigurationRepository.findAll();
    }
    
    public Map<String, Object> getRateConfigurationById(UUID id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<RateConfiguration> configOpt = rateConfigurationRepository.findById(id);
            if (configOpt.isPresent()) {
                response.put("success", true);
                response.put("data", configOpt.get());
            } else {
                response.put("success", false);
                response.put("message", "Rate configuration not found");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get rate configuration: " + e.getMessage());
        }
        
        return response;
    }
    
    public Map<String, Object> updateRateConfiguration(UUID id, Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<RateConfiguration> configOpt = rateConfigurationRepository.findById(id);
            if (configOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Rate configuration not found");
                return response;
            }
            
            RateConfiguration config = configOpt.get();
            if (request.containsKey("rateName")) config.setRateName((String) request.get("rateName"));
            if (request.containsKey("rateType")) config.setRateType((String) request.get("rateType"));
            if (request.containsKey("productType")) config.setProductType((String) request.get("productType"));
            if (request.containsKey("customerSegment")) config.setCustomerSegment((String) request.get("customerSegment"));
            if (request.containsKey("baseRate")) config.setBaseRate(new BigDecimal(request.get("baseRate").toString()));
            if (request.containsKey("spread")) config.setSpread(new BigDecimal(request.get("spread").toString()));
            if (request.containsKey("minimumRate")) config.setMinimumRate(new BigDecimal(request.get("minimumRate").toString()));
            if (request.containsKey("maximumRate")) config.setMaximumRate(new BigDecimal(request.get("maximumRate").toString()));
            if (request.containsKey("calculationMethod")) config.setCalculationMethod((String) request.get("calculationMethod"));
            if (request.containsKey("compoundingFrequency")) config.setCompoundingFrequency((String) request.get("compoundingFrequency"));
            if (request.containsKey("rateBasis")) config.setRateBasis((String) request.get("rateBasis"));
            if (request.containsKey("tierStructure")) config.setTierStructure(objectMapper.writeValueAsString(request.get("tierStructure")));
            if (request.containsKey("pricingFormula")) config.setPricingFormula((String) request.get("pricingFormula"));
            if (request.containsKey("benchmarkRate")) config.setBenchmarkRate((String) request.get("benchmarkRate"));
            if (request.containsKey("reviewFrequency")) config.setReviewFrequency((String) request.get("reviewFrequency"));
            if (request.containsKey("autoAdjustment")) config.setAutoAdjustment((Boolean) request.get("autoAdjustment"));
            if (request.containsKey("adjustmentThreshold")) config.setAdjustmentThreshold(new BigDecimal(request.get("adjustmentThreshold").toString()));
            if (request.containsKey("isActive")) config.setIsActive((Boolean) request.get("isActive"));
            
            config = rateConfigurationRepository.save(config);
            
            response.put("success", true);
            response.put("message", "Rate configuration updated successfully");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update rate configuration: " + e.getMessage());
        }
        
        return response;
    }
    
    public Map<String, Object> deleteRateConfiguration(UUID id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (rateConfigurationRepository.existsById(id)) {
                rateConfigurationRepository.deleteById(id);
                response.put("success", true);
                response.put("message", "Rate configuration deleted successfully");
            } else {
                response.put("success", false);
                response.put("message", "Rate configuration not found");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to delete rate configuration: " + e.getMessage());
        }
        
        return response;
    }
    
    public Map<String, Object> calculateRate(Map<String, Object> parameters) {
        Map<String, Object> calculation = new HashMap<>();
        
        try {
            String productType = (String) parameters.get("productType");
            String customerSegment = (String) parameters.get("customerSegment");
            BigDecimal amount = new BigDecimal(parameters.get("amount").toString());
            String rateType = (String) parameters.get("rateType");
            
            List<RateConfiguration> applicableRates = rateConfigurationRepository
                .findApplicableRates(productType, customerSegment, rateType);
            
            if (applicableRates.isEmpty()) {
                calculation.put("success", false);
                calculation.put("message", "No applicable rate configuration found");
                return calculation;
            }
            
            RateConfiguration rateConfig = applicableRates.get(0); // Get the first applicable rate
            
            BigDecimal effectiveRate = calculateEffectiveRate(rateConfig, amount);
            
            calculation.put("success", true);
            calculation.put("effectiveRate", effectiveRate);
            calculation.put("baseRate", rateConfig.getBaseRate());
            calculation.put("spread", rateConfig.getSpread());
            calculation.put("rateCode", rateConfig.getRateCode());
            calculation.put("rateName", rateConfig.getRateName());
            calculation.put("calculationMethod", rateConfig.getCalculationMethod());
            
        } catch (Exception e) {
            calculation.put("success", false);
            calculation.put("message", "Failed to calculate rate: " + e.getMessage());
        }
        
        return calculation;
    }
    
    public Map<String, Object> getEffectiveRate(String productType, String customerSegment) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<RateConfiguration> rates = rateConfigurationRepository
                .findByProductTypeAndCustomerSegmentAndIsActive(productType, customerSegment, true);
            
            List<Map<String, Object>> effectiveRates = new ArrayList<>();
            
            for (RateConfiguration rate : rates) {
                Map<String, Object> rateInfo = new HashMap<>();
                rateInfo.put("rateCode", rate.getRateCode());
                rateInfo.put("rateName", rate.getRateName());
                rateInfo.put("rateType", rate.getRateType());
                rateInfo.put("baseRate", rate.getBaseRate());
                rateInfo.put("spread", rate.getSpread());
                rateInfo.put("effectiveRate", calculateEffectiveRate(rate, BigDecimal.ZERO));
                rateInfo.put("minimumRate", rate.getMinimumRate());
                rateInfo.put("maximumRate", rate.getMaximumRate());
                effectiveRates.add(rateInfo);
            }
            
            result.put("success", true);
            result.put("productType", productType);
            result.put("customerSegment", customerSegment);
            result.put("rates", effectiveRates);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to get effective rates: " + e.getMessage());
        }
        
        return result;
    }
    
    public Map<String, Object> bulkUpdateRates(Map<String, Object> updateData) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String benchmarkRate = (String) updateData.get("benchmarkRate");
            BigDecimal adjustment = new BigDecimal(updateData.get("adjustment").toString());
            List<String> rateCodes = (List<String>) updateData.get("rateCodes");
            
            int updatedCount = 0;
            
            for (String rateCode : rateCodes) {
                Optional<RateConfiguration> configOpt = rateConfigurationRepository.findByRateCode(rateCode);
                if (configOpt.isPresent()) {
                    RateConfiguration config = configOpt.get();
                    if (benchmarkRate.equals(config.getBenchmarkRate())) {
                        BigDecimal newBaseRate = config.getBaseRate().add(adjustment);
                        
                        // Apply min/max constraints
                        if (config.getMinimumRate() != null && newBaseRate.compareTo(config.getMinimumRate()) < 0) {
                            newBaseRate = config.getMinimumRate();
                        }
                        if (config.getMaximumRate() != null && newBaseRate.compareTo(config.getMaximumRate()) > 0) {
                            newBaseRate = config.getMaximumRate();
                        }
                        
                        config.setBaseRate(newBaseRate);
                        rateConfigurationRepository.save(config);
                        updatedCount++;
                    }
                }
            }
            
            result.put("success", true);
            result.put("message", "Bulk rate update completed");
            result.put("updatedCount", updatedCount);
            result.put("benchmarkRate", benchmarkRate);
            result.put("adjustment", adjustment);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to bulk update rates: " + e.getMessage());
        }
        
        return result;
    }
    
    private BigDecimal calculateEffectiveRate(RateConfiguration config, BigDecimal amount) {
        BigDecimal effectiveRate = config.getBaseRate();
        
        if (config.getSpread() != null) {
            effectiveRate = effectiveRate.add(config.getSpread());
        }
        
        // Apply tiered structure if available
        if (config.getTierStructure() != null && !config.getTierStructure().isEmpty()) {
            try {
                List<Map<String, Object>> tiers = objectMapper.readValue(config.getTierStructure(), List.class);
                for (Map<String, Object> tier : tiers) {
                    BigDecimal tierMin = new BigDecimal(tier.get("minAmount").toString());
                    BigDecimal tierMax = tier.get("maxAmount") != null ? new BigDecimal(tier.get("maxAmount").toString()) : null;
                    BigDecimal tierRate = new BigDecimal(tier.get("rate").toString());
                    
                    if (amount.compareTo(tierMin) >= 0 && (tierMax == null || amount.compareTo(tierMax) <= 0)) {
                        effectiveRate = tierRate;
                        break;
                    }
                }
            } catch (Exception e) {
                // Use base rate if tier structure parsing fails
            }
        }
        
        // Apply min/max constraints
        if (config.getMinimumRate() != null && effectiveRate.compareTo(config.getMinimumRate()) < 0) {
            effectiveRate = config.getMinimumRate();
        }
        if (config.getMaximumRate() != null && effectiveRate.compareTo(config.getMaximumRate()) > 0) {
            effectiveRate = config.getMaximumRate();
        }
        
        return effectiveRate.setScale(6, RoundingMode.HALF_UP);
    }
}
