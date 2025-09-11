package com.xypay.xypay.repository;

import com.xypay.xypay.domain.WorkflowInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, Long> {
    
    List<WorkflowInstance> findByEntityTypeAndEntityId(String entityType, Long entityId);
    
    List<WorkflowInstance> findByStatus(String status);
    
    List<WorkflowInstance> findByInitiatedBy(Long userId);
    
    @Query("SELECT wi FROM WorkflowInstance wi WHERE wi.status IN ('PENDING', 'IN_PROGRESS') ORDER BY wi.priority DESC, wi.createdAt ASC")
    List<WorkflowInstance> findPendingWorkflows();
    
    @Query("SELECT wi FROM WorkflowInstance wi JOIN wi.instanceSteps wis WHERE wis.assignedTo = :userId AND wis.status = 'PENDING'")
    List<WorkflowInstance> findWorkflowsAssignedToUser(@Param("userId") Long userId);
    
    @Query("SELECT wi FROM WorkflowInstance wi JOIN wi.instanceSteps wis WHERE wis.assignedRole = :role AND wis.status = 'PENDING'")
    List<WorkflowInstance> findWorkflowsAssignedToRole(@Param("role") String role);
    
    Optional<WorkflowInstance> findByEntityTypeAndEntityIdAndStatus(String entityType, Long entityId, String status);
}
