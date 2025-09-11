package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "trade_finance")
public class TradeFinance extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false)
    private Bank bank;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;
    
    @Column(name = "reference_number", length = 50, unique = true, nullable = false)
    private String referenceNumber;
    
    @Column(name = "trade_type")
    @Enumerated(EnumType.STRING)
    private TradeType tradeType;
    
    @Column(name = "instrument_type")
    @Enumerated(EnumType.STRING)
    private InstrumentType instrumentType;
    
    @Column(name = "amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;
    
    @Column(name = "currency_code", length = 3)
    private String currencyCode;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private TradeStatus status = TradeStatus.DRAFT;
    
    // Letter of Credit specific fields
    @Column(name = "lc_number", length = 50)
    private String lcNumber;
    
    @Column(name = "applicant_name", length = 200)
    private String applicantName;
    
    @Column(name = "beneficiary_name", length = 200)
    private String beneficiaryName;
    
    @Column(name = "beneficiary_bank", length = 200)
    private String beneficiaryBank;
    
    @Column(name = "advising_bank", length = 200)
    private String advisingBank;
    
    @Column(name = "confirming_bank", length = 200)
    private String confirmingBank;
    
    @Column(name = "issue_date")
    private LocalDateTime issueDate;
    
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    
    @Column(name = "shipment_date")
    private LocalDateTime shipmentDate;
    
    @Column(name = "presentation_period_days")
    private Integer presentationPeriodDays;
    
    // Guarantee specific fields
    @Column(name = "guarantee_type")
    @Enumerated(EnumType.STRING)
    private GuaranteeType guaranteeType;
    
    @Column(name = "principal_name", length = 200)
    private String principalName;
    
    @Column(name = "beneficiary_address", length = 500)
    private String beneficiaryAddress;
    
    @Column(name = "guarantee_text", length = 2000)
    private String guaranteeText;
    
    @Column(name = "claim_expiry_date")
    private LocalDateTime claimExpiryDate;
    
    // Collection specific fields
    @Column(name = "collection_type")
    @Enumerated(EnumType.STRING)
    private CollectionType collectionType;
    
    @Column(name = "drawer_name", length = 200)
    private String drawerName;
    
    @Column(name = "drawee_name", length = 200)
    private String draweeName;
    
    @Column(name = "collecting_bank", length = 200)
    private String collectingBank;
    
    @Column(name = "presenting_bank", length = 200)
    private String presentingBank;
    
    // Document details
    @Column(name = "documents_required", length = 1000)
    private String documentsRequired;
    
    @Column(name = "documents_received", length = 1000)
    private String documentsReceived;
    
    @Column(name = "document_discrepancies", length = 1000)
    private String documentDiscrepancies;
    
    // Financial details
    @Column(name = "margin_percentage", precision = 5, scale = 2)
    private BigDecimal marginPercentage = BigDecimal.ZERO;
    
    @Column(name = "margin_amount", precision = 19, scale = 2)
    private BigDecimal marginAmount = BigDecimal.ZERO;
    
    @Column(name = "commission_rate", precision = 5, scale = 4)
    private BigDecimal commissionRate = BigDecimal.ZERO;
    
    @Column(name = "commission_amount", precision = 19, scale = 2)
    private BigDecimal commissionAmount = BigDecimal.ZERO;
    
    @Column(name = "charges", precision = 19, scale = 2)
    private BigDecimal charges = BigDecimal.ZERO;
    
    // Risk and compliance
    @Column(name = "country_risk_rating")
    @Enumerated(EnumType.STRING)
    private RiskRating countryRiskRating = RiskRating.LOW;
    
    @Column(name = "customer_risk_rating")
    @Enumerated(EnumType.STRING)
    private RiskRating customerRiskRating = RiskRating.LOW;
    
    @Column(name = "sanctions_screening_status")
    @Enumerated(EnumType.STRING)
    private ScreeningStatus sanctionsScreeningStatus = ScreeningStatus.PENDING;
    
    @Column(name = "aml_screening_status")
    @Enumerated(EnumType.STRING)
    private ScreeningStatus amlScreeningStatus = ScreeningStatus.PENDING;
    
    // Workflow and approval
    @Column(name = "approval_workflow_id")
    private Long approvalWorkflowId;
    
    @Column(name = "approved_by")
    private Long approvedBy;
    
    @Column(name = "approval_date")
    private LocalDateTime approvalDate;
    
    // Swift messaging
    @Column(name = "swift_message_type", length = 10)
    private String swiftMessageType; // MT700, MT760, etc.
    
    @Column(name = "swift_reference", length = 50)
    private String swiftReference;
    
    @Column(name = "swift_sent_date")
    private LocalDateTime swiftSentDate;
    
    // Islamic banking compliance
    @Column(name = "sharia_compliant")
    private Boolean shariaCompliant = false;
    
    @Column(name = "islamic_structure")
    @Enumerated(EnumType.STRING)
    private IslamicStructure islamicStructure;
    
    // Relationships
    @OneToMany(mappedBy = "tradeFinance", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TradeDocument> documents;
    
    @OneToMany(mappedBy = "tradeFinance", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TradeAmendment> amendments;
    
    public enum TradeType {
        IMPORT, EXPORT, DOMESTIC
    }
    
    public enum InstrumentType {
        LETTER_OF_CREDIT, STANDBY_LC, GUARANTEE, DOCUMENTARY_COLLECTION, 
        TRADE_LOAN, EXPORT_FINANCING, IMPORT_FINANCING
    }
    
    public enum TradeStatus {
        DRAFT, SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, ISSUED, 
        ADVISED, CONFIRMED, DOCUMENTS_PRESENTED, DOCUMENTS_ACCEPTED, 
        DOCUMENTS_REJECTED, PAID, EXPIRED, CANCELLED, CLOSED
    }
    
    public enum GuaranteeType {
        BID_BOND, PERFORMANCE_GUARANTEE, ADVANCE_PAYMENT_GUARANTEE, 
        RETENTION_GUARANTEE, FINANCIAL_GUARANTEE, CUSTOMS_GUARANTEE
    }
    
    public enum CollectionType {
        DOCUMENTS_AGAINST_PAYMENT, DOCUMENTS_AGAINST_ACCEPTANCE, CLEAN_COLLECTION
    }
    
    public enum RiskRating {
        LOW, MEDIUM, HIGH, VERY_HIGH
    }
    
    public enum ScreeningStatus {
        PENDING, CLEARED, FLAGGED, BLOCKED
    }
    
    public enum IslamicStructure {
        MURABAHA, WAKALA, MUSHARAKA, ISTISNA
    }
}
