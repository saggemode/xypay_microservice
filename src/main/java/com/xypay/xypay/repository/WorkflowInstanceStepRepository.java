package com.xypay.xypay.repository;

import com.xypay.xypay.domain.WorkflowInstanceStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface WorkflowInstanceStepRepository extends JpaRepository<WorkflowInstanceStep, Long> {
    
    List<WorkflowInstanceStep> findByWorkflowInstanceIdAndStatus(Long workflowInstanceId, String status);
    
    List<WorkflowInstanceStep> findByAssignedToAndStatus(Long assignedTo, String status);
    
    List<WorkflowInstanceStep> findByAssignedRoleAndStatus(String assignedRole, String status);
    
    @Query("SELECT wis FROM WorkflowInstanceStep wis WHERE wis.status = 'PENDING' AND wis.createdAt < :escalationTime AND wis.escalated = false")
    List<WorkflowInstanceStep> findStepsForEscalation(@Param("escalationTime") LocalDateTime escalationTime);
    
    @Query("SELECT wis FROM WorkflowInstanceStep wis WHERE wis.workflowInstance.id = :instanceId ORDER BY wis.workflowStep.stepOrder")
    List<WorkflowInstanceStep> findByWorkflowInstanceIdOrderByStepOrder(@Param("instanceId") Long instanceId);
}
