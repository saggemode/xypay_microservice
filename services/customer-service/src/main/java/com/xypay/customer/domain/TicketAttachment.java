package com.xypay.customer.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_attachments")
public class TicketAttachment {
    
    public enum AttachmentType {
        IMAGE("image", "Image"),
        DOCUMENT("document", "Document"),
        VIDEO("video", "Video"),
        AUDIO("audio", "Audio"),
        OTHER("other", "Other");
        
        private final String value;
        private final String displayName;
        
        AttachmentType(String value, String displayName) {
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
    
    @Column(name = "file_name", nullable = false)
    private String fileName;
    
    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;
    
    @Column(name = "file_path", nullable = false)
    private String filePath;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "mime_type")
    private String mimeType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "attachment_type")
    private AttachmentType attachmentType;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "uploaded_by")
    private String uploadedBy; // Username or system identifier
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public TicketAttachment() {}
    
    public TicketAttachment(SupportTicket ticket, String fileName, String originalFileName, 
                          String filePath, Long fileSize, String mimeType) {
        this.ticket = ticket;
        this.fileName = fileName;
        this.originalFileName = originalFileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.attachmentType = determineAttachmentType(mimeType);
    }
    
    private AttachmentType determineAttachmentType(String mimeType) {
        if (mimeType == null) return AttachmentType.OTHER;
        
        if (mimeType.startsWith("image/")) {
            return AttachmentType.IMAGE;
        } else if (mimeType.startsWith("video/")) {
            return AttachmentType.VIDEO;
        } else if (mimeType.startsWith("audio/")) {
            return AttachmentType.AUDIO;
        } else if (mimeType.contains("pdf") || mimeType.contains("document") || 
                   mimeType.contains("text") || mimeType.contains("spreadsheet")) {
            return AttachmentType.DOCUMENT;
        } else {
            return AttachmentType.OTHER;
        }
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public SupportTicket getTicket() { return ticket; }
    public void setTicket(SupportTicket ticket) { this.ticket = ticket; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    
    public AttachmentType getAttachmentType() { return attachmentType; }
    public void setAttachmentType(AttachmentType attachmentType) { this.attachmentType = attachmentType; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
