package com.xypay.xypay.repository;

import com.xypay.xypay.domain.MLFeatureEngineering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MLFeatureEngineeringRepository extends JpaRepository<MLFeatureEngineering, UUID> {
    
    List<MLFeatureEngineering> findByIsActiveTrueOrderByImportanceScoreDesc();
    
    List<MLFeatureEngineering> findByFeatureTypeOrderByImportanceScoreDesc(MLFeatureEngineering.FeatureType featureType);
    
    Optional<MLFeatureEngineering> findByFeatureName(String featureName);
    
    List<MLFeatureEngineering> findByVersionOrderByImportanceScoreDesc(String version);
    
    @Query("SELECT mfe FROM MLFeatureEngineering mfe WHERE mfe.isActive = true AND mfe.importanceScore >= :minScore ORDER BY mfe.importanceScore DESC")
    List<MLFeatureEngineering> findActiveFeaturesByMinImportance(@Param("minScore") Double minScore);
    
    @Query("SELECT mfe FROM MLFeatureEngineering mfe WHERE mfe.featureType = :featureType AND mfe.isActive = true ORDER BY mfe.importanceScore DESC")
    List<MLFeatureEngineering> findActiveFeaturesByType(@Param("featureType") MLFeatureEngineering.FeatureType featureType);
}
