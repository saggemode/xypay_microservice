package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ml_model_training", indexes = {
    @Index(name = "idx_model_name", columnList = "model_name"),
    @Index(name = "idx_model_version", columnList = "version"),
    @Index(name = "idx_model_production", columnList = "is_production"),
    @Index(name = "idx_training_dates", columnList = "training_start, training_end")
})
public class MLModelTraining extends BaseEntity {
    
    public enum ModelType {
        CLASSIFICATION("classification", "Classification"),
        REGRESSION("regression", "Regression"),
        CLUSTERING("clustering", "Clustering"),
        ANOMALY_DETECTION("anomaly_detection", "Anomaly Detection");
        
        private final String code;
        private final String displayName;
        
        ModelType(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    @Column(name = "model_name", length = 100, nullable = false)
    private String modelName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "model_type", length = 50, nullable = false)
    private ModelType modelType;
    
    // Training data info
    @Column(name = "training_data_size", nullable = false)
    private Integer trainingDataSize;
    
    @Column(name = "features_used", columnDefinition = "JSON", nullable = false)
    private String featuresUsed;
    
    @Column(name = "training_start", nullable = false)
    private LocalDateTime trainingStart;
    
    @Column(name = "training_end")
    private LocalDateTime trainingEnd;
    
    // Model configuration
    @Column(name = "hyperparameters", columnDefinition = "JSON", nullable = false)
    private String hyperparameters;
    
    @Column(name = "model_architecture", columnDefinition = "JSON", nullable = false)
    private String modelArchitecture;
    
    // Performance metrics
    @Column(name = "accuracy")
    private Double accuracy;
    
    @Column(name = "precision")
    private Double precision;
    
    @Column(name = "recall")
    private Double recall;
    
    @Column(name = "f1_score")
    private Double f1Score;
    
    @Column(name = "auc_roc")
    private Double aucRoc;
    
    // Cross-validation results
    @Column(name = "cv_scores", columnDefinition = "JSON")
    private String cvScores;
    
    @Column(name = "validation_metrics", columnDefinition = "JSON")
    private String validationMetrics;
    
    // Model artifacts
    @Column(name = "model_file_path", length = 255, nullable = false)
    private String modelFilePath;
    
    @Column(name = "feature_importance", columnDefinition = "JSON", nullable = false)
    private String featureImportance;
    
    // Version control
    @Column(name = "version", length = 50, nullable = false)
    private String version;
    
    @Column(name = "is_production", nullable = false)
    private Boolean isProduction = false;
    
    // Constructors
    public MLModelTraining() {}
    
    public MLModelTraining(String modelName, ModelType modelType, Integer trainingDataSize, 
                          String featuresUsed, LocalDateTime trainingStart, String hyperparameters, 
                          String modelArchitecture, String modelFilePath, String featureImportance, 
                          String version) {
        this.modelName = modelName;
        this.modelType = modelType;
        this.trainingDataSize = trainingDataSize;
        this.featuresUsed = featuresUsed;
        this.trainingStart = trainingStart;
        this.hyperparameters = hyperparameters;
        this.modelArchitecture = modelArchitecture;
        this.modelFilePath = modelFilePath;
        this.featureImportance = featureImportance;
        this.version = version;
    }
}
