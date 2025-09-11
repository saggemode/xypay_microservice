package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;


@Data
@Entity
@Table(name = "gl_accounts")
public class GLAccount {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String code;
    
    private String name;
    
    private String type;
    
    private BigDecimal balance;
}