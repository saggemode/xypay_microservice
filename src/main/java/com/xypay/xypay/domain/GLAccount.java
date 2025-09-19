package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;


@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "gl_accounts")
public class GLAccount extends BaseEntity {
    
    private String code;
    
    private String name;
    
    private String type;
    
    private BigDecimal balance;
}