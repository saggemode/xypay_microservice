package com.xypay.xypay.controller;

import com.xypay.xypay.config.BranchConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class ConfigController {
    
    @Autowired
    private BranchConfig branchConfig;
    
    @GetMapping("/branches")
    public ResponseEntity<Map<String, BranchConfig.BranchProperties>> getBranchConfig() {
        return ResponseEntity.ok(branchConfig.getBranches());
    }
    
    @PostMapping("/branches/{branchId}")
    public ResponseEntity<String> updateBranchConfig(
            @PathVariable String branchId,
            @RequestBody BranchConfig.BranchProperties branchProperties) {
        
        // In a real implementation, this would update the configuration
        // For now, we'll just return a success message
        return ResponseEntity.ok("Branch configuration updated successfully");
    }
}