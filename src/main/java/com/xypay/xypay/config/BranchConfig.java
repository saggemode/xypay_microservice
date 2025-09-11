package com.xypay.xypay.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "banking")
public class BranchConfig {
    private Map<String, BranchProperties> branches;
    
    public Map<String, BranchProperties> getBranches() {
        return branches;
    }
    
    public void setBranches(Map<String, BranchProperties> branches) {
        this.branches = branches;
    }
    
    public static class BranchProperties {
        private String name;
        private String location;
        private String currency;
        private String timezone;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getLocation() {
            return location;
        }
        
        public void setLocation(String location) {
            this.location = location;
        }
        
        public String getCurrency() {
            return currency;
        }
        
        public void setCurrency(String currency) {
            this.currency = currency;
        }
        
        public String getTimezone() {
            return timezone;
        }
        
        public void setTimezone(String timezone) {
            this.timezone = timezone;
        }
    }
}