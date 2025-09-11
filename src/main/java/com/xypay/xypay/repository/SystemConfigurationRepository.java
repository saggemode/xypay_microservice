package com.xypay.xypay.repository;

import com.xypay.xypay.domain.SystemConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for SystemConfiguration entity
 * Provides enterprise-grade data access methods for system configuration management
 */
@Repository
public interface SystemConfigurationRepository extends JpaRepository<SystemConfiguration, Long> {

    /**
     * Find configuration by key
     */
    Optional<SystemConfiguration> findByConfigKey(String configKey);

    /**
     * Find active configuration by key
     */
    @Query("SELECT sc FROM SystemConfiguration sc WHERE sc.configKey = :configKey AND sc.isActive = true")
    Optional<SystemConfiguration> findActiveByConfigKey(@Param("configKey") String configKey);

    /**
     * Find all configurations by category
     */
    List<SystemConfiguration> findByCategoryAndIsActiveTrue(String category);

    /**
     * Find all active configurations
     */
    List<SystemConfiguration> findByIsActiveTrue();

    /**
     * Find configurations by category with pagination
     */
    Page<SystemConfiguration> findByCategoryAndIsActiveTrue(String category, Pageable pageable);

    /**
     * Find all active configurations with pagination
     */
    Page<SystemConfiguration> findByIsActiveTrue(Pageable pageable);

    /**
     * Check if configuration exists by key
     */
    boolean existsByConfigKey(String configKey);

    /**
     * Find configurations that require restart
     */
    @Query("SELECT sc FROM SystemConfiguration sc WHERE sc.requiresRestart = true AND sc.isActive = true")
    List<SystemConfiguration> findConfigurationsRequiringRestart();

    /**
     * Find system configurations
     */
    @Query("SELECT sc FROM SystemConfiguration sc WHERE sc.isSystemConfig = true AND sc.isActive = true")
    List<SystemConfiguration> findSystemConfigurations();

    /**
     * Find encrypted configurations
     */
    @Query("SELECT sc FROM SystemConfiguration sc WHERE sc.isEncrypted = true AND sc.isActive = true")
    List<SystemConfiguration> findEncryptedConfigurations();

    /**
     * Update configuration value by key
     */
    @Modifying
    @Query("UPDATE SystemConfiguration sc SET sc.configValue = :configValue, sc.lastUpdated = CURRENT_TIMESTAMP, sc.updatedBy = :updatedBy WHERE sc.configKey = :configKey")
    int updateConfigValueByKey(@Param("configKey") String configKey, 
                              @Param("configValue") String configValue, 
                              @Param("updatedBy") String updatedBy);

    /**
     * Deactivate configuration by key
     */
    @Modifying
    @Query("UPDATE SystemConfiguration sc SET sc.isActive = false, sc.lastUpdated = CURRENT_TIMESTAMP, sc.updatedBy = :updatedBy WHERE sc.configKey = :configKey")
    int deactivateByConfigKey(@Param("configKey") String configKey, @Param("updatedBy") String updatedBy);

    /**
     * Find configurations by key pattern
     */
    @Query("SELECT sc FROM SystemConfiguration sc WHERE sc.configKey LIKE :keyPattern AND sc.isActive = true ORDER BY sc.configKey")
    List<SystemConfiguration> findByConfigKeyPattern(@Param("keyPattern") String keyPattern);

    /**
     * Count active configurations by category
     */
    @Query("SELECT COUNT(sc) FROM SystemConfiguration sc WHERE sc.category = :category AND sc.isActive = true")
    long countActiveByCategoryName(@Param("category") String category);

    /**
     * Find all categories
     */
    @Query("SELECT DISTINCT sc.category FROM SystemConfiguration sc WHERE sc.category IS NOT NULL AND sc.isActive = true ORDER BY sc.category")
    List<String> findAllCategories();

    /**
     * Find configurations modified after a specific timestamp
     */
    @Query("SELECT sc FROM SystemConfiguration sc WHERE sc.lastUpdated > :timestamp AND sc.isActive = true ORDER BY sc.lastUpdated DESC")
    List<SystemConfiguration> findRecentlyModified(@Param("timestamp") java.time.LocalDateTime timestamp);

    /**
     * Bulk update category for configurations
     */
    @Modifying
    @Query("UPDATE SystemConfiguration sc SET sc.category = :newCategory, sc.lastUpdated = CURRENT_TIMESTAMP, sc.updatedBy = :updatedBy WHERE sc.configKey IN :configKeys")
    int updateCategoryForKeys(@Param("configKeys") List<String> configKeys, 
                             @Param("newCategory") String newCategory, 
                             @Param("updatedBy") String updatedBy);
}
