package com.xypay.customer.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_comments")
public class TicketComment {
    
    public enum CommentType {
        CUSTOMER("customer", "Customer"),
        AGENT("agent", "Agent"),
        SYSTEM("system", "System"),
        INTERNAL("internal", "Internal Note");
        
        private final String value;
        private final String displayName;
        
        CommentType(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }
        
        public String getValue() { return value; }
        public String getDisplayName() { return displayName; }
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private SupportTicket ticket;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "comment_type", nullable = false)
    private CommentType commentType;
    
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(name = "is_internal", nullable = false)
    private Boolean isInternal = false;
    
    @Column(name = "is_visible_to_customer", nullable = false)
    private Boolean isVisibleToCustomer = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public TicketComment() {}
    
    public TicketComment(SupportTicket ticket, User author, CommentType commentType, String content) {
        this.ticket = ticket;
        this.author = author;
        this.commentType = commentType;
        this.content = content;
        this.isInternal = commentType == CommentType.INTERNAL;
        this.isVisibleToCustomer = commentType != CommentType.INTERNAL;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public SupportTicket getTicket() { return ticket; }
    public void setTicket(SupportTicket ticket) { this.ticket = ticket; }
    
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
    
    public CommentType getCommentType() { return commentType; }
    public void setCommentType(CommentType commentType) { this.commentType = commentType; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public Boolean getIsInternal() { return isInternal; }
    public void setIsInternal(Boolean isInternal) { this.isInternal = isInternal; }
    
    public Boolean getIsVisibleToCustomer() { return isVisibleToCustomer; }
    public void setIsVisibleToCustomer(Boolean isVisibleToCustomer) { this.isVisibleToCustomer = isVisibleToCustomer; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
