package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.util.UUID;


@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "reconciliation_items")
public class ReconciliationItem extends BaseEntity {
    
    @Column(name = "external_ref")
    private String externalRef;
    
    @Column(name = "tx_id")
    private UUID txId;
    
    private BigDecimal amount;
    
    private String status;
}