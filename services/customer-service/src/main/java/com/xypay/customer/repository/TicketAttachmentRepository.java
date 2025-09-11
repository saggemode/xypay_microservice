package com.xypay.customer.repository;

import com.xypay.customer.domain.SupportTicket;
import com.xypay.customer.domain.TicketAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TicketAttachmentRepository extends JpaRepository<TicketAttachment, Long> {
    
    List<TicketAttachment> findByTicket(SupportTicket ticket);
    
    List<TicketAttachment> findByTicketOrderByCreatedAtAsc(SupportTicket ticket);
    
    @Query("SELECT a FROM TicketAttachment a WHERE a.ticket.id = :ticketId ORDER BY a.createdAt ASC")
    List<TicketAttachment> findByTicketId(@Param("ticketId") Long ticketId);
    
    @Query("SELECT a FROM TicketAttachment a WHERE a.ticket.id = :ticketId AND a.attachmentType = :attachmentType ORDER BY a.createdAt ASC")
    List<TicketAttachment> findByTicketIdAndAttachmentType(@Param("ticketId") Long ticketId, @Param("attachmentType") TicketAttachment.AttachmentType attachmentType);
    
    @Query("SELECT a FROM TicketAttachment a WHERE a.uploadedBy = :uploadedBy ORDER BY a.createdAt DESC")
    List<TicketAttachment> findByUploadedBy(@Param("uploadedBy") String uploadedBy);
    
    @Query("SELECT a FROM TicketAttachment a WHERE a.createdAt >= :startDate AND a.createdAt <= :endDate")
    List<TicketAttachment> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM TicketAttachment a WHERE a.mimeType = :mimeType")
    List<TicketAttachment> findByMimeType(@Param("mimeType") String mimeType);
    
    @Query("SELECT a FROM TicketAttachment a WHERE a.fileSize > :minSize")
    List<TicketAttachment> findByFileSizeGreaterThan(@Param("minSize") Long minSize);
    
    @Query("SELECT COUNT(a) FROM TicketAttachment a WHERE a.ticket.id = :ticketId")
    Long countAttachmentsByTicketId(@Param("ticketId") Long ticketId);
    
    @Query("SELECT SUM(a.fileSize) FROM TicketAttachment a WHERE a.ticket.id = :ticketId")
    Long getTotalFileSizeByTicketId(@Param("ticketId") Long ticketId);
}
