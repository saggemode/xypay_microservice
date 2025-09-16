package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "xysave_goal_transactions")
public class XySaveGoalTransaction {
    
    public enum TransactionType {
        DEPOSIT("deposit", "Deposit"),
        WITHDRAWAL("withdrawal", "Withdrawal"),
        INTEREST_CREDIT("interest_credit", "Interest Credit");
        
        private final String code;
        private final String description;
        
        TransactionType(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "xysave_goal_id", nullable = false)
    private XySaveGoal xysaveGoal;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 20, nullable = false)
    private TransactionType transactionType;
    
    @Column(name = "amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;
    
    @Column(name = "balance_before", precision = 19, scale = 4, nullable = false)
    private BigDecimal balanceBefore;
    
    @Column(name = "balance_after", precision = 19, scale = 4, nullable = false)
    private BigDecimal balanceAfter;
    
    @Column(name = "reference", length = 100, unique = true, nullable = false)
    private String reference;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (reference == null) {
            reference = generateReference();
        }
    }
    
    private String generateReference() {
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "XSG" + uuid;
    }
}
