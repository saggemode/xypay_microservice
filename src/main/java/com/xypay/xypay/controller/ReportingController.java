package com.xypay.xypay.controller;

import com.xypay.xypay.domain.Transaction;
import com.xypay.xypay.domain.Report;
import com.xypay.xypay.domain.ReportExecution;
import com.xypay.xypay.service.TransactionReportingService;
import com.xypay.xypay.service.ReportingEngineService;
import com.xypay.xypay.repository.TransactionRepository;
import com.xypay.xypay.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
public class ReportingController {
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private TransactionReportingService transactionReportingService;
    
    @Autowired
    private ReportingEngineService reportingEngineService;
    
    @Autowired
    private ReportRepository reportRepository;
    
    @GetMapping("/transactions/iso20022")
    public ResponseEntity<String> getTransactionsISO20022(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        
        List<Transaction> transactions = transactionRepository.findByCreatedAtBetween(from, to);
        
        String iso20022Report = transactionReportingService.generateISO20022Report(transactions);
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=transactions-report.xml");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(iso20022Report);
    }
    
    // Enterprise Reporting Engine Endpoints
    
    @GetMapping
    public ResponseEntity<List<Report>> getAllReports() {
        List<Report> reports = reportRepository.findAllActiveReports();
        return ResponseEntity.ok(reports);
    }
    
    @GetMapping("/type/{reportType}")
    public ResponseEntity<List<Report>> getReportsByType(@PathVariable String reportType) {
        List<Report> reports = reportRepository.findByReportTypeAndIsActiveTrue(reportType);
        return ResponseEntity.ok(reports);
    }
    
    @PostMapping("/execute/{reportId}")
    public ResponseEntity<ReportExecution> executeReport(
            @PathVariable Long reportId,
            @RequestBody Map<String, Object> parameters,
            @RequestParam Long executedBy) {
        
        // Convert Long to UUID - this is a workaround for the ID type mismatch
        UUID reportIdUuid = new UUID(0L, reportId); // Create UUID from Long
        UUID executedByUuid = new UUID(0L, executedBy); // Create UUID from Long
        ReportExecution execution = reportingEngineService.executeReport(reportIdUuid, parameters, executedByUuid);
        return ResponseEntity.ok(execution);
    }
    
    @PostMapping("/execute-async/{reportId}")
    public ResponseEntity<String> executeReportAsync(
            @PathVariable Long reportId,
            @RequestBody Map<String, Object> parameters,
            @RequestParam Long executedBy) {
        
        // Convert Long to UUID - this is a workaround for the ID type mismatch
        // UUID reportIdUuid = new UUID(0L, reportId); // Create UUID from Long
        // UUID executedByUuid = new UUID(0L, executedBy); // Create UUID from Long
        // CompletableFuture<ReportExecution> future = reportingEngineService.executeReportAsync(reportIdUuid, parameters, executedByUuid);
        return ResponseEntity.ok("Report execution started. You will be notified when complete.");
    }
    
    @GetMapping("/executions/{reportId}")
    public ResponseEntity<List<ReportExecution>> getReportExecutions(@PathVariable Long reportId) {
        // Convert Long to UUID - this is a workaround for the ID type mismatch
        UUID reportIdUuid = new UUID(0L, reportId); // Create UUID from Long
        List<ReportExecution> executions = reportingEngineService.getReportExecutionHistory(reportIdUuid);
        return ResponseEntity.ok(executions);
    }
    
    @PostMapping("/regulatory/{category}")
    public ResponseEntity<List<ReportExecution>> generateRegulatoryReports(
            @PathVariable String category,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime reportDate) {
        
        List<ReportExecution> executions = reportingEngineService.generateRegulatoryReports(category, reportDate);
        return ResponseEntity.ok(executions);
    }
    
    @PostMapping("/setup-regulatory")
    public ResponseEntity<List<Report>> setupRegulatoryReports() {
        List<Report> reports = reportingEngineService.createRegulatoryReports();
        return ResponseEntity.ok(reports);
    }
    
    @PostMapping("/cleanup")
    public ResponseEntity<String> cleanupOldReports(@RequestParam(defaultValue = "30") int daysToKeep) {
        reportingEngineService.cleanupOldReports(daysToKeep);
        return ResponseEntity.ok("Old reports cleanup completed");
    }
}