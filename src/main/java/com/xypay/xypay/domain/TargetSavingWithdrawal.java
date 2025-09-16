package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "target_saving_withdrawals")
public class TargetSavingWithdrawal {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_saving_id", nullable = false)
    private TargetSaving targetSaving;
    
    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "destination", length = 10, nullable = false)
    private TargetSavingSource destination = TargetSavingSource.WALLET;
    
    @Column(name = "withdrawal_date", nullable = false, updatable = false)
    private LocalDateTime withdrawalDate;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @PrePersist
    protected void onCreate() {
        if (withdrawalDate == null) {
            withdrawalDate = LocalDateTime.now();
        }
        
        // Update target saving current amount
        if (targetSaving != null) {
            targetSaving.setCurrentAmount(targetSaving.getCurrentAmount().subtract(amount));
        }
    }
}