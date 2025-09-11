package com.xypay.xypay.repository;

import com.xypay.xypay.domain.WorkflowDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, Long> {
    List<WorkflowDefinition> findByOwner(String owner);
    
    List<WorkflowDefinition> findByWorkflowTypeAndIsActiveTrue(String workflowType);
    
    Optional<WorkflowDefinition> findByNameAndIsActiveTrue(String name);
    
    @Query("SELECT wd FROM WorkflowDefinition wd WHERE wd.isActive = true ORDER BY wd.name")
    List<WorkflowDefinition> findAllActiveWorkflows();
    
    @Query("SELECT wd FROM WorkflowDefinition wd WHERE wd.workflowType = :type AND wd.isActive = true ORDER BY wd.version DESC")
    List<WorkflowDefinition> findLatestVersionByType(@Param("type") String workflowType);
}
