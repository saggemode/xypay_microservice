package com.xypay.analytics.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "customer_segments")
public class CustomerSegment extends BaseEntity {
    
    @Column(name = "customer_id", nullable = false)
    private Long customerId;
    
    @Column(name = "segment_type", length = 20, nullable = false)
    private String segmentType;
    
    @Column(name = "segment_date", nullable = false)
    private LocalDateTime segmentDate;
    
    @Column(name = "customer_value", precision = 19, scale = 2)
    private BigDecimal customerValue;
    
    @Column(name = "behavior_score", precision = 5, scale = 4)
    private BigDecimal behaviorScore;
    
    @Column(name = "segment_confidence", precision = 5, scale = 4)
    private BigDecimal segmentConfidence;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "model_version", length = 20)
    private String modelVersion = "1.0";
}
