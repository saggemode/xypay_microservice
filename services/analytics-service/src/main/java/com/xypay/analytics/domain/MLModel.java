package com.xypay.analytics.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ml_models")
public class MLModel extends BaseEntity {
    
    @Column(name = "model_name", length = 100, nullable = false)
    private String modelName;
    
    @Column(name = "model_type", length = 50, nullable = false)
    private String modelType; // CREDIT_SCORING, FRAUD_DETECTION, CUSTOMER_SEGMENTATION, RISK_ASSESSMENT
    
    @Column(name = "model_version", length = 20, nullable = false)
    private String modelVersion;
    
    @Column(name = "model_data", columnDefinition = "TEXT")
    private String modelData; // Serialized model
    
    @Column(name = "training_data_size")
    private Long trainingDataSize;
    
    @Column(name = "accuracy")
    private Double accuracy;
    
    @Column(name = "precision_score")
    private Double precisionScore;
    
    @Column(name = "recall_score")
    private Double recallScore;
    
    @Column(name = "f1_score")
    private Double f1Score;
    
    @Column(name = "training_date", nullable = false)
    private LocalDateTime trainingDate;
    
    @Column(name = "is_active")
    private Boolean isActive = false;
    
    @Column(name = "deployment_date")
    private LocalDateTime deploymentDate;
    
    @Column(name = "performance_metrics", columnDefinition = "TEXT")
    private String performanceMetrics;
}