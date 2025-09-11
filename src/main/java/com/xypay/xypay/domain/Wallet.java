package com.xypay.xypay.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "wallets", indexes = {
    @Index(name = "idx_wallet_user", columnList = "user_id"),
    @Index(name = "idx_wallet_account_number", columnList = "account_number"),
    @Index(name = "idx_wallet_alt_account_number", columnList = "alternative_account_number"),
    @Index(name = "idx_wallet_phone_alias", columnList = "phone_alias", unique = true)
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Wallet extends BaseEntity {
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;
    
    @Column(name = "account_number", unique = true, nullable = false, length = 10)
    private String accountNumber;
    
    @Column(name = "alternative_account_number", unique = true, nullable = false, length = 10)
    private String alternativeAccountNumber;
    
    @Column(name = "balance", precision = 19, scale = 4, nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;
    
    @Column(name = "currency", length = 5, nullable = false)
    private String currency = "NGN";
    
    @Column(name = "phone_alias", unique = true, length = 15)
    private String phoneAlias;
    
    // Constructors
    public Wallet() {}
    
    public Wallet(User user, String accountNumber) {
        this.user = user;
        this.accountNumber = accountNumber;
    }
    
    // Getters and Setters
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public String getAlternativeAccountNumber() {
        return alternativeAccountNumber;
    }
    
    public void setAlternativeAccountNumber(String alternativeAccountNumber) {
        this.alternativeAccountNumber = alternativeAccountNumber;
    }
    
    public BigDecimal getBalance() {
        return balance;
    }
    
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public Branch getBranch() {
        return branch;
    }
    
    public void setBranch(Branch branch) {
        this.branch = branch;
    }
    
    public String getPhoneAlias() {
        return phoneAlias;
    }
    
    public void setPhoneAlias(String phoneAlias) {
        this.phoneAlias = phoneAlias;
    }
}