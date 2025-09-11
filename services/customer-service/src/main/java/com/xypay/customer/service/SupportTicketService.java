package com.xypay.customer.service;

import com.xypay.customer.domain.*;
import com.xypay.customer.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class SupportTicketService {
    
    @Autowired
    private SupportTicketRepository supportTicketRepository;
    
    @Autowired
    private TicketCommentRepository ticketCommentRepository;
    
    @Autowired
    private TicketAttachmentRepository ticketAttachmentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private static final String UPLOAD_DIR = "uploads/tickets/";
    
    // Support Ticket Management
    public SupportTicket createTicket(Long customerId, String subject, String description, 
                                    SupportTicket.TicketCategory category, SupportTicket.TicketPriority priority) {
        User customer = userRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        SupportTicket ticket = new SupportTicket(customer, subject, description, category);
        ticket.setPriority(priority);
        
        SupportTicket savedTicket = supportTicketRepository.save(ticket);
        
        // Add initial system comment
        addComment(savedTicket.getId(), null, TicketComment.CommentType.SYSTEM, 
                  "Support ticket created automatically");
        
        return savedTicket;
    }
    
    public Optional<SupportTicket> getTicketById(Long ticketId) {
        return supportTicketRepository.findById(ticketId);
    }
    
    public Optional<SupportTicket> getTicketByNumber(String ticketNumber) {
        return supportTicketRepository.findByTicketNumber(ticketNumber);
    }
    
    public List<SupportTicket> getTicketsByCustomer(Long customerId) {
        User customer = userRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        return supportTicketRepository.findByCustomer(customer);
    }
    
    public Page<SupportTicket> getTicketsByCustomer(Long customerId, Pageable pageable) {
        User customer = userRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        return supportTicketRepository.findByCustomer(customer, pageable);
    }
    
    public List<SupportTicket> getTicketsByAgent(Long agentId) {
        User agent = userRepository.findById(agentId)
            .orElseThrow(() -> new RuntimeException("Agent not found"));
        return supportTicketRepository.findByAssignedAgent(agent);
    }
    
    public List<SupportTicket> getUnassignedTickets() {
        return supportTicketRepository.findUnassignedTickets();
    }
    
    public List<SupportTicket> getOverdueTickets() {
        return supportTicketRepository.findOverdueTickets(LocalDateTime.now());
    }
    
    public SupportTicket assignTicket(Long ticketId, Long agentId) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket not found"));
        
        User agent = userRepository.findById(agentId)
            .orElseThrow(() -> new RuntimeException("Agent not found"));
        
        ticket.assignToAgent(agent);
        
        // Add system comment
        addComment(ticketId, agentId, TicketComment.CommentType.SYSTEM, 
                  "Ticket assigned to agent: " + agent.getFirstName() + " " + agent.getLastName());
        
        return supportTicketRepository.save(ticket);
    }
    
    public SupportTicket updateTicketStatus(Long ticketId, SupportTicket.TicketStatus status) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket not found"));
        
        ticket.setStatus(status);
        
        // Add system comment
        addComment(ticketId, null, TicketComment.CommentType.SYSTEM, 
                  "Ticket status changed to: " + status.getDisplayName());
        
        return supportTicketRepository.save(ticket);
    }
    
    public SupportTicket updateTicketPriority(Long ticketId, SupportTicket.TicketPriority priority) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket not found"));
        
        ticket.setPriority(priority);
        
        // Add system comment
        addComment(ticketId, null, TicketComment.CommentType.SYSTEM, 
                  "Ticket priority changed to: " + priority.getDisplayName());
        
        return supportTicketRepository.save(ticket);
    }
    
    public SupportTicket resolveTicket(Long ticketId, String resolutionNotes) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket not found"));
        
        ticket.resolve(resolutionNotes);
        
        // Add system comment
        addComment(ticketId, null, TicketComment.CommentType.SYSTEM, 
                  "Ticket resolved with notes: " + resolutionNotes);
        
        return supportTicketRepository.save(ticket);
    }
    
    public SupportTicket closeTicket(Long ticketId) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket not found"));
        
        ticket.close();
        
        // Add system comment
        addComment(ticketId, null, TicketComment.CommentType.SYSTEM, 
                  "Ticket closed");
        
        return supportTicketRepository.save(ticket);
    }
    
    public SupportTicket escalateTicket(Long ticketId, String reason) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket not found"));
        
        ticket.escalate(reason);
        
        // Add system comment
        addComment(ticketId, null, TicketComment.CommentType.SYSTEM, 
                  "Ticket escalated. Reason: " + reason);
        
        return supportTicketRepository.save(ticket);
    }
    
    public SupportTicket addCustomerRating(Long ticketId, Integer rating, String feedback) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket not found"));
        
        ticket.addCustomerRating(rating, feedback);
        
        return supportTicketRepository.save(ticket);
    }
    
    // Comment Management
    public TicketComment addComment(Long ticketId, Long authorId, TicketComment.CommentType commentType, String content) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket not found"));
        
        User author = null;
        if (authorId != null) {
            author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Author not found"));
        }
        
        TicketComment comment = new TicketComment(ticket, author, commentType, content);
        TicketComment savedComment = ticketCommentRepository.save(comment);
        
        // Update first response time if this is the first agent response
        if (commentType == TicketComment.CommentType.AGENT && ticket.getFirstResponseTimeMinutes() == null) {
            ticket.setFirstResponseTimeMinutes(
                java.time.Duration.between(ticket.getCreatedAt(), LocalDateTime.now()).toMinutes()
            );
            supportTicketRepository.save(ticket);
        }
        
        return savedComment;
    }
    
    public List<TicketComment> getTicketComments(Long ticketId) {
        return ticketCommentRepository.findAllCommentsByTicketId(ticketId);
    }
    
    public List<TicketComment> getVisibleTicketComments(Long ticketId) {
        return ticketCommentRepository.findVisibleCommentsByTicketId(ticketId);
    }
    
    // Attachment Management
    public TicketAttachment addAttachment(Long ticketId, MultipartFile file, String description) throws IOException {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket not found"));
        
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String originalFileName = file.getOriginalFilename();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + fileExtension;
        Path filePath = uploadPath.resolve(fileName);
        
        // Save file
        Files.copy(file.getInputStream(), filePath);
        
        // Create attachment record
        TicketAttachment attachment = new TicketAttachment(
            ticket, fileName, originalFileName, filePath.toString(), 
            file.getSize(), file.getContentType()
        );
        attachment.setDescription(description);
        
        return ticketAttachmentRepository.save(attachment);
    }
    
    public List<TicketAttachment> getTicketAttachments(Long ticketId) {
        return ticketAttachmentRepository.findByTicketId(ticketId);
    }
    
    // Search and Filter
    public List<SupportTicket> searchTickets(String searchTerm) {
        return supportTicketRepository.searchTickets(searchTerm);
    }
    
    public List<SupportTicket> getTicketsByStatus(SupportTicket.TicketStatus status) {
        return supportTicketRepository.findByStatus(status);
    }
    
    public List<SupportTicket> getTicketsByPriority(SupportTicket.TicketPriority priority) {
        return supportTicketRepository.findByPriority(priority);
    }
    
    public List<SupportTicket> getTicketsByCategory(SupportTicket.TicketCategory category) {
        return supportTicketRepository.findByCategory(category);
    }
    
    public List<SupportTicket> getTicketsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return supportTicketRepository.findByDateRange(startDate, endDate);
    }
    
    // Analytics
    public Long getActiveTicketCountByCustomer(Long customerId) {
        return supportTicketRepository.countActiveTicketsByCustomer(customerId);
    }
    
    public Long getActiveTicketCountByAgent(Long agentId) {
        return supportTicketRepository.countActiveTicketsByAgent(agentId);
    }
    
    public Double getAverageCustomerSatisfaction() {
        return supportTicketRepository.getAverageCustomerSatisfaction();
    }
    
    public Double getAverageResolutionTime() {
        return supportTicketRepository.getAverageResolutionTime();
    }
    
    public Long getTicketCountByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return supportTicketRepository.countTicketsByDateRange(startDate, endDate);
    }
    
    public Long getTicketCountByDateRangeAndStatus(LocalDateTime startDate, LocalDateTime endDate, SupportTicket.TicketStatus status) {
        return supportTicketRepository.countTicketsByDateRangeAndStatus(startDate, endDate, status);
    }
    
    public List<SupportTicket> getTicketsWithRatings() {
        return supportTicketRepository.findTicketsWithRatings();
    }
}
