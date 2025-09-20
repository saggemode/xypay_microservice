package com.xypay.account.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "balance_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "account_id", nullable = false)
    private UUID accountId;
    
    @Column(name = "old_balance", precision = 19, scale = 2)
    private BigDecimal oldBalance;
    
    @Column(name = "new_balance", precision = 19, scale = 2)
    private BigDecimal newBalance;
    
    @Column(name = "change_amount", precision = 19, scale = 2)
    private BigDecimal changeAmount;
    
    @Column(name = "reason", nullable = false)
    private String reason;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
