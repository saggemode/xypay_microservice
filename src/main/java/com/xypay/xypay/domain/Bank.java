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
@Table(name = "banks")
public class Bank extends BaseEntity {
    
    @Column(name = "name", length = 100, nullable = false)
    private String name;
    
    @Column(name = "code", length = 10, unique = true, nullable = false)
    private String code;
    
    @Column(name = "swift_code", length = 11)
    private String swiftCode;
    
    @Column(name = "country_code", length = 3)
    private String countryCode;
    
    @Column(name = "slug")
    private String slug;
    
    @Column(name = "ussd", length = 20)
    private String ussd;
    
    @Column(name = "logo")
    private String logo;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "bank_type")
    @Enumerated(EnumType.STRING)
    private BankType bankType = BankType.COMMERCIAL;
    
    @Column(name = "license_number", length = 50)
    private String licenseNumber;
    
    @Column(name = "regulatory_authority", length = 100)
    private String regulatoryAuthority;
    
    @Column(name = "head_office_address", length = 500)
    private String headOfficeAddress;
    
    @Column(name = "contact_email", length = 100)
    private String contactEmail;
    
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;
    
    @Column(name = "established_date")
    private LocalDateTime establishedDate;
    
    @Column(name = "capital_adequacy_ratio", precision = 5, scale = 2)
    private BigDecimal capitalAdequacyRatio;
    
    @Column(name = "tier1_capital", precision = 19, scale = 2)
    private BigDecimal tier1Capital;
    
    @Column(name = "total_assets", precision = 19, scale = 2)
    private BigDecimal totalAssets;
    
    // Multi-entity relationships
    @OneToMany(mappedBy = "bank", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Branch> branches;
    
    @OneToMany(mappedBy = "bank", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BankCurrency> supportedCurrencies;
    
    // Additional Configuration
    @Column(name = "applies_to_fees")
    private Boolean appliesToFees = true;
    
    @Column(name = "applies_to_levies")
    private Boolean appliesToLevies = false;
    
    @Column(name = "minimum_vatable_amount", precision = 19, scale = 4)
    private BigDecimal minimumVatableAmount = BigDecimal.ZERO;
    
    // Exemption Settings
    @Column(name = "exempt_internal_transfers")
    private Boolean exemptInternalTransfers = false;
    
    @Column(name = "exempt_international_transfers")
    private Boolean exemptInternationalTransfers = false;
    
    // Rounding Configuration
    @Column(name = "rounding_method", length = 20)
    private String roundingMethod = "none"; // none, nearest, up, down
    
    // Basel III Compliance
    @Column(name = "basel_compliant")
    private Boolean baselCompliant = false;
    
    @Column(name = "ifrs_compliant")
    private Boolean ifrsCompliant = false;
    
    @Column(name = "islamic_banking_enabled")
    private Boolean islamicBankingEnabled = false;
    
    public enum BankType {
        COMMERCIAL, INVESTMENT, CENTRAL, ISLAMIC, COOPERATIVE, DEVELOPMENT
    }
    
    // Constructors
    public Bank() {}
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getSlug() {
        return slug;
    }
    
    public void setSlug(String slug) {
        this.slug = slug;
    }
    
    public String getUssd() {
        return ussd;
    }
    
    public void setUssd(String ussd) {
        this.ussd = ussd;
    }
    
    public String getLogo() {
        return logo;
    }
    
    public void setLogo(String logo) {
        this.logo = logo;
    }
    
    public Boolean getActive() {
        return isActive;
    }
    
    public void setActive(Boolean active) {
        isActive = active;
    }
    
    public Boolean getAppliesToFees() {
        return appliesToFees;
    }
    
    public void setAppliesToFees(Boolean appliesToFees) {
        this.appliesToFees = appliesToFees;
    }
    
    public Boolean getAppliesToLevies() {
        return appliesToLevies;
    }
    
    public void setAppliesToLevies(Boolean appliesToLevies) {
        this.appliesToLevies = appliesToLevies;
    }
    
    public BigDecimal getMinimumVatableAmount() {
        return minimumVatableAmount;
    }
    
    public void setMinimumVatableAmount(BigDecimal minimumVatableAmount) {
        this.minimumVatableAmount = minimumVatableAmount;
    }
    
    public Boolean getExemptInternalTransfers() {
        return exemptInternalTransfers;
    }
    
    public void setExemptInternalTransfers(Boolean exemptInternalTransfers) {
        this.exemptInternalTransfers = exemptInternalTransfers;
    }
    
    public Boolean getExemptInternationalTransfers() {
        return exemptInternationalTransfers;
    }
    
    public void setExemptInternationalTransfers(Boolean exemptInternationalTransfers) {
        this.exemptInternationalTransfers = exemptInternationalTransfers;
    }
    
    public String getRoundingMethod() {
        return roundingMethod;
    }
    
    public void setRoundingMethod(String roundingMethod) {
        this.roundingMethod = roundingMethod;
    }
}