package com.xypay.xypay.admin;

import com.xypay.xypay.config.ReportingConfiguration;
import com.xypay.xypay.config.WorkflowConfiguration;
import com.xypay.xypay.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/admin/reporting-workflow")
public class ReportingWorkflowAdminController {
    
    @Autowired
    private ConfigurationService configurationService;
    
    // Reporting Configuration Endpoints
    
    @PostMapping("/reporting-config")
    public ResponseEntity<ReportingConfiguration> createReportingConfig(
            @RequestBody ReportingConfiguration config) {
        ReportingConfiguration savedConfig = configurationService.saveReportingConfiguration(config);
        return ResponseEntity.ok(savedConfig);
    }
    
    @GetMapping("/reporting-config/{reportName}")
    public ResponseEntity<ReportingConfiguration> getReportingConfig(
            @PathVariable String reportName) {
        ReportingConfiguration config = configurationService.getReportingConfiguration(reportName);
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/reporting-config")
    public ResponseEntity<List<ReportingConfiguration>> getAllReportingConfigs() {
        List<ReportingConfiguration> configs = configurationService.getAllActiveReportingConfigurations();
        return ResponseEntity.ok(configs);
    }
    
    @GetMapping("/reporting-config/type/{reportType}")
    public ResponseEntity<List<ReportingConfiguration>> getReportingConfigsByType(
            @PathVariable String reportType) {
        List<ReportingConfiguration> configs = configurationService.getActiveReportsByType(reportType);
        return ResponseEntity.ok(configs);
    }
    
    @PutMapping("/reporting-config/{id}")
    public ResponseEntity<ReportingConfiguration> updateReportingConfiguration(
            @PathVariable Long id,
            @RequestBody ReportingConfiguration config) {
        // In a real implementation, we would update the existing record
        ReportingConfiguration updatedConfig = configurationService.saveReportingConfiguration(config);
        return ResponseEntity.ok(updatedConfig);
    }
    
    @DeleteMapping("/reporting-config/{id}")
    public ResponseEntity<Void> deleteReportingConfiguration(@PathVariable Long id) {
        // In a real implementation, we would delete the record
        return ResponseEntity.ok().build();
    }
    
    // Workflow Configuration Endpoints
    
    @PostMapping("/workflow-config")
    public ResponseEntity<WorkflowConfiguration> createWorkflowConfig(
            @RequestBody WorkflowConfiguration config) {
        WorkflowConfiguration savedConfig = configurationService.saveWorkflowConfiguration(config);
        return ResponseEntity.ok(savedConfig);
    }
    
    @GetMapping("/workflow-config/{workflowName}")
    public ResponseEntity<WorkflowConfiguration> getWorkflowConfig(
            @PathVariable String workflowName) {
        WorkflowConfiguration config = configurationService.getWorkflowConfiguration(workflowName);
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/workflow-config")
    public ResponseEntity<List<WorkflowConfiguration>> getAllWorkflowConfigs() {
        List<WorkflowConfiguration> configs = configurationService.getAllActiveWorkflowConfigurations();
        return ResponseEntity.ok(configs);
    }
    
    @GetMapping("/workflow-config/process-type/{processType}")
    public ResponseEntity<List<WorkflowConfiguration>> getWorkflowConfigsByProcessType(
            @PathVariable String processType) {
        List<WorkflowConfiguration> configs = configurationService.getActiveWorkflowsByProcessType(processType);
        return ResponseEntity.ok(configs);
    }
    
    @PutMapping("/workflow-config/{id}")
    public ResponseEntity<WorkflowConfiguration> updateWorkflowConfiguration(
            @PathVariable Long id,
            @RequestBody WorkflowConfiguration config) {
        // In a real implementation, we would update the existing record
        WorkflowConfiguration updatedConfig = configurationService.saveWorkflowConfiguration(config);
        return ResponseEntity.ok(updatedConfig);
    }
    
    @DeleteMapping("/workflow-config/{id}")
    public ResponseEntity<Void> deleteWorkflowConfiguration(@PathVariable Long id) {
        // In a real implementation, we would delete the record
        return ResponseEntity.ok().build();
    }
}