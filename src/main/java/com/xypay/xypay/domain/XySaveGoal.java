package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "xysave_goals")
public class XySaveGoal {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "name", length = 100, nullable = false)
    private String name;
    
    @Column(name = "target_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal targetAmount;
    
    @Column(name = "current_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal currentAmount = BigDecimal.ZERO;
    
    @Column(name = "target_date")
    private LocalDate targetDate;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "xysaveGoal", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<XySaveGoalTransaction> transactions;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public BigDecimal getProgressPercentage() {
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return currentAmount.divide(targetAmount, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
    }
    
    public boolean isCompleted() {
        return currentAmount.compareTo(targetAmount) >= 0;
    }
}