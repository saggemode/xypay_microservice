package com.xypay.analytics.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "daily_aggregates")
public class DailyAggregate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date", nullable = false, unique = true)
    private LocalDate date;

    @Column(name = "transactions_count")
    private Long transactionsCount = 0L;

    @Column(name = "transactions_volume", precision = 19, scale = 2)
    private BigDecimal transactionsVolume = BigDecimal.ZERO;

    @Column(name = "active_users")
    private Long activeUsers = 0L;
}


