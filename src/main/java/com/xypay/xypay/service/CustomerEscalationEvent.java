package com.xypay.xypay.service;

import com.xypay.xypay.domain.CustomerEscalation;

/**
 * Event representing a customer escalation.
 */
public class CustomerEscalationEvent {
    
    private CustomerEscalation escalation;
    private boolean created;
    
    public CustomerEscalationEvent(CustomerEscalation escalation, boolean created) {
        this.escalation = escalation;
        this.created = created;
    }
    
    // Getters and setters
    public CustomerEscalation getEscalation() {
        return escalation;
    }
    
    public void setEscalation(CustomerEscalation escalation) {
        this.escalation = escalation;
    }
    
    public boolean isCreated() {
        return created;
    }
    
    public void setCreated(boolean created) {
        this.created = created;
    }
}