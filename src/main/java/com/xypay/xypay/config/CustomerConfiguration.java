package com.xypay.xypay.config;

import com.xypay.xypay.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "customer_configurations")
public class CustomerConfiguration extends BaseEntity {
    
    @Column(name = "customer_type")
    private String customerType; // INDIVIDUAL, CORPORATE, SME
    
    @Column(name = "kyc_level")
    private String kycLevel; // BASIC, STANDARD, ENHANCED
    
    @Column(name = "required_documents")
    private String requiredDocuments; // JSON format list of required documents
    
    @Column(name = "risk_rating")
    private String riskRating; // LOW, MEDIUM, HIGH
    
    
    @Column(name = "is_active")
    private Boolean isActive = true;
}