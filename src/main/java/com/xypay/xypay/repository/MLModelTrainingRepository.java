package com.xypay.xypay.repository;

import com.xypay.xypay.domain.MLModelTraining;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MLModelTrainingRepository extends JpaRepository<MLModelTraining, UUID> {
    
    List<MLModelTraining> findByModelNameOrderByVersionDesc(String modelName);
    
    List<MLModelTraining> findByModelTypeOrderByCreatedAtDesc(MLModelTraining.ModelType modelType);
    
    List<MLModelTraining> findByIsProductionTrue();
    
    Optional<MLModelTraining> findByModelNameAndVersion(String modelName, String version);
    
    List<MLModelTraining> findByTrainingStartBetweenOrderByTrainingStartDesc(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT mmt FROM MLModelTraining mmt WHERE mmt.modelName = :modelName AND mmt.isProduction = true")
    Optional<MLModelTraining> findProductionModel(@Param("modelName") String modelName);
    
    @Query("SELECT mmt FROM MLModelTraining mmt WHERE mmt.modelType = :modelType AND mmt.isProduction = true ORDER BY mmt.createdAt DESC")
    List<MLModelTraining> findProductionModelsByType(@Param("modelType") MLModelTraining.ModelType modelType);
}
