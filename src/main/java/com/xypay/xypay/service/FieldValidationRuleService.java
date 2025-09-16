package com.xypay.xypay.service;

import com.xypay.xypay.domain.FieldValidationRule;
import com.xypay.xypay.repository.FieldValidationRuleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.UUID;

@Service
public class FieldValidationRuleService {
    
    @Autowired
    private FieldValidationRuleRepository fieldValidationRuleRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public Map<String, Object> createValidationRule(Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            FieldValidationRule rule = new FieldValidationRule();
            rule.setRuleCode((String) request.get("ruleCode"));
            rule.setRuleName((String) request.get("ruleName"));
            rule.setScreenCode((String) request.get("screenCode"));
            rule.setFieldName((String) request.get("fieldName"));
            rule.setValidationType((String) request.get("validationType"));
            rule.setValidationExpression((String) request.get("validationValue"));
            rule.setErrorMessage((String) request.get("errorMessage"));
            rule.setIsRequired((Boolean) request.getOrDefault("isRequired", false));
            rule.setExecutionOrder((Integer) request.getOrDefault("executionOrder", 1));
            rule.setApplicableRoles(objectMapper.writeValueAsString(request.get("applicableRoles")));
            rule.setConditions(objectMapper.writeValueAsString(request.get("conditions")));
            
            rule = fieldValidationRuleRepository.save(rule);
            
            response.put("success", true);
            response.put("message", "Validation rule created successfully");
            response.put("ruleId", rule.getId());
            response.put("ruleCode", rule.getRuleCode());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to create validation rule: " + e.getMessage());
        }
        
