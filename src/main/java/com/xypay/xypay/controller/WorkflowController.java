package com.xypay.xypay.controller;

import com.xypay.xypay.domain.WorkflowDefinition;
import com.xypay.xypay.domain.WorkflowInstance;
import com.xypay.xypay.service.WorkflowService;
import com.xypay.xypay.service.WorkflowEngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {
    private final WorkflowService service;
    
    @Autowired
    private WorkflowEngineService workflowEngineService;
    
    public WorkflowController(WorkflowService service) { this.service = service; }

    @GetMapping
    public List<WorkflowDefinition> list(Principal principal) {
        return service.findByOwner(principal.getName());
    }

    @GetMapping("/all")
    public List<WorkflowDefinition> all() { return service.findAll(); }

    @GetMapping("/{id}")
    public WorkflowDefinition get(@PathVariable UUID id) { return service.findById(id); }

    @PostMapping
    public WorkflowDefinition save(@RequestBody WorkflowDefinition def, Principal principal) {
        def.setOwner(principal.getName());
        return service.save(def);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) { service.save(null); }
    
    // Workflow Engine Endpoints
    
    @PostMapping("/start")
    public ResponseEntity<WorkflowInstance> startWorkflow(
            @RequestBody Map<String, Object> request,
            Principal principal) {
        
        String workflowType = (String) request.get("workflowType");
        String entityType = (String) request.get("entityType");
        UUID entityId = UUID.fromString(request.get("entityId").toString());
        UUID initiatedBy = UUID.fromString(request.get("initiatedBy").toString());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> contextData = (Map<String, Object>) request.get("contextData");
        
        WorkflowInstance instance = workflowEngineService.startWorkflow(
            workflowType, entityType, entityId, initiatedBy, contextData);
        
        return ResponseEntity.ok(instance);
    }
    
    @PostMapping("/action/{stepId}")
    public ResponseEntity<String> processAction(
            @PathVariable UUID stepId,
            @RequestBody Map<String, Object> request,
            Principal principal) {
        
        String action = (String) request.get("action"); // APPROVED, REJECTED
        String comments = (String) request.get("comments");
        UUID actionBy = UUID.fromString(request.get("actionBy").toString());
        
        workflowEngineService.processAction(stepId, action, comments, actionBy);
        
        return ResponseEntity.ok("Action processed successfully");
    }
    
    @GetMapping("/pending/user/{userId}")
    public ResponseEntity<List<WorkflowInstance>> getPendingWorkflowsForUser(
            @PathVariable UUID userId) {
        
        List<WorkflowInstance> workflows = workflowEngineService.getPendingWorkflowsForUser(userId);
        return ResponseEntity.ok(workflows);
    }
    
    @GetMapping("/pending/role/{role}")
    public ResponseEntity<List<WorkflowInstance>> getPendingWorkflowsForRole(
            @PathVariable String role) {
        
        List<WorkflowInstance> workflows = workflowEngineService.getPendingWorkflowsForRole(role);
        return ResponseEntity.ok(workflows);
    }
    
    @PostMapping("/escalate")
    public ResponseEntity<String> processEscalations() {
        workflowEngineService.processEscalations();
        return ResponseEntity.ok("Escalations processed");
    }
}
