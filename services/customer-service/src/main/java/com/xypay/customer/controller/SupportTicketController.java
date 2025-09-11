package com.xypay.customer.controller;

import com.xypay.customer.domain.*;
import com.xypay.customer.service.SupportTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/support-tickets")
public class SupportTicketController {
    
    @Autowired
    private SupportTicketService supportTicketService;
    
    // Ticket Management Endpoints
    @PostMapping
    public ResponseEntity<SupportTicket> createTicket(@RequestBody Map<String, Object> ticketData) {
        Long customerId = Long.valueOf(ticketData.get("customerId").toString());
        String subject = (String) ticketData.get("subject");
        String description = (String) ticketData.get("description");
        SupportTicket.TicketCategory category = SupportTicket.TicketCategory.valueOf(
            ticketData.get("category").toString().toUpperCase());
        SupportTicket.TicketPriority priority = SupportTicket.TicketPriority.valueOf(
            ticketData.getOrDefault("priority", "MEDIUM").toString().toUpperCase());
        
        SupportTicket ticket = supportTicketService.createTicket(customerId, subject, description, category, priority);
        return ResponseEntity.ok(ticket);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<SupportTicket> getTicket(@PathVariable Long id) {
        Optional<SupportTicket> ticket = supportTicketService.getTicketById(id);
        return ticket.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/number/{ticketNumber}")
    public ResponseEntity<SupportTicket> getTicketByNumber(@PathVariable String ticketNumber) {
        Optional<SupportTicket> ticket = supportTicketService.getTicketByNumber(ticketNumber);
        return ticket.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<SupportTicket>> getTicketsByCustomer(@PathVariable Long customerId) {
        List<SupportTicket> tickets = supportTicketService.getTicketsByCustomer(customerId);
        return ResponseEntity.ok(tickets);
    }
    
    @GetMapping("/customer/{customerId}/paged")
    public ResponseEntity<Page<SupportTicket>> getTicketsByCustomerPaged(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SupportTicket> tickets = supportTicketService.getTicketsByCustomer(customerId, pageable);
        return ResponseEntity.ok(tickets);
    }
    
    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<SupportTicket>> getTicketsByAgent(@PathVariable Long agentId) {
        List<SupportTicket> tickets = supportTicketService.getTicketsByAgent(agentId);
        return ResponseEntity.ok(tickets);
    }
    
    @GetMapping("/unassigned")
    public ResponseEntity<List<SupportTicket>> getUnassignedTickets() {
        List<SupportTicket> tickets = supportTicketService.getUnassignedTickets();
        return ResponseEntity.ok(tickets);
    }
    
    @GetMapping("/overdue")
    public ResponseEntity<List<SupportTicket>> getOverdueTickets() {
        List<SupportTicket> tickets = supportTicketService.getOverdueTickets();
        return ResponseEntity.ok(tickets);
    }
    
    @PutMapping("/{id}/assign")
    public ResponseEntity<SupportTicket> assignTicket(@PathVariable Long id, @RequestBody Map<String, Long> assignData) {
        Long agentId = assignData.get("agentId");
        SupportTicket ticket = supportTicketService.assignTicket(id, agentId);
        return ResponseEntity.ok(ticket);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<SupportTicket> updateTicketStatus(@PathVariable Long id, @RequestBody Map<String, String> statusData) {
        SupportTicket.TicketStatus status = SupportTicket.TicketStatus.valueOf(statusData.get("status").toUpperCase());
        SupportTicket ticket = supportTicketService.updateTicketStatus(id, status);
        return ResponseEntity.ok(ticket);
    }
    
    @PutMapping("/{id}/priority")
    public ResponseEntity<SupportTicket> updateTicketPriority(@PathVariable Long id, @RequestBody Map<String, String> priorityData) {
        SupportTicket.TicketPriority priority = SupportTicket.TicketPriority.valueOf(priorityData.get("priority").toUpperCase());
        SupportTicket ticket = supportTicketService.updateTicketPriority(id, priority);
        return ResponseEntity.ok(ticket);
    }
    
    @PutMapping("/{id}/resolve")
    public ResponseEntity<SupportTicket> resolveTicket(@PathVariable Long id, @RequestBody Map<String, String> resolutionData) {
        String resolutionNotes = resolutionData.get("resolutionNotes");
        SupportTicket ticket = supportTicketService.resolveTicket(id, resolutionNotes);
        return ResponseEntity.ok(ticket);
    }
    
    @PutMapping("/{id}/close")
    public ResponseEntity<SupportTicket> closeTicket(@PathVariable Long id) {
        SupportTicket ticket = supportTicketService.closeTicket(id);
        return ResponseEntity.ok(ticket);
    }
    
    @PutMapping("/{id}/escalate")
    public ResponseEntity<SupportTicket> escalateTicket(@PathVariable Long id, @RequestBody Map<String, String> escalationData) {
        String reason = escalationData.get("reason");
        SupportTicket ticket = supportTicketService.escalateTicket(id, reason);
        return ResponseEntity.ok(ticket);
    }
    
    @PutMapping("/{id}/rating")
    public ResponseEntity<SupportTicket> addCustomerRating(@PathVariable Long id, @RequestBody Map<String, Object> ratingData) {
        Integer rating = Integer.valueOf(ratingData.get("rating").toString());
        String feedback = (String) ratingData.get("feedback");
        SupportTicket ticket = supportTicketService.addCustomerRating(id, rating, feedback);
        return ResponseEntity.ok(ticket);
    }
    
    // Comment Management Endpoints
    @PostMapping("/{id}/comments")
    public ResponseEntity<TicketComment> addComment(@PathVariable Long id, @RequestBody Map<String, Object> commentData) {
        Long authorId = commentData.get("authorId") != null ? Long.valueOf(commentData.get("authorId").toString()) : null;
        TicketComment.CommentType commentType = TicketComment.CommentType.valueOf(
            commentData.get("commentType").toString().toUpperCase());
        String content = (String) commentData.get("content");
        
        TicketComment comment = supportTicketService.addComment(id, authorId, commentType, content);
        return ResponseEntity.ok(comment);
    }
    
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<TicketComment>> getTicketComments(@PathVariable Long id) {
        List<TicketComment> comments = supportTicketService.getTicketComments(id);
        return ResponseEntity.ok(comments);
    }
    
    @GetMapping("/{id}/comments/visible")
    public ResponseEntity<List<TicketComment>> getVisibleTicketComments(@PathVariable Long id) {
        List<TicketComment> comments = supportTicketService.getVisibleTicketComments(id);
        return ResponseEntity.ok(comments);
    }
    
    // Attachment Management Endpoints
    @PostMapping("/{id}/attachments")
    public ResponseEntity<TicketAttachment> addAttachment(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description) {
        try {
            TicketAttachment attachment = supportTicketService.addAttachment(id, file, description);
            return ResponseEntity.ok(attachment);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}/attachments")
    public ResponseEntity<List<TicketAttachment>> getTicketAttachments(@PathVariable Long id) {
        List<TicketAttachment> attachments = supportTicketService.getTicketAttachments(id);
        return ResponseEntity.ok(attachments);
    }
    
    // Search and Filter Endpoints
    @GetMapping("/search")
    public ResponseEntity<List<SupportTicket>> searchTickets(@RequestParam String q) {
        List<SupportTicket> tickets = supportTicketService.searchTickets(q);
        return ResponseEntity.ok(tickets);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<SupportTicket>> getTicketsByStatus(@PathVariable String status) {
        SupportTicket.TicketStatus ticketStatus = SupportTicket.TicketStatus.valueOf(status.toUpperCase());
        List<SupportTicket> tickets = supportTicketService.getTicketsByStatus(ticketStatus);
        return ResponseEntity.ok(tickets);
    }
    
    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<SupportTicket>> getTicketsByPriority(@PathVariable String priority) {
        SupportTicket.TicketPriority ticketPriority = SupportTicket.TicketPriority.valueOf(priority.toUpperCase());
        List<SupportTicket> tickets = supportTicketService.getTicketsByPriority(ticketPriority);
        return ResponseEntity.ok(tickets);
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<List<SupportTicket>> getTicketsByCategory(@PathVariable String category) {
        SupportTicket.TicketCategory ticketCategory = SupportTicket.TicketCategory.valueOf(category.toUpperCase());
        List<SupportTicket> tickets = supportTicketService.getTicketsByCategory(ticketCategory);
        return ResponseEntity.ok(tickets);
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<List<SupportTicket>> getTicketsByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);
        List<SupportTicket> tickets = supportTicketService.getTicketsByDateRange(start, end);
        return ResponseEntity.ok(tickets);
    }
    
    // Analytics Endpoints
    @GetMapping("/analytics/customer/{customerId}/active-count")
    public ResponseEntity<Map<String, Long>> getActiveTicketCountByCustomer(@PathVariable Long customerId) {
        Long count = supportTicketService.getActiveTicketCountByCustomer(customerId);
        return ResponseEntity.ok(Map.of("activeTicketCount", count));
    }
    
    @GetMapping("/analytics/agent/{agentId}/active-count")
    public ResponseEntity<Map<String, Long>> getActiveTicketCountByAgent(@PathVariable Long agentId) {
        Long count = supportTicketService.getActiveTicketCountByAgent(agentId);
        return ResponseEntity.ok(Map.of("activeTicketCount", count));
    }
    
    @GetMapping("/analytics/satisfaction/average")
    public ResponseEntity<Map<String, Double>> getAverageCustomerSatisfaction() {
        Double average = supportTicketService.getAverageCustomerSatisfaction();
        return ResponseEntity.ok(Map.of("averageSatisfaction", average != null ? average : 0.0));
    }
    
    @GetMapping("/analytics/resolution/average-time")
    public ResponseEntity<Map<String, Double>> getAverageResolutionTime() {
        Double average = supportTicketService.getAverageResolutionTime();
        return ResponseEntity.ok(Map.of("averageResolutionTimeMinutes", average != null ? average : 0.0));
    }
    
    @GetMapping("/analytics/count/date-range")
    public ResponseEntity<Map<String, Long>> getTicketCountByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);
        Long count = supportTicketService.getTicketCountByDateRange(start, end);
        return ResponseEntity.ok(Map.of("ticketCount", count));
    }
    
    @GetMapping("/analytics/ratings")
    public ResponseEntity<List<SupportTicket>> getTicketsWithRatings() {
        List<SupportTicket> tickets = supportTicketService.getTicketsWithRatings();
        return ResponseEntity.ok(tickets);
    }
    
    // Enum Endpoints for Frontend
    @GetMapping("/enums/status")
    public ResponseEntity<SupportTicket.TicketStatus[]> getTicketStatuses() {
        return ResponseEntity.ok(SupportTicket.TicketStatus.values());
    }
    
    @GetMapping("/enums/priority")
    public ResponseEntity<SupportTicket.TicketPriority[]> getTicketPriorities() {
        return ResponseEntity.ok(SupportTicket.TicketPriority.values());
    }
    
    @GetMapping("/enums/category")
    public ResponseEntity<SupportTicket.TicketCategory[]> getTicketCategories() {
        return ResponseEntity.ok(SupportTicket.TicketCategory.values());
    }
    
    @GetMapping("/enums/comment-type")
    public ResponseEntity<TicketComment.CommentType[]> getCommentTypes() {
        return ResponseEntity.ok(TicketComment.CommentType.values());
    }
}
