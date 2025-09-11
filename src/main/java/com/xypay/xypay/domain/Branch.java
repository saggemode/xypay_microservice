package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "branches")
public class Branch extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false)
    private Bank bank;
    
    @Column(name = "name", length = 100, nullable = false)
    private String name;
    
    @Column(name = "code", length = 20, unique = true, nullable = false)
    private String code;
    
    @Column(name = "swift_code", length = 11)
    private String swiftCode;
    
    @Column(name = "address", length = 500)
    private String address;
    
    @Column(name = "city", length = 100)
    private String city;
    
    @Column(name = "state", length = 100)
    private String state;
    
    @Column(name = "country", length = 100)
    private String country;
    
    @Column(name = "postal_code", length = 20)
    private String postalCode;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "email", length = 100)
    private String email;
    
    @Column(name = "manager_name", length = 100)
    private String managerName;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "branch_type")
    @Enumerated(EnumType.STRING)
    private BranchType branchType = BranchType.FULL_SERVICE;
    
    @Column(name = "operating_hours", length = 200)
    private String operatingHours;
    
    @Column(name = "time_zone", length = 50)
    private String timeZone;
    
    @Column(name = "cash_limit", precision = 19, scale = 2)
    private BigDecimal cashLimit;
    
    @Column(name = "daily_transaction_limit", precision = 19, scale = 2)
    private BigDecimal dailyTransactionLimit;
    
    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;
    
    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;
    
    @Column(name = "established_date")
    private LocalDateTime establishedDate;
    
    @Column(name = "license_number", length = 50)
    private String licenseNumber;
    
    // Relationships
    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Wallet> accounts;
    
    // 24/7 Operations Support
    @Column(name = "supports_24x7")
    private Boolean supports24x7 = false;
    
    @Column(name = "automated_processing")
    private Boolean automatedProcessing = true;
    
    @Column(name = "real_time_processing")
    private Boolean realTimeProcessing = true;
    
    public enum BranchType {
        FULL_SERVICE, ATM_ONLY, MOBILE, DIGITAL, CORPORATE, RETAIL
    }
}
