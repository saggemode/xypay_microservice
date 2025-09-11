package com.xypay.xypay.service;

import com.xypay.xypay.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DatabaseSetupService {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseSetupService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Check if the system has been initialized by checking if admin user exists
     * In a real implementation, this would query the database
     * @return true if system is initialized, false otherwise
     */
    public boolean isSystemInitialized() {
        return userRepository.count() > 0;
    }
    
    /**
     * Initialize the system with bank and admin user information
     * @param bankName The name of the bank
     * @param bankCode The bank code
     * @param adminUser Admin user details
     * @return true if successful, false otherwise
     */
    public boolean initializeSystem(String bankName, String bankCode, SetupService.AdminUser adminUser) {
        try {
            logger.info("Initializing system for bank: {} with code: {}", bankName, bankCode);
            
            // In a real implementation, this would:
            // 1. Insert the bank/branch information into the branches table
            // 2. Create the admin user in the users table
            // 3. Assign the ADMIN role to the user in the user_roles table
            // 4. Set system initialization flag
            
            // For demo purposes, we'll just log the operation
            logger.info("Creating main branch: {} ({})", bankName, bankCode);
            logger.info("Creating admin user: {}", adminUser.getUsername());
            logger.info("Assigning ADMIN role to user: {}", adminUser.getUsername());
            
            // Simulate successful initialization
            logger.info("System initialization completed successfully");
            return true;
        } catch (Exception e) {
            logger.error("Failed to initialize system", e);
            return false;
        }
    }
}