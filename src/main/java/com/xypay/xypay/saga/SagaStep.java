package com.xypay.xypay.saga;

public class SagaStep {
    private String sagaType;
    private String step;
    private String payload;
    private String status;
    
    // Getters and setters
    public String getSagaType() {
        return sagaType;
    }
    
    public void setSagaType(String sagaType) {
        this.sagaType = sagaType;
    }
    
    public String getStep() {
        return step;
    }
    
    public void setStep(String step) {
        this.step = step;
    }
    
    public String getPayload() {
        return payload;
    }
    
    public void setPayload(String payload) {
        this.payload = payload;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}