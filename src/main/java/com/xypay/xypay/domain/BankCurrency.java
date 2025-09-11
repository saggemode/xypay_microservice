package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "bank_currencies")
public class BankCurrency extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false)
    private Bank bank;
    
    @Column(name = "currency_code", length = 3, nullable = false)
    private String currencyCode; // USD, EUR, NGN, GBP, etc.
    
    @Column(name = "currency_name", length = 100)
    private String currencyName;
    
    @Column(name = "currency_symbol", length = 10)
    private String currencySymbol;
    
    @Column(name = "is_base_currency")
    private Boolean isBaseCurrency = false;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "exchange_rate", precision = 19, scale = 6)
    private BigDecimal exchangeRate = BigDecimal.ONE;
    
    @Column(name = "buy_rate", precision = 19, scale = 6)
    private BigDecimal buyRate;
    
    @Column(name = "sell_rate", precision = 19, scale = 6)
    private BigDecimal sellRate;
    
    @Column(name = "mid_rate", precision = 19, scale = 6)
    private BigDecimal midRate;
    
    @Column(name = "decimal_places")
    private Integer decimalPlaces = 2;
    
    @Column(name = "minimum_amount", precision = 19, scale = 6)
    private BigDecimal minimumAmount = BigDecimal.ZERO;
    
    @Column(name = "maximum_amount", precision = 19, scale = 6)
    private BigDecimal maximumAmount;
    
    @Column(name = "daily_limit", precision = 19, scale = 6)
    private BigDecimal dailyLimit;
    
    @Column(name = "monthly_limit", precision = 19, scale = 6)
    private BigDecimal monthlyLimit;
    
    @Column(name = "supports_international")
    private Boolean supportsInternational = true;
    
    @Column(name = "supports_domestic")
    private Boolean supportsDomestic = true;
    
    @Column(name = "rate_last_updated")
    private LocalDateTime rateLastUpdated;
    
    @Column(name = "rate_source", length = 100)
    private String rateSource; // Central Bank, Reuters, Bloomberg, etc.
    
    // Real-time processing support
    @Column(name = "real_time_enabled")
    private Boolean realTimeEnabled = true;
    
    @Column(name = "batch_processing_enabled")
    private Boolean batchProcessingEnabled = true;
    
    // Regulatory compliance
    @Column(name = "aml_monitoring_enabled")
    private Boolean amlMonitoringEnabled = true;
    
    @Column(name = "reporting_currency")
    private Boolean reportingCurrency = false;
    
    @Column(name = "nostro_account", length = 50)
    private String nostroAccount; // Correspondent bank account
    
    @Column(name = "vostro_account", length = 50)
    private String vostroAccount; // Our account with correspondent bank
}