        return response;
    }
    
    public FieldValidationRule getValidationRule(String ruleCode) {
        return fieldValidationRuleRepository.findByRuleCode(ruleCode).orElse(null);
    }
    
    public List<FieldValidationRule> getAllValidationRules() {
        return fieldValidationRuleRepository.findAll();
    }
    
    public List<FieldValidationRule> getValidationRulesByScreen(String screenCode) {
        return fieldValidationRuleRepository.findByScreenCodeAndIsActiveOrderByExecutionOrder(screenCode, true);
    }
    
    public Map<String, Object> updateValidationRule(UUID id, Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<FieldValidationRule> ruleOpt = fieldValidationRuleRepository.findById(id);
            if (ruleOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Validation rule not found");
                return response;
            }
            
            FieldValidationRule rule = ruleOpt.get();
            if (request.containsKey("ruleName")) rule.setRuleName((String) request.get("ruleName"));
            if (request.containsKey("screenCode")) rule.setScreenCode((String) request.get("screenCode"));
            if (request.containsKey("fieldName")) rule.setFieldName((String) request.get("fieldName"));
            if (request.containsKey("validationType")) rule.setValidationType((String) request.get("validationType"));
            if (request.containsKey("validationValue")) rule.setValidationExpression((String) request.get("validationValue"));
            if (request.containsKey("errorMessage")) rule.setErrorMessage((String) request.get("errorMessage"));
            if (request.containsKey("isRequired")) rule.setIsRequired((Boolean) request.get("isRequired"));
            if (request.containsKey("executionOrder")) rule.setExecutionOrder((Integer) request.get("executionOrder"));
            if (request.containsKey("applicableRoles")) rule.setApplicableRoles(objectMapper.writeValueAsString(request.get("applicableRoles")));
            if (request.containsKey("conditions")) rule.setConditions(objectMapper.writeValueAsString(request.get("conditions")));
            if (request.containsKey("isActive")) rule.setIsActive((Boolean) request.get("isActive"));
            
            rule = fieldValidationRuleRepository.save(rule);
            
            response.put("success", true);
            response.put("message", "Validation rule updated successfully");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update validation rule: " + e.getMessage());
        }
        
        return response;
    }
    
    public Map<String, Object> deleteValidationRule(UUID id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (fieldValidationRuleRepository.existsById(id)) {
                fieldValidationRuleRepository.deleteById(id);
                response.put("success", true);
                response.put("message", "Validation rule deleted successfully");
            } else {
                response.put("success", false);
                response.put("message", "Validation rule not found");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to delete validation rule: " + e.getMessage());
        }
        
        return response;
    }
    
    public Map<String, Object> validateField(String screenCode, String fieldName, Object fieldValue, String userRole) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        
        try {
            List<FieldValidationRule> rules = fieldValidationRuleRepository
                .findByScreenCodeAndFieldNameOrderByExecutionOrder(screenCode, fieldName);
            
            for (FieldValidationRule rule : rules) {
                if (!rule.getIsActive()) continue;
                
                // Check if rule applies to user role
                if (rule.getApplicableRoles() != null && !rule.getApplicableRoles().isEmpty()) {
                    @SuppressWarnings("unchecked")
                    List<String> applicableRoles = objectMapper.readValue(rule.getApplicableRoles(), List.class);
                    if (!applicableRoles.contains(userRole)) continue;
                }
                
                // Perform validation based on type
                boolean isValid = performValidation(rule, fieldValue);
                
                if (!isValid) {
                    errors.add(rule.getErrorMessage());
                    break; // Stop on first validation failure
                }
            }
            
            result.put("success", true);
            result.put("isValid", errors.isEmpty());
            result.put("errors", errors);
            result.put("fieldName", fieldName);
            result.put("fieldValue", fieldValue);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to validate field: " + e.getMessage());
        }
        
        return result;
    }
    
    public Map<String, Object> validateForm(String screenCode, Map<String, Object> formData, String userRole) {
        Map<String, Object> result = new HashMap<>();
        Map<String, List<String>> fieldErrors = new HashMap<>();
        boolean isFormValid = true;
        
        try {
            List<FieldValidationRule> rules = fieldValidationRuleRepository
                .findByScreenCodeAndIsActiveOrderByExecutionOrder(screenCode, true);
            
            // Group rules by field name
            Map<String, List<FieldValidationRule>> rulesByField = new HashMap<>();
            for (FieldValidationRule rule : rules) {
                rulesByField.computeIfAbsent(rule.getFieldName(), k -> new ArrayList<>()).add(rule);
            }
            
            // Validate each field
            for (Map.Entry<String, List<FieldValidationRule>> entry : rulesByField.entrySet()) {
                String fieldName = entry.getKey();
                List<FieldValidationRule> fieldRules = entry.getValue();
                Object fieldValue = formData.get(fieldName);
                
                List<String> errors = new ArrayList<>();
                
                for (FieldValidationRule rule : fieldRules) {
                    // Check if rule applies to user role
                    if (rule.getApplicableRoles() != null && !rule.getApplicableRoles().isEmpty()) {
                        @SuppressWarnings("unchecked")
                        List<String> applicableRoles = objectMapper.readValue(rule.getApplicableRoles(), List.class);
                        if (!applicableRoles.contains(userRole)) continue;
                    }
                    
                    // Perform validation
                    boolean isValid = performValidation(rule, fieldValue);
                    
                    if (!isValid) {
                        errors.add(rule.getErrorMessage());
                        isFormValid = false;
                    }
                }
                
                if (!errors.isEmpty()) {
                    fieldErrors.put(fieldName, errors);
                }
            }
            
            result.put("success", true);
            result.put("isValid", isFormValid);
            result.put("fieldErrors", fieldErrors);
            result.put("screenCode", screenCode);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to validate form: " + e.getMessage());
        }
        
        return result;
    }
    
    private boolean performValidation(FieldValidationRule rule, Object fieldValue) {
        String validationType = rule.getValidationType();
        String validationValue = rule.getValidationExpression();
        
        // Handle null/empty values
        if (fieldValue == null || fieldValue.toString().trim().isEmpty()) {
            return !rule.getIsRequired();
        }
        
        String value = fieldValue.toString();
        
        switch (validationType.toLowerCase()) {
            case "required":
                return !value.trim().isEmpty();
                
            case "regex":
                if (validationValue != null) {
                    return Pattern.matches(validationValue, value);
                }
                return true;
                
            case "minlength":
                if (validationValue != null) {
                    int minLength = Integer.parseInt(validationValue);
                    return value.length() >= minLength;
                }
                return true;
                
            case "maxlength":
                if (validationValue != null) {
                    int maxLength = Integer.parseInt(validationValue);
                    return value.length() <= maxLength;
                }
                return true;
                
            case "range":
                if (validationValue != null) {
                    String[] range = validationValue.split(",");
                    if (range.length == 2) {
                        try {
                            double val = Double.parseDouble(value);
                            double min = Double.parseDouble(range[0].trim());
                            double max = Double.parseDouble(range[1].trim());
                            return val >= min && val <= max;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    }
                }
                return true;
                
            case "email":
                String emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$";
                return Pattern.matches(emailRegex, value);
                
            case "phone":
                String phoneRegex = "^[+]?[0-9]{10,15}$";
                return Pattern.matches(phoneRegex, value.replaceAll("[\\s()-]", ""));
                
            case "numeric":
                try {
                    Double.parseDouble(value);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
                
            case "alphanumeric":
                return value.matches("^[a-zA-Z0-9]+$");
                
            case "custom":
                // For custom validations, you could implement a script engine
                // For now, return true as placeholder
                return true;
                
            default:
                return true;
        }
    }
}
