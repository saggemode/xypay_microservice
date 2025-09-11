package com.xypay.analytics.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "credit_scores")
public class CreditScore extends BaseEntity {
    
    @Column(name = "customer_id", nullable = false)
    private Long customerId;
    
    @Column(name = "score", precision = 10, scale = 2, nullable = false)
    private BigDecimal score;
    
    @Column(name = "score_date", nullable = false)
    private LocalDateTime scoreDate;
    
    @Column(name = "risk_category", length = 20)
    private String riskCategory;
    
    @Column(name = "default_probability", precision = 10, scale = 4)
    private BigDecimal defaultProbability;
    
    @ElementCollection
    @CollectionTable(name = "credit_score_components", joinColumns = @JoinColumn(name = "credit_score_id"))
    @MapKeyColumn(name = "component_name")
    @Column(name = "component_value")
    private Map<String, BigDecimal> scoreComponents;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "model_version", length = 20)
    private String modelVersion = "1.0";
}