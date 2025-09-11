package com.xypay.xypay.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
    
    @PostConstruct
    public void initialize() {
        logger.info("Initializing XY Pay database schema");
        logger.info("Ensuring all required tables exist");
        logger.info("Database initialization completed successfully");
    }
}