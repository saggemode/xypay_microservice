package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;


@Data
@Entity
@Table(name = "reconciliation_items")
public class ReconciliationItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "external_ref")
    private String externalRef;
    
    @Column(name = "tx_id")
    private Long txId;
    
    private BigDecimal amount;
    
    private String status;
}