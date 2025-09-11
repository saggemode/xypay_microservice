package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "regulatory_compliance")
public class RegulatoryCompliance extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false)
    private Bank bank;
    
    @Column(name = "regulation_type")
    @Enumerated(EnumType.STRING)
    private RegulationType regulationType;
    
    @Column(name = "regulation_name", length = 200)
    private String regulationName;
    
    @Column(name = "jurisdiction", length = 100)
    private String jurisdiction;
    
    @Column(name = "compliance_status")
    @Enumerated(EnumType.STRING)
    private ComplianceStatus complianceStatus = ComplianceStatus.PENDING;
    
    @Column(name = "effective_date")
    private LocalDateTime effectiveDate;
    
    @Column(name = "last_assessment_date")
    private LocalDateTime lastAssessmentDate;
    
    @Column(name = "next_assessment_date")
    private LocalDateTime nextAssessmentDate;
    
    // Basel III Specific Fields
    @Column(name = "capital_adequacy_ratio", precision = 5, scale = 2)
    private BigDecimal capitalAdequacyRatio;
    
    @Column(name = "tier1_capital_ratio", precision = 5, scale = 2)
    private BigDecimal tier1CapitalRatio;
    
    @Column(name = "leverage_ratio", precision = 5, scale = 2)
    private BigDecimal leverageRatio;
    
    @Column(name = "liquidity_coverage_ratio", precision = 5, scale = 2)
    private BigDecimal liquidityCoverageRatio;
    
    @Column(name = "net_stable_funding_ratio", precision = 5, scale = 2)
    private BigDecimal netStableFundingRatio;
    
    // IFRS Specific Fields
    @Column(name = "ifrs_version", length = 20)
    private String ifrsVersion;
    
    @Column(name = "expected_credit_loss_model")
    private Boolean expectedCreditLossModel = false;
    
    @Column(name = "fair_value_measurement")
    private Boolean fairValueMeasurement = false;
    
    // AML/KYC Fields
    @Column(name = "aml_program_status")
    @Enumerated(EnumType.STRING)
    private ComplianceStatus amlProgramStatus = ComplianceStatus.PENDING;
    
    @Column(name = "kyc_program_status")
    @Enumerated(EnumType.STRING)
    private ComplianceStatus kycProgramStatus = ComplianceStatus.PENDING;
    
    @Column(name = "sanctions_screening_enabled")
    private Boolean sanctionsScreeningEnabled = true;
    
    @Column(name = "transaction_monitoring_enabled")
    private Boolean transactionMonitoringEnabled = true;
    
    // Reporting Requirements
    @Column(name = "reporting_frequency")
    @Enumerated(EnumType.STRING)
    private ReportingFrequency reportingFrequency = ReportingFrequency.MONTHLY;
    
    @Column(name = "last_report_date")
    private LocalDateTime lastReportDate;
    
    @Column(name = "next_report_due")
    private LocalDateTime nextReportDue;
    
    @Column(name = "automated_reporting")
    private Boolean automatedReporting = false;
    
    // Risk Management
    @Column(name = "risk_appetite_framework")
    private Boolean riskAppetiteFramework = false;
    
    @Column(name = "stress_testing_enabled")
    private Boolean stressTestingEnabled = false;
    
    @Column(name = "operational_risk_management")
    private Boolean operationalRiskManagement = false;
    
    // Documentation
    @Column(name = "policy_document_path", length = 500)
    private String policyDocumentPath;
    
    @Column(name = "compliance_officer", length = 100)
    private String complianceOfficer;
    
    @Column(name = "external_auditor", length = 100)
    private String externalAuditor;
    
    @Column(name = "notes", length = 1000)
    private String notes;
    
    public enum RegulationType {
        BASEL_III, IFRS_9, IFRS_17, AML_BSA, KYC_CDD, GDPR, SOX, DODD_FRANK, 
        MiFID_II, PCI_DSS, LOCAL_BANKING, CENTRAL_BANK, FATCA, CRS
    }
    
    public enum ComplianceStatus {
        PENDING, IN_PROGRESS, COMPLIANT, NON_COMPLIANT, UNDER_REVIEW, EXEMPTED
    }
    
    public enum ReportingFrequency {
        DAILY, WEEKLY, MONTHLY, QUARTERLY, SEMI_ANNUALLY, ANNUALLY, AD_HOC
    }
}
