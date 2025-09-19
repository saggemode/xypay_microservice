package com.xypay.xypay.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "kyc_documents")
public class KYCDocument extends BaseEntity {
    private String customerName;
    private String fileName;
    private String status;

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
