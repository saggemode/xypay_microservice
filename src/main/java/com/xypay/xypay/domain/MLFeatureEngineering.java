package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ml_feature_engineering", indexes = {
    @Index(name = "idx_feature_type", columnList = "feature_type"),
    @Index(name = "idx_feature_importance", columnList = "importance_score")
})
public class MLFeatureEngineering extends BaseEntity {
    
    public enum FeatureType {
        NUMERICAL("numerical", "Numerical"),
        CATEGORICAL("categorical", "Categorical"),
        TEMPORAL("temporal", "Temporal"),
        TEXT("text", "Text"),
        COMPOSITE("composite", "Composite");
        
        private final String code;
        private final String displayName;
        
        FeatureType(String code, String displayName) {
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
    
    @Column(name = "feature_name", length = 100, unique = true, nullable = false)
    private String featureName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "feature_type", length = 50, nullable = false)
    private FeatureType featureType;
    
    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;
    
    @Column(name = "computation_logic", columnDefinition = "JSON", nullable = false)
    private String computationLogic;
    
    @Column(name = "dependencies", columnDefinition = "JSON", nullable = false)
    private String dependencies;
    
    @Column(name = "validation_rules", columnDefinition = "JSON", nullable = false)
    private String validationRules;
    
    // Feature statistics
    @Column(name = "mean_value")
    private Double meanValue;
    
    @Column(name = "std_dev")
    private Double stdDev;
    
    @Column(name = "min_value")
    private Double minValue;
    
    @Column(name = "max_value")
    private Double maxValue;
    
    @Column(name = "unique_values", columnDefinition = "JSON")
    private String uniqueValues;
    
    // Feature importance
    @Min(0)
    @Max(1)
    @Column(name = "importance_score", nullable = false)
    private Double importanceScore;
    
    // Version control
    @Column(name = "version", length = 50, nullable = false)
    private String version;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    // Constructors
    public MLFeatureEngineering() {}
    
    public MLFeatureEngineering(String featureName, FeatureType featureType, String description, 
                               String computationLogic, String dependencies, String validationRules, 
                               Double importanceScore, String version) {
        this.featureName = featureName;
        this.featureType = featureType;
        this.description = description;
        this.computationLogic = computationLogic;
        this.dependencies = dependencies;
        this.validationRules = validationRules;
        this.importanceScore = importanceScore;
        this.version = version;
    }
}
