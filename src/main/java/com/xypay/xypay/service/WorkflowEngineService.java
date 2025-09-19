package com.xypay.xypay.service;

import com.xypay.xypay.domain.*;
import com.xypay.xypay.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.UUID;

@Service
@Transactional
public class WorkflowEngineService {

    @Autowired
    private WorkflowDefinitionRepository workflowDefinitionRepository;
    
    @Autowired
    private WorkflowInstanceRepository workflowInstanceRepository;
    
    @Autowired
    private WorkflowInstanceStepRepository workflowInstanceStepRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Start a workflow for a specific entity
     */
    public WorkflowInstance startWorkflow(String workflowType, String entityType, UUID entityId, 
                                        UUID initiatedBy, Map<String, Object> contextData) {
        
        // Find the appropriate workflow definition
        List<WorkflowDefinition> definitions = workflowDefinitionRepository
            .findLatestVersionByType(workflowType);
        
        if (definitions.isEmpty()) {
            throw new RuntimeException("No active workflow definition found for type: " + workflowType);
        }
        
        WorkflowDefinition definition = definitions.get(0);
        
        // Create workflow instance
        WorkflowInstance instance = new WorkflowInstance();
        instance.setWorkflowDefinition(definition);
        instance.setEntityType(entityType);
        instance.setEntityId(entityId);
        instance.setStatus("PENDING");
        instance.setInitiatedBy(initiatedBy);
        instance.setPriority("MEDIUM");
        
        try {
            instance.setContextData(objectMapper.writeValueAsString(contextData));
        } catch (Exception e) {
            instance.setContextData("{}");
        }
        
        instance = workflowInstanceRepository.save(instance);
        
        // Create instance steps based on workflow definition
        createInstanceSteps(instance, definition);
        
        // Start the first step
        processNextStep(instance.getId());
        
        return instance;
    }

    /**
     * Process approval/rejection action
     */
    public void processAction(UUID instanceStepId, String action, String comments, UUID actionBy) {
        WorkflowInstanceStep step = workflowInstanceStepRepository.findById(instanceStepId)
            .orElseThrow(() -> new RuntimeException("Workflow step not found"));
        
        if (!"PENDING".equals(step.getStatus())) {
            throw new RuntimeException("Step is not in pending status");
        }
        
        // Update step status
        step.setStatus(action); // APPROVED, REJECTED
        step.setActionTakenBy(actionBy);
        step.setActionDate(LocalDateTime.now());
        step.setComments(comments);
        
        workflowInstanceStepRepository.save(step);
        
        // Update workflow instance based on action
        WorkflowInstance instance = step.getWorkflowInstance();
        
        if ("REJECTED".equals(action)) {
            instance.setStatus("REJECTED");
            instance.setCompletedAt(LocalDateTime.now());
            workflowInstanceRepository.save(instance);
            
            // Notify rejection
            notifyWorkflowCompletion(instance, "REJECTED");
        } else if ("APPROVED".equals(action)) {
            // Process next step
            processNextStep(instance.getId());
        }
    }

    /**
     * Process the next step in workflow
     */
    private void processNextStep(UUID instanceId) {
        WorkflowInstance instance = workflowInstanceRepository.findById(instanceId)
            .orElseThrow(() -> new RuntimeException("Workflow instance not found"));
        
        List<WorkflowInstanceStep> steps = workflowInstanceStepRepository
            .findByWorkflowInstanceIdOrderByStepOrder(instanceId);
        
        // Find next pending step
        Optional<WorkflowInstanceStep> nextStep = steps.stream()
            .filter(s -> "PENDING".equals(s.getStatus()))
            .findFirst();
        
        if (nextStep.isPresent()) {
            WorkflowInstanceStep step = nextStep.get();
            
            // Check if step can be auto-approved
            if (canAutoApprove(step, instance)) {
                step.setStatus("APPROVED");
                step.setActionDate(LocalDateTime.now());
                step.setComments("Auto-approved based on conditions");
                workflowInstanceStepRepository.save(step);
                
                // Continue to next step
                processNextStep(instanceId);
                return;
            }
            
            // Assign step to appropriate approver
            assignStep(step);
            
            instance.setCurrentStepId(step.getId());
            instance.setStatus("IN_PROGRESS");
            workflowInstanceRepository.save(instance);
            
            // Send notification
            notifyStepAssignment(step);
            
        } else {
            // All steps completed - workflow approved
            instance.setStatus("APPROVED");
            instance.setCompletedAt(LocalDateTime.now());
            workflowInstanceRepository.save(instance);
            
            notifyWorkflowCompletion(instance, "APPROVED");
        }
    }

