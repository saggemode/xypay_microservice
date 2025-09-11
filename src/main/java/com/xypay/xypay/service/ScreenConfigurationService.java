package com.xypay.xypay.service;

import com.xypay.xypay.domain.ScreenConfiguration;
import com.xypay.xypay.repository.ScreenConfigurationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ScreenConfigurationService {
    
    @Autowired
    private ScreenConfigurationRepository screenConfigurationRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public Map<String, Object> createScreenConfiguration(Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            ScreenConfiguration config = new ScreenConfiguration();
            config.setScreenCode((String) request.get("screenCode"));
            config.setScreenName((String) request.get("screenName"));
            config.setScreenType((String) request.get("screenType"));
            config.setLayoutConfig(objectMapper.writeValueAsString(request.get("layoutConfig")));
            config.setFieldConfig(objectMapper.writeValueAsString(request.get("fieldConfig")));
            config.setValidationRules(objectMapper.writeValueAsString(request.get("validationRules")));
            config.setAccessRoles(objectMapper.writeValueAsString(request.get("accessRoles")));
            config.setCreatedBy((String) request.get("createdBy"));
            
            config = screenConfigurationRepository.save(config);
            
            response.put("success", true);
            response.put("message", "Screen configuration created successfully");
            response.put("screenId", config.getId());
            response.put("screenCode", config.getScreenCode());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to create screen configuration: " + e.getMessage());
        }
        
        return response;
    }
    
    public ScreenConfiguration getScreenConfiguration(String screenCode) {
        return screenConfigurationRepository.findByScreenCode(screenCode).orElse(null);
    }
    
    public List<ScreenConfiguration> getAllScreenConfigurations() {
        return screenConfigurationRepository.findAll();
    }
    
    public Map<String, Object> updateScreenConfiguration(Long id, Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<ScreenConfiguration> configOpt = screenConfigurationRepository.findById(id);
            if (configOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Screen configuration not found");
                return response;
            }
            
            ScreenConfiguration config = configOpt.get();
            if (request.containsKey("screenName")) config.setScreenName((String) request.get("screenName"));
            if (request.containsKey("screenType")) config.setScreenType((String) request.get("screenType"));
            if (request.containsKey("layoutConfig")) config.setLayoutConfig(objectMapper.writeValueAsString(request.get("layoutConfig")));
            if (request.containsKey("fieldConfig")) config.setFieldConfig(objectMapper.writeValueAsString(request.get("fieldConfig")));
            if (request.containsKey("validationRules")) config.setValidationRules(objectMapper.writeValueAsString(request.get("validationRules")));
            if (request.containsKey("accessRoles")) config.setAccessRoles(objectMapper.writeValueAsString(request.get("accessRoles")));
            if (request.containsKey("isActive")) config.setIsActive((Boolean) request.get("isActive"));
            
            config.setVersion(config.getVersion() + 1);
            config = screenConfigurationRepository.save(config);
            
            response.put("success", true);
            response.put("message", "Screen configuration updated successfully");
            response.put("version", config.getVersion());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update screen configuration: " + e.getMessage());
        }
        
        return response;
    }
    
    public Map<String, Object> deleteScreenConfiguration(Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (screenConfigurationRepository.existsById(id)) {
                screenConfigurationRepository.deleteById(id);
                response.put("success", true);
                response.put("message", "Screen configuration deleted successfully");
            } else {
                response.put("success", false);
                response.put("message", "Screen configuration not found");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to delete screen configuration: " + e.getMessage());
        }
        
        return response;
    }
    
    public Map<String, Object> generateDynamicForm(String screenCode) {
        Map<String, Object> formConfig = new HashMap<>();
        
        try {
            ScreenConfiguration config = getScreenConfiguration(screenCode);
            if (config == null) {
                formConfig.put("error", "Screen configuration not found");
                return formConfig;
            }
            
            Map<String, Object> layoutConfig = objectMapper.readValue(config.getLayoutConfig(), Map.class);
            Map<String, Object> fieldConfig = objectMapper.readValue(config.getFieldConfig(), Map.class);
            Map<String, Object> validationRules = objectMapper.readValue(config.getValidationRules(), Map.class);
            
            formConfig.put("screenCode", config.getScreenCode());
            formConfig.put("screenName", config.getScreenName());
            formConfig.put("screenType", config.getScreenType());
            formConfig.put("layout", layoutConfig);
            formConfig.put("fields", fieldConfig);
            formConfig.put("validations", validationRules);
            formConfig.put("version", config.getVersion());
            
        } catch (Exception e) {
            formConfig.put("error", "Failed to generate form: " + e.getMessage());
        }
        
        return formConfig;
    }
    
    public Map<String, Object> validateFormData(String screenCode, Map<String, Object> formData) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        try {
            ScreenConfiguration config = getScreenConfiguration(screenCode);
            if (config == null) {
                result.put("valid", false);
                result.put("error", "Screen configuration not found");
                return result;
            }
            
            Map<String, Object> validationRules = objectMapper.readValue(config.getValidationRules(), Map.class);
            
            // Perform validation logic here
            for (Map.Entry<String, Object> entry : formData.entrySet()) {
                String fieldName = entry.getKey();
                Object fieldValue = entry.getValue();
                
                if (validationRules.containsKey(fieldName)) {
                    Map<String, Object> fieldRules = (Map<String, Object>) validationRules.get(fieldName);
                    
                    // Required field validation
                    if (Boolean.TRUE.equals(fieldRules.get("required")) && (fieldValue == null || fieldValue.toString().trim().isEmpty())) {
                        errors.add(fieldName + " is required");
                    }
                    
                    // Add more validation logic as needed
                }
            }
            
            result.put("valid", errors.isEmpty());
            result.put("errors", errors);
            result.put("warnings", warnings);
            
        } catch (Exception e) {
            result.put("valid", false);
            result.put("error", "Validation failed: " + e.getMessage());
        }
        
        return result;
    }
    
    public Map<String, Object> previewScreenConfiguration(Map<String, Object> configData) {
        Map<String, Object> preview = new HashMap<>();
        
        try {
            preview.put("screenName", configData.get("screenName"));
            preview.put("screenType", configData.get("screenType"));
            preview.put("layout", configData.get("layoutConfig"));
            preview.put("fields", configData.get("fieldConfig"));
            preview.put("validations", configData.get("validationRules"));
            preview.put("previewGenerated", true);
            
        } catch (Exception e) {
            preview.put("error", "Failed to generate preview: " + e.getMessage());
        }
        
        return preview;
    }
}
