package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "data_warehouse_facts")
public class DataWarehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Date dimension
    @Column(name = "fact_date")
    private LocalDate factDate;

    @Column(name = "year")
    private Integer year;

    @Column(name = "month")
    private Integer month;

    @Column(name = "quarter")
    private Integer quarter;

    @Column(name = "day_of_week")
    private Integer dayOfWeek;

    // Transaction facts
    @Column(name = "total_transactions")
    private Long totalTransactions = 0L;

    @Column(name = "total_amount")
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "credit_transactions")
    private Long creditTransactions = 0L;

    @Column(name = "credit_amount")
    private BigDecimal creditAmount = BigDecimal.ZERO;

    @Column(name = "debit_transactions")
    private Long debitTransactions = 0L;

    @Column(name = "debit_amount")
    private BigDecimal debitAmount = BigDecimal.ZERO;

    // Customer facts
    @Column(name = "new_customers")
    private Long newCustomers = 0L;

    @Column(name = "active_customers")
    private Long activeCustomers = 0L;

    @Column(name = "total_customers")
    private Long totalCustomers = 0L;

    @Column(name = "verified_customers")
    private Long verifiedCustomers = 0L;

    // Wallet facts
    @Column(name = "total_wallet_balance")
    private BigDecimal totalWalletBalance = BigDecimal.ZERO;

    @Column(name = "average_wallet_balance")
    private BigDecimal averageWalletBalance = BigDecimal.ZERO;

    // Channel facts
    @Column(name = "mobile_transactions")
    private Long mobileTransactions = 0L;

    @Column(name = "web_transactions")
    private Long webTransactions = 0L;

    @Column(name = "api_transactions")
    private Long apiTransactions = 0L;

    // Status facts
    @Column(name = "successful_transactions")
    private Long successfulTransactions = 0L;

    @Column(name = "failed_transactions")
    private Long failedTransactions = 0L;

    @Column(name = "pending_transactions")
    private Long pendingTransactions = 0L;

    // Metadata
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
