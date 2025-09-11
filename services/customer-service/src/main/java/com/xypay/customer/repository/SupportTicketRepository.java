package com.xypay.customer.repository;

import com.xypay.customer.domain.SupportTicket;
import com.xypay.customer.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    
    Optional<SupportTicket> findByTicketNumber(String ticketNumber);
    
    List<SupportTicket> findByCustomer(User customer);
    
    Page<SupportTicket> findByCustomer(User customer, Pageable pageable);
    
    List<SupportTicket> findByAssignedAgent(User agent);
    
    Page<SupportTicket> findByAssignedAgent(User agent, Pageable pageable);
    
    List<SupportTicket> findByStatus(SupportTicket.TicketStatus status);
    
    List<SupportTicket> findByPriority(SupportTicket.TicketPriority priority);
    
    List<SupportTicket> findByCategory(SupportTicket.TicketCategory category);
    
    List<SupportTicket> findByEscalated(Boolean escalated);
    
    @Query("SELECT t FROM SupportTicket t WHERE t.slaDeadline < :now AND t.status NOT IN ('RESOLVED', 'CLOSED')")
    List<SupportTicket> findOverdueTickets(@Param("now") LocalDateTime now);
    
    @Query("SELECT t FROM SupportTicket t WHERE t.createdAt >= :startDate AND t.createdAt <= :endDate")
    List<SupportTicket> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT t FROM SupportTicket t WHERE t.customer.id = :customerId AND t.status = :status")
    List<SupportTicket> findByCustomerIdAndStatus(@Param("customerId") Long customerId, @Param("status") SupportTicket.TicketStatus status);
    
    @Query("SELECT t FROM SupportTicket t WHERE t.assignedAgent.id = :agentId AND t.status = :status")
    List<SupportTicket> findByAgentIdAndStatus(@Param("agentId") Long agentId, @Param("status") SupportTicket.TicketStatus status);
    
    @Query("SELECT t FROM SupportTicket t WHERE t.status = 'OPEN' AND t.assignedAgent IS NULL")
    List<SupportTicket> findUnassignedTickets();
    
    @Query("SELECT COUNT(t) FROM SupportTicket t WHERE t.customer.id = :customerId AND t.status IN ('OPEN', 'IN_PROGRESS', 'PENDING_CUSTOMER')")
    Long countActiveTicketsByCustomer(@Param("customerId") Long customerId);
    
    @Query("SELECT COUNT(t) FROM SupportTicket t WHERE t.assignedAgent.id = :agentId AND t.status IN ('OPEN', 'IN_PROGRESS', 'PENDING_CUSTOMER')")
    Long countActiveTicketsByAgent(@Param("agentId") Long agentId);
    
    @Query("SELECT t FROM SupportTicket t WHERE t.subject LIKE %:searchTerm% OR t.description LIKE %:searchTerm% OR t.ticketNumber LIKE %:searchTerm%")
    List<SupportTicket> searchTickets(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT t FROM SupportTicket t WHERE t.customer.firstName LIKE %:name% OR t.customer.lastName LIKE %:name% OR t.customer.email LIKE %:name%")
    List<SupportTicket> findByCustomerNameOrEmail(@Param("name") String name);
    
    @Query("SELECT t FROM SupportTicket t WHERE t.createdAt >= :startDate AND t.createdAt <= :endDate AND t.status = :status")
    List<SupportTicket> findByDateRangeAndStatus(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("status") SupportTicket.TicketStatus status);
    
    @Query("SELECT t FROM SupportTicket t WHERE t.priority = :priority AND t.status NOT IN ('RESOLVED', 'CLOSED') ORDER BY t.createdAt ASC")
    List<SupportTicket> findOpenTicketsByPriority(@Param("priority") SupportTicket.TicketPriority priority);
    
    @Query("SELECT t FROM SupportTicket t WHERE t.customerSatisfactionRating IS NOT NULL ORDER BY t.customerSatisfactionRating DESC")
    List<SupportTicket> findTicketsWithRatings();
    
    @Query("SELECT AVG(t.customerSatisfactionRating) FROM SupportTicket t WHERE t.customerSatisfactionRating IS NOT NULL")
    Double getAverageCustomerSatisfaction();
    
    @Query("SELECT AVG(t.resolutionTimeMinutes) FROM SupportTicket t WHERE t.resolutionTimeMinutes IS NOT NULL")
    Double getAverageResolutionTime();
    
    @Query("SELECT COUNT(t) FROM SupportTicket t WHERE t.createdAt >= :startDate AND t.createdAt <= :endDate")
    Long countTicketsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(t) FROM SupportTicket t WHERE t.createdAt >= :startDate AND t.createdAt <= :endDate AND t.status = :status")
    Long countTicketsByDateRangeAndStatus(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("status") SupportTicket.TicketStatus status);
}
