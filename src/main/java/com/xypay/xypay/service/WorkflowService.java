package com.xypay.xypay.service;

import com.xypay.xypay.domain.WorkflowDefinition;
import com.xypay.xypay.repository.WorkflowDefinitionRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class WorkflowService {
    private final WorkflowDefinitionRepository repo;
    public WorkflowService(WorkflowDefinitionRepository repo) { this.repo = repo; }

    public WorkflowDefinition save(WorkflowDefinition def) { return repo.save(def); }
    public List<WorkflowDefinition> findByOwner(String owner) { return repo.findByOwner(owner); }
    public List<WorkflowDefinition> findAll() { return repo.findAll(); }
    public WorkflowDefinition findById(UUID id) { return repo.findById(id).orElse(null); }
}
