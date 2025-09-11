package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "general_ledger", indexes = {
    @Index(name = "idx_gl_account_date", columnList = "chart_of_accounts_id, transaction_date"),
    @Index(name = "idx_gl_reference", columnList = "reference_number"),
    @Index(name = "idx_gl_batch", columnList = "batch_number")
})
public class GeneralLedger extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chart_of_accounts_id", nullable = false)
    private ChartOfAccounts chartOfAccounts;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false)
    private Bank bank;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;
    
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;
    
    @Column(name = "value_date", nullable = false)
    private LocalDateTime valueDate;
    
    @Column(name = "reference_number", length = 50, nullable = false)
    private String referenceNumber;
    
    @Column(name = "batch_number", length = 20)
    private String batchNumber;
    
    @Column(name = "transaction_type", length = 50)
    private String transactionType;
    
    @Column(name = "description", length = 500, nullable = false)
    private String description;
    
    @Column(name = "debit_amount", precision = 19, scale = 2)
    private BigDecimal debitAmount = BigDecimal.ZERO;
    
    @Column(name = "credit_amount", precision = 19, scale = 2)
    private BigDecimal creditAmount = BigDecimal.ZERO;
    
    @Column(name = "currency_code", length = 3, nullable = false)
    private String currencyCode;
    
    @Column(name = "exchange_rate", precision = 10, scale = 6)
    private BigDecimal exchangeRate = BigDecimal.ONE;
    
    @Column(name = "base_currency_debit", precision = 19, scale = 2)
    private BigDecimal baseCurrencyDebit = BigDecimal.ZERO;
    
    @Column(name = "base_currency_credit", precision = 19, scale = 2)
    private BigDecimal baseCurrencyCredit = BigDecimal.ZERO;
    
    @Column(name = "running_balance", precision = 19, scale = 2)
    private BigDecimal runningBalance = BigDecimal.ZERO;
    
    @Column(name = "contra_account_code", length = 20)
    private String contraAccountCode;
    
    @Column(name = "source_module", length = 50)
    private String sourceModule; // LOANS, DEPOSITS, TRANSFERS, TREASURY, etc.
    
    @Column(name = "source_transaction_id")
    private Long sourceTransactionId;
    
    @Column(name = "posting_status")
    @Enumerated(EnumType.STRING)
    private PostingStatus postingStatus = PostingStatus.POSTED;
    
    @Column(name = "reversal_indicator")
    private Boolean reversalIndicator = false;
    
    @Column(name = "original_entry_id")
    private Long originalEntryId;
    
    // Regulatory and Compliance
    @Column(name = "regulatory_code", length = 20)
    private String regulatoryCode;
    
    @Column(name = "ifrs_stage")
    @Enumerated(EnumType.STRING)
    private IFRSStage ifrsStage;
    
    @Column(name = "basel_category", length = 50)
    private String baselCategory;
    
    // Audit Trail
    @Column(name = "posted_by", length = 100, nullable = false)
    private String postedBy;
    
    @Column(name = "posting_date", nullable = false)
    private LocalDateTime postingDate;
    
    @Column(name = "authorized_by", length = 100)
    private String authorizedBy;
    
    @Column(name = "authorization_date")
    private LocalDateTime authorizationDate;
    
    @Column(name = "maker_checker_status")
    @Enumerated(EnumType.STRING)
    private MakerCheckerStatus makerCheckerStatus = MakerCheckerStatus.APPROVED;
    
    // Additional metadata
    @Column(name = "customer_id")
    private Long customerId;
    
    @Column(name = "product_code", length = 20)
    private String productCode;
    
    @Column(name = "channel", length = 20)
    private String channel;
    
    @Column(name = "narrative", length = 1000)
    private String narrative;
    
    public enum PostingStatus {
        POSTED, PENDING, REJECTED, REVERSED
    }
    
    public enum IFRSStage {
        STAGE_1, STAGE_2, STAGE_3, PURCHASED_CREDIT_IMPAIRED
    }
    
    public enum MakerCheckerStatus {
        PENDING, APPROVED, REJECTED
    }
    
    // Helper methods
    public boolean isDebitEntry() {
        return debitAmount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public boolean isCreditEntry() {
        return creditAmount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public BigDecimal getTransactionAmount() {
        return isDebitEntry() ? debitAmount : creditAmount;
    }
    
    public void reverseEntry() {
        this.reversalIndicator = true;
        this.postingStatus = PostingStatus.REVERSED;
        
        // Swap debit and credit amounts for reversal
        BigDecimal tempDebit = this.debitAmount;
        this.debitAmount = this.creditAmount;
        this.creditAmount = tempDebit;
        
        // Swap base currency amounts
        BigDecimal tempBaseDebit = this.baseCurrencyDebit;
        this.baseCurrencyDebit = this.baseCurrencyCredit;
        this.baseCurrencyCredit = tempBaseDebit;
    }
    
    public String getEntryType() {
        return isDebitEntry() ? "DR" : "CR";
    }
    
    public void calculateBaseCurrencyAmounts(String baseCurrency) {
        if (currencyCode.equals(baseCurrency)) {
            baseCurrencyDebit = debitAmount;
            baseCurrencyCredit = creditAmount;
        } else {
            baseCurrencyDebit = debitAmount.multiply(exchangeRate);
            baseCurrencyCredit = creditAmount.multiply(exchangeRate);
        }
    }
}
