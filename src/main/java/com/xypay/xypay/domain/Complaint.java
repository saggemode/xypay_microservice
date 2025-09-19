package com.xypay.xypay.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "complaints")
public class Complaint extends BaseEntity {
    
    @Column(name = "customer_id")
    private UUID customerId;
    
    private String customerName;
    
    @Column(name = "complaint_type")
    private String complaintType;
    
    private String description;
    private String status;

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getComplaintType() { return complaintType; }
    public void setComplaintType(String complaintType) { this.complaintType = complaintType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
