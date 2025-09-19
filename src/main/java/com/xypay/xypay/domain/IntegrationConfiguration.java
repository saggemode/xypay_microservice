package com.xypay.xypay.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "integration_configurations")
public class IntegrationConfiguration extends BaseEntity {
    
    @Column(name = "integration_name")
    private String integrationName;
    
    @Column(name = "integration_type")
    private String integrationType; // PAYMENT_SWITCH, MOBILE_BANKING, CRM, FINTECH
    
    @Column(name = "endpoint_url")
    private String endpointUrl;
    
    @Column(name = "authentication_method")
    private String authenticationMethod; // API_KEY, OAUTH, CERTIFICATE
    
    @Column(name = "message_format")
    private String messageFormat; // ISO8583, ISO20022, PROPRIETARY
    
    @Column(name = "connection_settings")
    private String connectionSettings; // JSON format of connection parameters
    
    @Column(name = "mapping_rules")
    private String mappingRules; // JSON format of field mapping rules
    
    @Column(name = "is_active")
    private Boolean isActive = true;

    // Getters and Setters

    public String getIntegrationName() {
        return integrationName;
    }

    public void setIntegrationName(String integrationName) {
        this.integrationName = integrationName;
    }

    public String getIntegrationType() {
        return integrationType;
    }

    public void setIntegrationType(String integrationType) {
        this.integrationType = integrationType;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    public String getAuthenticationMethod() {
        return authenticationMethod;
    }

    public void setAuthenticationMethod(String authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
    }

    public String getMessageFormat() {
        return messageFormat;
    }

    public void setMessageFormat(String messageFormat) {
        this.messageFormat = messageFormat;
    }

    public String getConnectionSettings() {
        return connectionSettings;
    }

    public void setConnectionSettings(String connectionSettings) {
        this.connectionSettings = connectionSettings;
    }

    public String getMappingRules() {
        return mappingRules;
    }

    public void setMappingRules(String mappingRules) {
        this.mappingRules = mappingRules;
    }


    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}