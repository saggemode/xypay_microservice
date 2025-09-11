package com.xypay.xypay.admin;

import com.xypay.xypay.domain.ApprovalMatrix;
import com.xypay.xypay.service.ApprovalMatrixService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/approval-matrix")
public class ApprovalMatrixController {
    
    @Autowired
    private ApprovalMatrixService approvalMatrixService;
    
    /**
     * Create approval matrix configuration
     */
    @PostMapping("/matrix-config")
    public ResponseEntity<Map<String, Object>> createMatrixConfig(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = approvalMatrixService.createApprovalMatrix(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get approval matrix by code
     */
    @GetMapping("/matrix-config/{matrixCode}")
    public ResponseEntity<ApprovalMatrix> getMatrixConfig(@PathVariable String matrixCode) {
        ApprovalMatrix matrix = approvalMatrixService.getApprovalMatrix(matrixCode);
        return matrix != null ? ResponseEntity.ok(matrix) : ResponseEntity.notFound().build();
    }
    
    /**
     * Get all approval matrices
     */
    @GetMapping("/matrix-config")
    public ResponseEntity<List<ApprovalMatrix>> getAllMatrixConfigs() {
        List<ApprovalMatrix> matrices = approvalMatrixService.getAllApprovalMatrices();
        return ResponseEntity.ok(matrices);
    }
    
    /**
     * Get approval requirements for transaction
     */
    @PostMapping("/get-approval-requirements")
    public ResponseEntity<Map<String, Object>> getApprovalRequirements(@RequestBody Map<String, Object> transactionData) {
        Map<String, Object> requirements = approvalMatrixService.getApprovalRequirements(transactionData);
        return ResponseEntity.ok(requirements);
    }
    
    /**
     * Update approval matrix
     */
    @PutMapping("/matrix-config/{id}")
    public ResponseEntity<Map<String, Object>> updateMatrixConfig(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Map<String, Object> response = approvalMatrixService.updateApprovalMatrix(id, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete approval matrix
     */
    @DeleteMapping("/matrix-config/{id}")
    public ResponseEntity<Map<String, Object>> deleteMatrixConfig(@PathVariable Long id) {
        Map<String, Object> response = approvalMatrixService.deleteApprovalMatrix(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Test approval matrix against sample transaction
     */
    @PostMapping("/test-matrix")
    public ResponseEntity<Map<String, Object>> testMatrix(@RequestBody Map<String, Object> testData) {
        Map<String, Object> result = approvalMatrixService.testApprovalMatrix(testData);
        return ResponseEntity.ok(result);
    }
}
