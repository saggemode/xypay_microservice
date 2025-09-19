package com.xypay.xypay.controller;

import com.xypay.xypay.domain.STPRule;
import com.xypay.xypay.domain.Transaction;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.service.StraightThroughProcessingService;
import com.xypay.xypay.service.StraightThroughProcessingService.STPResult;
import com.xypay.xypay.repository.STPRuleRepository;
import com.xypay.xypay.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/stp")
public class STPController {

    @Autowired
    private StraightThroughProcessingService stpService;
    
    @Autowired
    private STPRuleRepository stpRuleRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    

    @PostMapping("/process/{transactionId}")
    public ResponseEntity<STPResult> processTransaction(
            @PathVariable UUID transactionId,
            @RequestBody(required = false) Map<String, Object> context) {
        
        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transaction not found"));
        
        User user = transaction.getWallet().getUser();
        
        if (context == null) {
            context = new HashMap<>();
        }
        
        STPResult result = stpService.processTransaction(transaction, user, context);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/rules")
    public ResponseEntity<List<STPRule>> getAllRules() {
        List<STPRule> rules = stpRuleRepository.findAll();
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/rules/entity/{entityType}")
    public ResponseEntity<List<STPRule>> getRulesByEntity(@PathVariable String entityType) {
        List<STPRule> rules = stpRuleRepository.findByEntityTypeAndIsActiveTrueOrderByPriorityDesc(entityType);
        return ResponseEntity.ok(rules);
    }

    @PostMapping("/rules")
    public ResponseEntity<STPRule> createRule(@RequestBody STPRule rule) {
        STPRule savedRule = stpRuleRepository.save(rule);
        return ResponseEntity.ok(savedRule);
    }

    @PutMapping("/rules/{ruleId}")
    public ResponseEntity<STPRule> updateRule(@PathVariable UUID ruleId, @RequestBody STPRule rule) {
        STPRule existing = stpRuleRepository.findById(ruleId).orElseThrow(() -> new RuntimeException("Rule not found"));
        existing.setEntityType(rule.getEntityType());
        existing.setPriority(rule.getPriority());
        existing.setIsActive(rule.getIsActive());
        STPRule updatedRule = stpRuleRepository.save(existing);
        return ResponseEntity.ok(updatedRule);
    }

    @DeleteMapping("/rules/{ruleId}")
    public ResponseEntity<String> deleteRule(@PathVariable UUID ruleId) {
        stpRuleRepository.deleteById(ruleId);
        return ResponseEntity.ok("Rule deleted successfully");
    }

    @PostMapping("/setup-default")
    public ResponseEntity<List<STPRule>> setupDefaultRules() {
        List<STPRule> rules = stpService.createDefaultSTPRules();
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = stpService.getSTPStatistics();
        return ResponseEntity.ok(stats);
    }
}
