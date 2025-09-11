package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "treasury_positions")
public class TreasuryPosition extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treasury_operation_id", nullable = false)
    private TreasuryOperation treasuryOperation;
    
    @Column(name = "position_date")
    private LocalDateTime positionDate;
    
    @Column(name = "quantity", precision = 19, scale = 6)
    private BigDecimal quantity;
    
    @Column(name = "unit_price", precision = 19, scale = 6)
    private BigDecimal unitPrice;
    
    @Column(name = "market_price", precision = 19, scale = 6)
    private BigDecimal marketPrice;
    
    @Column(name = "position_value", precision = 19, scale = 2)
    private BigDecimal positionValue;
    
    @Column(name = "unrealized_pnl", precision = 19, scale = 2)
    private BigDecimal unrealizedPnl = BigDecimal.ZERO;
    
    @Column(name = "position_type")
    @Enumerated(EnumType.STRING)
    private PositionType positionType;
    
    public enum PositionType {
        LONG, SHORT, FLAT
    }
}
