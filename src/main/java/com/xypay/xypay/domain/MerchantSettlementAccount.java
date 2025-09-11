package com.xypay.xypay.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "merchant_settlement_accounts", indexes = {
    @Index(name = "idx_merchant_settlement_merchant_id", columnList = "merchant_id")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class MerchantSettlementAccount extends BaseEntity {

    @Column(name = "merchant_id", length = 128, unique = true, nullable = false)
    private String merchantId;

    @Column(name = "bank_code", length = 10, nullable = false)
    private String bankCode;

    @Column(name = "account_number", length = 20, nullable = false)
    private String accountNumber;

    @Column(name = "account_name", length = 128)
    private String accountName;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Column(name = "verification_method", length = 20)
    private String verificationMethod = "api";

    @Column(name = "preferred_schedule", length = 20)
    private String preferredSchedule = "manual"; // manual, daily, weekly

    // Constructors
    public MerchantSettlementAccount() {}

    public MerchantSettlementAccount(String merchantId, String bankCode, String accountNumber) {
        this.merchantId = merchantId;
        this.bankCode = bankCode;
        this.accountNumber = accountNumber;
    }

    // Business methods
    public void verify(String verificationMethod) {
        this.isVerified = true;
        this.verificationMethod = verificationMethod;
    }

    public void unverify() {
        this.isVerified = false;
        this.verificationMethod = null;
    }

    @Override
    public String toString() {
        return String.format("MerchantSettlementAccount{merchantId='%s', bankCode='%s', accountNumber='%s'}", 
            merchantId, bankCode, accountNumber);
    }
}