    /**
     * Check if step can be auto-approved based on conditions
     */
    private boolean canAutoApprove(WorkflowInstanceStep step, WorkflowInstance instance) {
        WorkflowStep workflowStep = step.getWorkflowStep();
        
        if (workflowStep.getAutoApproveConditions() == null || 
            workflowStep.getAutoApproveConditions().isEmpty()) {
            return false;
        }
        
        try {
            Map<String, Object> contextData = objectMapper.readValue(
                instance.getContextData(), new TypeReference<Map<String, Object>>() {});
            
            // Simple condition evaluation (can be enhanced)
            if (workflowStep.getApprovalLimit() != null) {
                Object amountObj = contextData.get("amount");
                if (amountObj != null) {
                    BigDecimal amount = new BigDecimal(amountObj.toString());
                    return amount.compareTo(workflowStep.getApprovalLimit()) <= 0;
                }
            }
            
        } catch (Exception e) {
            // Log error and don't auto-approve
        }
        
        return false;
    }

    /**
     * Assign step to appropriate user/role
     */
    private void assignStep(WorkflowInstanceStep step) {
        WorkflowStep workflowStep = step.getWorkflowStep();
        
        if (workflowStep.getApproverUserId() != null) {
            step.setAssignedTo(workflowStep.getApproverUserId());
        }
        
        if (workflowStep.getApproverRole() != null) {
            step.setAssignedRole(workflowStep.getApproverRole());
        }
        
        step.setStatus("PENDING");
        workflowInstanceStepRepository.save(step);
    }

    /**
     * Create instance steps from workflow definition
     */
    private void createInstanceSteps(WorkflowInstance instance, WorkflowDefinition definition) {
        List<WorkflowStep> steps = definition.getSteps();
        
        for (WorkflowStep workflowStep : steps) {
            WorkflowInstanceStep instanceStep = new WorkflowInstanceStep();
            instanceStep.setWorkflowInstance(instance);
            instanceStep.setWorkflowStep(workflowStep);
            instanceStep.setStatus("PENDING");
            
            workflowInstanceStepRepository.save(instanceStep);
        }
    }

    /**
     * Get pending workflows for a user
     */
    public List<WorkflowInstance> getPendingWorkflowsForUser(UUID userId) {
        return workflowInstanceRepository.findWorkflowsAssignedToUser(userId);
    }

    /**
     * Get pending workflows for a role
     */
    public List<WorkflowInstance> getPendingWorkflowsForRole(String role) {
        return workflowInstanceRepository.findWorkflowsAssignedToRole(role);
    }

    /**
     * Handle escalation for overdue steps
     */
    public void processEscalations() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24); // Default 24 hours
        
        List<WorkflowInstanceStep> overdueSteps = workflowInstanceStepRepository
            .findStepsForEscalation(cutoffTime);
        
        for (WorkflowInstanceStep step : overdueSteps) {
            escalateStep(step);
        }
    }

    private void escalateStep(WorkflowInstanceStep step) {
        WorkflowStep workflowStep = step.getWorkflowStep();
        
        if (workflowStep.getEscalationToRole() != null) {
            step.setEscalated(true);
            step.setEscalatedAt(LocalDateTime.now());
            step.setAssignedRole(workflowStep.getEscalationToRole());
            
            workflowInstanceStepRepository.save(step);
            
            // Send escalation notification
            notifyEscalation(step);
        }
    }

    private void notifyStepAssignment(WorkflowInstanceStep step) {
        // Implementation depends on your notification service
        try {
            String message = String.format("New workflow step assigned: %s", 
                step.getWorkflowStep().getStepName());
            
            if (step.getAssignedTo() != null) {
                // implement your notification send method for UUID user ids
            }
        } catch (Exception e) {
            // Log notification failure
        }
    }

    private void notifyWorkflowCompletion(WorkflowInstance instance, String status) {
        try {
            String message = String.format("Workflow %s: %s", status.toLowerCase(), 
                instance.getWorkflowDefinition().getName());
            
            // implement your notification send method for UUID user ids
        } catch (Exception e) {
            // Log notification failure
        }
    }

    private void notifyEscalation(WorkflowInstanceStep step) {
        try {
            String message = String.format("Workflow step escalated: %s", 
                step.getWorkflowStep().getStepName());
            
            // TODO: Implement role-based notification system
            System.out.println("Escalation notification: " + message);
        } catch (Exception e) {
            // Log notification failure
        }
    }
}
