package com.xypay.customer.repository;

import com.xypay.customer.domain.SupportTicket;
import com.xypay.customer.domain.TicketComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {
    
    List<TicketComment> findByTicket(SupportTicket ticket);
    
    List<TicketComment> findByTicketOrderByCreatedAtAsc(SupportTicket ticket);
    
    @Query("SELECT c FROM TicketComment c WHERE c.ticket.id = :ticketId AND c.isVisibleToCustomer = true ORDER BY c.createdAt ASC")
    List<TicketComment> findVisibleCommentsByTicketId(@Param("ticketId") Long ticketId);
    
    @Query("SELECT c FROM TicketComment c WHERE c.ticket.id = :ticketId ORDER BY c.createdAt ASC")
    List<TicketComment> findAllCommentsByTicketId(@Param("ticketId") Long ticketId);
    
    @Query("SELECT c FROM TicketComment c WHERE c.ticket.id = :ticketId AND c.commentType = :commentType ORDER BY c.createdAt ASC")
    List<TicketComment> findByTicketIdAndCommentType(@Param("ticketId") Long ticketId, @Param("commentType") TicketComment.CommentType commentType);
    
    @Query("SELECT c FROM TicketComment c WHERE c.ticket.id = :ticketId AND c.isInternal = :isInternal ORDER BY c.createdAt ASC")
    List<TicketComment> findByTicketIdAndIsInternal(@Param("ticketId") Long ticketId, @Param("isInternal") Boolean isInternal);
    
    @Query("SELECT c FROM TicketComment c WHERE c.author.id = :authorId ORDER BY c.createdAt DESC")
    List<TicketComment> findByAuthorId(@Param("authorId") Long authorId);
    
    @Query("SELECT c FROM TicketComment c WHERE c.createdAt >= :startDate AND c.createdAt <= :endDate")
    List<TicketComment> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(c) FROM TicketComment c WHERE c.ticket.id = :ticketId")
    Long countCommentsByTicketId(@Param("ticketId") Long ticketId);
    
    @Query("SELECT COUNT(c) FROM TicketComment c WHERE c.ticket.id = :ticketId AND c.commentType = 'CUSTOMER'")
    Long countCustomerCommentsByTicketId(@Param("ticketId") Long ticketId);
    
    @Query("SELECT COUNT(c) FROM TicketComment c WHERE c.ticket.id = :ticketId AND c.commentType = 'AGENT'")
    Long countAgentCommentsByTicketId(@Param("ticketId") Long ticketId);
}
