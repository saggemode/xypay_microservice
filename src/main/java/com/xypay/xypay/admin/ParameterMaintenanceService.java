package com.xypay.xypay.admin;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ParameterMaintenanceService {
    
    // In-memory storage for system parameters
    // In a real implementation, this would interface with the database directly
    private Map<String, Object> systemParameters = new HashMap<>();
    
    public ParameterMaintenanceService() {
        // Initialize with some default parameters
        systemParameters.put("BANK_NAME", "XY Bank");
        systemParameters.put("BANK_CODE", "XY001");
        systemParameters.put("DEFAULT_CURRENCY", "USD");
        systemParameters.put("BUSINESS_DATE", "2023-01-01");
        systemParameters.put("CUTOFF_TIME", "17:00:00");
        systemParameters.put("MAX_LOGIN_ATTEMPTS", 3);
    }
    
    /**
     * Get a system parameter by name
     * 
     * @param paramName The name of the parameter
     * @return The parameter value
     */
    public Object getParameter(String paramName) {
        return systemParameters.get(paramName);
    }
    
    /**
     * Set a system parameter
     * 
     * @param paramName The name of the parameter
     * @param value The value to set
     */
    public void setParameter(String paramName, Object value) {
        systemParameters.put(paramName, value);
    }
    
    /**
     * Get all system parameters
     * 
     * @return A map of all parameters
     */
    public Map<String, Object> getAllParameters() {
        return new HashMap<>(systemParameters);
    }
    
    /**
     * Delete a system parameter
     * 
     * @param paramName The name of the parameter to delete
     */
    public void deleteParameter(String paramName) {
        systemParameters.remove(paramName);
    }
}