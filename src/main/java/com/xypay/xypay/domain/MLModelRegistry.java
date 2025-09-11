package com.xypay.xypay.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ml_model_registry", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"model_name", "model_version"}),
    indexes = {
        @Index(name = "idx_ml_model_name_version", columnList = "model_name, model_version"),
        @Index(name = "idx_ml_model_env_active", columnList = "deployment_environment, is_active"),
        @Index(name = "idx_ml_model_health", columnList = "health_status")
    })
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class MLModelRegistry extends BaseEntity {

    @Column(name = "model_name", length = 100, nullable = false)
    private String modelName;

    @Column(name = "model_version", length = 50, nullable = false)
    private String modelVersion;

    @Column(name = "model_type", length = 50, nullable = false)
    private String modelType; // fraud_detection, risk_scoring, anomaly_detection, behavioral_analysis, transaction_monitoring

    @Column(name = "framework", length = 50)
    private String framework;

    @Column(name = "input_features", columnDefinition = "TEXT")
    private String inputFeatures = "{}";

    @Column(name = "output_format", columnDefinition = "TEXT")
    private String outputFormat = "{}";

    @Column(name = "preprocessing_steps", columnDefinition = "TEXT")
    private String preprocessingSteps = "{}";

    @Column(name = "deployment_environment", length = 50, nullable = false)
    private String deploymentEnvironment = "development"; // development, staging, production

    @Column(name = "deployed_at")
    private LocalDateTime deployedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deployed_by_id")
    private User deployedBy;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;

    @Column(name = "performance_metrics", columnDefinition = "TEXT")
    private String performanceMetrics = "{}";

    @Column(name = "inference_latency")
    private Double inferenceLatency;

    @Column(name = "error_rate")
    private Double errorRate;

    @Column(name = "health_status", length = 20, nullable = false)
    private String healthStatus = "healthy"; // healthy, degraded, failed

    @Column(name = "last_monitoring_check", nullable = false)
    private LocalDateTime lastMonitoringCheck = LocalDateTime.now();

    @Column(name = "monitoring_metrics", columnDefinition = "TEXT")
    private String monitoringMetrics = "{}";

    // Constructors
    public MLModelRegistry() {}

    public MLModelRegistry(String modelName, String modelVersion, String modelType) {
        this.modelName = modelName;
        this.modelVersion = modelVersion;
        this.modelType = modelType;
    }

    // Business methods
    public void deploy(String environment, User deployedBy) {
        this.deploymentEnvironment = environment;
        this.deployedAt = LocalDateTime.now();
        this.deployedBy = deployedBy;
        this.isActive = true;
    }

    public void rollback() {
        this.isActive = false;
        this.healthStatus = "failed";
    }

    public void checkHealth() {
        if (errorRate != null && errorRate > 0.1) {
            this.healthStatus = "degraded";
        }
        if (errorRate != null && errorRate > 0.2) {
            this.healthStatus = "failed";
            this.isActive = false;
        }
        this.lastMonitoringCheck = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("MLModelRegistry{modelName='%s', version='%s', environment='%s'}", 
            modelName, modelVersion, deploymentEnvironment);
    }
}
