package com.xypay.analytics.repository;

import com.xypay.analytics.domain.MLModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MLModelRepository extends JpaRepository<MLModel, Long> {
    
    Optional<MLModel> findByModelNameAndModelVersion(String modelName, String modelVersion);
    
    List<MLModel> findByModelType(String modelType);
    
    List<MLModel> findByIsActiveTrue();
    
    Optional<MLModel> findByModelTypeAndIsActiveTrue(String modelType);
    
    @Query("SELECT mm FROM MLModel mm WHERE mm.trainingDate >= :startDate AND mm.trainingDate <= :endDate")
    List<MLModel> findByTrainingDateRange(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT mm FROM MLModel mm WHERE mm.accuracy >= :minAccuracy")
    List<MLModel> findByMinAccuracy(@Param("minAccuracy") Double minAccuracy);
    
    @Query("SELECT mm FROM MLModel mm WHERE mm.modelName = :modelName ORDER BY mm.trainingDate DESC")
    List<MLModel> findByModelNameOrderByTrainingDateDesc(@Param("modelName") String modelName);
    
    @Query("SELECT COUNT(mm) FROM MLModel mm WHERE mm.modelType = :modelType")
    Long countByModelType(@Param("modelType") String modelType);
    
    @Query("SELECT AVG(mm.accuracy) FROM MLModel mm WHERE mm.modelType = :modelType")
    Double getAverageAccuracyByModelType(@Param("modelType") String modelType);
}