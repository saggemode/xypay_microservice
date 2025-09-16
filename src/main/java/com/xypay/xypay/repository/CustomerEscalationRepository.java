package com.xypay.xypay.repository;

import com.xypay.xypay.domain.CustomerEscalation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CustomerEscalationRepository extends JpaRepository<CustomerEscalation, UUID> {
    
    List<CustomerEscalation> findByStatusOrderByPriorityDescCreatedAtDesc(CustomerEscalation.EscalationStatus status);
    
    List<CustomerEscalation> findByCustomerOrderByCreatedAtDesc(UUID customerId);
    
    List<CustomerEscalation> findByCreatedByOrderByCreatedAtDesc(UUID createdById);
    
    List<CustomerEscalation> findByAssignedToOrderByCreatedAtDesc(UUID assignedToId);
    
    List<CustomerEscalation> findByPriorityOrderByCreatedAtDesc(CustomerEscalation.Priority priority);
    
    @Query("SELECT ce FROM CustomerEscalation ce WHERE ce.status = 'OPEN' ORDER BY ce.priority DESC, ce.createdAt DESC")
    List<CustomerEscalation> findOpenEscalations();
    
    @Query("SELECT ce FROM CustomerEscalation ce WHERE ce.status = 'IN_PROGRESS' AND ce.assignedTo.id = :staffId ORDER BY ce.priority DESC, ce.createdAt DESC")
    List<CustomerEscalation> findAssignedEscalations(@Param("staffId") UUID staffId);
}
