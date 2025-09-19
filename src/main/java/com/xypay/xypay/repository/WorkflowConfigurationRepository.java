package com.xypay.xypay.repository;

import com.xypay.xypay.config.WorkflowConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

import java.util.List;
import java.util.Optional;


@Repository
public interface WorkflowConfigurationRepository extends JpaRepository<WorkflowConfiguration, UUID> {
    Optional<WorkflowConfiguration> findByWorkflowName(String workflowName);
    List<WorkflowConfiguration> findByProcessTypeAndIsActiveTrue(String processType);
    List<WorkflowConfiguration> findByIsActiveTrue();
}