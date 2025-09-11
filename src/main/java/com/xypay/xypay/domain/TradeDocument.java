package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "trade_documents")
public class TradeDocument extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_finance_id", nullable = false)
    private TradeFinance tradeFinance;
    
    @Column(name = "document_type")
    @Enumerated(EnumType.STRING)
    private DocumentType documentType;
    
    @Column(name = "document_name", length = 200)
    private String documentName;
    
    @Column(name = "document_number", length = 100)
    private String documentNumber;
    
    @Column(name = "document_date")
    private LocalDateTime documentDate;
    
    @Column(name = "issuer", length = 200)
    private String issuer;
    
    @Column(name = "file_path", length = 500)
    private String filePath;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "mime_type", length = 100)
    private String mimeType;
    
    @Column(name = "is_required")
    private Boolean isRequired = true;
    
    @Column(name = "is_received")
    private Boolean isReceived = false;
    
    @Column(name = "received_date")
    private LocalDateTime receivedDate;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private DocumentStatus status = DocumentStatus.PENDING;
    
    @Column(name = "discrepancies", length = 1000)
    private String discrepancies;
    
    @Column(name = "reviewed_by")
    private Long reviewedBy;
    
    @Column(name = "review_date")
    private LocalDateTime reviewDate;
    
    @Column(name = "notes", length = 500)
    private String notes;
    
    public enum DocumentType {
        COMMERCIAL_INVOICE, PACKING_LIST, BILL_OF_LADING, AIRWAY_BILL, 
        INSURANCE_CERTIFICATE, CERTIFICATE_OF_ORIGIN, INSPECTION_CERTIFICATE,
        WEIGHT_CERTIFICATE, QUALITY_CERTIFICATE, BENEFICIARY_CERTIFICATE,
        DRAFT, PROMISSORY_NOTE, OTHER
    }
    
    public enum DocumentStatus {
        PENDING, RECEIVED, ACCEPTED, REJECTED, DISCREPANT
    }
}
