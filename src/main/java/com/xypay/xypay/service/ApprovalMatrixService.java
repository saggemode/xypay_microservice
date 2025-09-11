package com.xypay.xypay.service;

import com.xypay.xypay.domain.ApprovalMatrix;
import com.xypay.xypay.repository.ApprovalMatrixRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ApprovalMatrixService {
    
    @Autowired
    private ApprovalMatrixRepository approvalMatrixRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public Map<String, Object> createApprovalMatrix(Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            ApprovalMatrix matrix = new ApprovalMatrix();
            matrix.setMatrixCode((String) request.get("matrixCode"));
            matrix.setMatrixName((String) request.get("matrixName"));
            matrix.setTransactionType((String) request.get("transactionType"));
            matrix.setProductType((String) request.get("productType"));
            matrix.setBranchCode((String) request.get("branchCode"));
            matrix.setCustomerType((String) request.get("customerType"));
            
            if (request.get("amountFrom") != null) {
                matrix.setAmountFrom(new BigDecimal(request.get("amountFrom").toString()));
            }
            if (request.get("amountTo") != null) {
                matrix.setAmountTo(new BigDecimal(request.get("amountTo").toString()));
            }
            
            matrix.setCurrency((String) request.get("currency"));
            matrix.setApprovalLevel((Integer) request.get("approvalLevel"));
            matrix.setRequiredRole((String) request.get("requiredRole"));
            matrix.setAlternativeRoles(objectMapper.writeValueAsString(request.get("alternativeRoles")));
            matrix.setIsMandatory((Boolean) request.getOrDefault("isMandatory", true));
            matrix.setCanSelfApprove((Boolean) request.getOrDefault("canSelfApprove", false));
            matrix.setTimeoutHours((Integer) request.get("timeoutHours"));
            matrix.setEscalationRole((String) request.get("escalationRole"));
            matrix.setApprovalConditions(objectMapper.writeValueAsString(request.get("approvalConditions")));
            matrix.setEffectiveFrom(LocalDateTime.now());
            
            matrix = approvalMatrixRepository.save(matrix);
            
            response.put("success", true);
            response.put("message", "Approval matrix created successfully");
            response.put("matrixId", matrix.getId());
            response.put("matrixCode", matrix.getMatrixCode());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to create approval matrix: " + e.getMessage());
        }
        
        return response;
    }
    
    public ApprovalMatrix getApprovalMatrix(String matrixCode) {
        return approvalMatrixRepository.findByMatrixCode(matrixCode).orElse(null);
    }
    
    public List<ApprovalMatrix> getAllApprovalMatrices() {
        return approvalMatrixRepository.findAll();
    }
    
    public Map<String, Object> updateApprovalMatrix(Long id, Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<ApprovalMatrix> matrixOpt = approvalMatrixRepository.findById(id);
            if (matrixOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Approval matrix not found");
                return response;
            }
            
            ApprovalMatrix matrix = matrixOpt.get();
            if (request.containsKey("matrixName")) matrix.setMatrixName((String) request.get("matrixName"));
            if (request.containsKey("transactionType")) matrix.setTransactionType((String) request.get("transactionType"));
            if (request.containsKey("productType")) matrix.setProductType((String) request.get("productType"));
            if (request.containsKey("branchCode")) matrix.setBranchCode((String) request.get("branchCode"));
            if (request.containsKey("customerType")) matrix.setCustomerType((String) request.get("customerType"));
            if (request.containsKey("amountFrom")) matrix.setAmountFrom(new BigDecimal(request.get("amountFrom").toString()));
            if (request.containsKey("amountTo")) matrix.setAmountTo(new BigDecimal(request.get("amountTo").toString()));
            if (request.containsKey("currency")) matrix.setCurrency((String) request.get("currency"));
            if (request.containsKey("approvalLevel")) matrix.setApprovalLevel((Integer) request.get("approvalLevel"));
            if (request.containsKey("requiredRole")) matrix.setRequiredRole((String) request.get("requiredRole"));
            if (request.containsKey("alternativeRoles")) matrix.setAlternativeRoles(objectMapper.writeValueAsString(request.get("alternativeRoles")));
            if (request.containsKey("isMandatory")) matrix.setIsMandatory((Boolean) request.get("isMandatory"));
            if (request.containsKey("canSelfApprove")) matrix.setCanSelfApprove((Boolean) request.get("canSelfApprove"));
            if (request.containsKey("timeoutHours")) matrix.setTimeoutHours((Integer) request.get("timeoutHours"));
            if (request.containsKey("escalationRole")) matrix.setEscalationRole((String) request.get("escalationRole"));
            if (request.containsKey("approvalConditions")) matrix.setApprovalConditions(objectMapper.writeValueAsString(request.get("approvalConditions")));
            if (request.containsKey("isActive")) matrix.setIsActive((Boolean) request.get("isActive"));
            
            matrix = approvalMatrixRepository.save(matrix);
            
            response.put("success", true);
            response.put("message", "Approval matrix updated successfully");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update approval matrix: " + e.getMessage());
        }
        
        return response;
    }
    
    public Map<String, Object> deleteApprovalMatrix(Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (approvalMatrixRepository.existsById(id)) {
                approvalMatrixRepository.deleteById(id);
                response.put("success", true);
                response.put("message", "Approval matrix deleted successfully");
            } else {
                response.put("success", false);
                response.put("message", "Approval matrix not found");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to delete approval matrix: " + e.getMessage());
        }
        
        return response;
    }
    
    public Map<String, Object> getApprovalRequirements(Map<String, Object> transactionData) {
        Map<String, Object> requirements = new HashMap<>();
        
        try {
            String transactionType = (String) transactionData.get("transactionType");
            BigDecimal amount = new BigDecimal(transactionData.get("amount").toString());
            String currency = (String) transactionData.get("currency");
            String customerType = (String) transactionData.get("customerType");
            String productType = (String) transactionData.get("productType");
            String branchCode = (String) transactionData.get("branchCode");
            
            List<ApprovalMatrix> applicableMatrices = approvalMatrixRepository
                .findApplicableMatrices(transactionType, amount, currency, customerType, productType, branchCode);
            
            List<Map<String, Object>> approvalLevels = new ArrayList<>();
            
            for (ApprovalMatrix matrix : applicableMatrices) {
                Map<String, Object> level = new HashMap<>();
                level.put("approvalLevel", matrix.getApprovalLevel());
                level.put("requiredRole", matrix.getRequiredRole());
                level.put("isMandatory", matrix.getIsMandatory());
                level.put("canSelfApprove", matrix.getCanSelfApprove());
                level.put("timeoutHours", matrix.getTimeoutHours());
                level.put("escalationRole", matrix.getEscalationRole());
                
                if (matrix.getAlternativeRoles() != null) {
                    level.put("alternativeRoles", objectMapper.readValue(matrix.getAlternativeRoles(), List.class));
                }
                
                approvalLevels.add(level);
            }
            
            // Sort by approval level
            approvalLevels.sort((a, b) -> Integer.compare((Integer) a.get("approvalLevel"), (Integer) b.get("approvalLevel")));
            
            requirements.put("success", true);
            requirements.put("approvalRequired", !approvalLevels.isEmpty());
            requirements.put("approvalLevels", approvalLevels);
            requirements.put("totalLevels", approvalLevels.size());
            
        } catch (Exception e) {
            requirements.put("success", false);
            requirements.put("message", "Failed to get approval requirements: " + e.getMessage());
        }
        
        return requirements;
    }
    
    public Map<String, Object> testApprovalMatrix(Map<String, Object> testData) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> transactionData = (Map<String, Object>) testData.get("transactionData");
            Map<String, Object> requirements = getApprovalRequirements(transactionData);
            
            result.put("success", true);
            result.put("testResult", requirements);
            result.put("message", "Approval matrix test completed successfully");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to test approval matrix: " + e.getMessage());
        }
        
        return result;
    }
}
