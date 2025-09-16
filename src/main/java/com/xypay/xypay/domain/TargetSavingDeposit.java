package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "target_saving_deposits")
public class TargetSavingDeposit {
    
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
    @Column(name = "source", length = 10, nullable = false)
    private TargetSavingSource source = TargetSavingSource.WALLET;
    
    @Column(name = "deposit_date", nullable = false, updatable = false)
    private LocalDateTime depositDate;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @PrePersist
    protected void onCreate() {
        if (depositDate == null) {
            depositDate = LocalDateTime.now();
        }
        
        // Update target saving current amount
        if (targetSaving != null) {
            targetSaving.setCurrentAmount(targetSaving.getCurrentAmount().add(amount));
            
            // Check if target is completed
            if (targetSaving.getCurrentAmount().compareTo(targetSaving.getTargetAmount()) >= 0) {
                targetSaving.setIsCompleted(true);
            }
        }
    }
}