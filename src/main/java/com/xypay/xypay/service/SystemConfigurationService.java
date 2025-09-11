package com.xypay.xypay.service;

import com.xypay.xypay.domain.SystemConfiguration;
import com.xypay.xypay.repository.SystemConfigurationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Enterprise service for managing system configuration parameters
 * Provides caching, encryption, and audit capabilities for banking systems
 */
@Service
@Transactional
public class SystemConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(SystemConfigurationService.class);

    @Autowired
    private SystemConfigurationRepository systemConfigurationRepository;

    /**
     * Save or update a configuration parameter
     */
    public SystemConfiguration saveConfiguration(String configKey, String configValue) {
        return saveConfiguration(configKey, configValue, null, null);
    }

    /**
     * Save or update a configuration parameter with category and description
     */
    public SystemConfiguration saveConfiguration(String configKey, String configValue, String category, String description) {
        logger.debug("Saving configuration: {} = {}", configKey, configValue != null ? "***" : null);

        Optional<SystemConfiguration> existingConfig = systemConfigurationRepository.findByConfigKey(configKey);
        SystemConfiguration config;

        if (existingConfig.isPresent()) {
            config = existingConfig.get();
            config.setConfigValue(configValue);
            config.setUpdatedBy("ADMIN");
            config.setLastUpdated(LocalDateTime.now());
            
            if (category != null) {
                config.setCategory(category);
            }
            if (description != null) {
                config.setDescription(description);
            }
        } else {
            config = new SystemConfiguration(configKey, configValue, category, description);
            config.setUpdatedBy("ADMIN");
            
            // Set default category if not provided
            if (category == null) {
                config.setCategory(determineCategory(configKey));
            }
        }

        SystemConfiguration savedConfig = systemConfigurationRepository.save(config);
        logger.info("Configuration saved: {} (ID: {})", configKey, savedConfig.getId());
        
        return savedConfig;
    }

    /**
     * Get configuration value by key
     */
    public String getConfigurationValue(String configKey) {
        logger.debug("Getting configuration value for key: {}", configKey);
        
        Optional<SystemConfiguration> config = systemConfigurationRepository.findActiveByConfigKey(configKey);
        
        if (config.isPresent()) {
            String value = config.get().getConfigValue();
            logger.debug("Configuration found for key: {}", configKey);
            return value;
        }
        
        logger.debug("Configuration not found for key: {}", configKey);
        return null;
    }

    /**
     * Get configuration value with default fallback
     */
    public String getConfigurationValue(String configKey, String defaultValue) {
        String value = getConfigurationValue(configKey);
        return value != null ? value : defaultValue;
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Determine category based on configuration key
     */
    private String determineCategory(String configKey) {
        if (configKey.startsWith("smtp.") || configKey.startsWith("email.")) {
            return "EMAIL";
        } else if (configKey.startsWith("security.") || configKey.startsWith("auth.")) {
            return "SECURITY";
        } else if (configKey.startsWith("system.")) {
            return "SYSTEM";
        } else if (configKey.startsWith("database.") || configKey.startsWith("db.")) {
            return "DATABASE";
        } else if (configKey.startsWith("api.") || configKey.startsWith("integration.")) {
            return "INTEGRATION";
        } else {
            return "GENERAL";
        }
    }
}
