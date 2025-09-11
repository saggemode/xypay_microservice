package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "loan_amortizations")
public class LoanAmortization extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;
    
    @Column(name = "installment_number")
    private Integer installmentNumber;
    
    @Column(name = "due_date")
    private LocalDateTime dueDate;
    
    @Column(name = "opening_balance", precision = 19, scale = 2)
    private BigDecimal openingBalance = BigDecimal.ZERO;
    
    @Column(name = "principal_amount", precision = 19, scale = 2)
    private BigDecimal principalAmount = BigDecimal.ZERO;
    
    @Column(name = "interest_amount", precision = 19, scale = 2)
    private BigDecimal interestAmount = BigDecimal.ZERO;
    
    @Column(name = "total_payment", precision = 19, scale = 2)
    private BigDecimal totalPayment = BigDecimal.ZERO;
    
    @Column(name = "closing_balance", precision = 19, scale = 2)
    private BigDecimal closingBalance = BigDecimal.ZERO;
    
    @Column(name = "cumulative_principal", precision = 19, scale = 2)
    private BigDecimal cumulativePrincipal = BigDecimal.ZERO;
    
    @Column(name = "cumulative_interest", precision = 19, scale = 2)
    private BigDecimal cumulativeInterest = BigDecimal.ZERO;
    
    @Column(name = "is_paid")
    private Boolean isPaid = false;
    
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;
    
    @Column(name = "days_in_period")
    private Integer daysInPeriod = 30;
    
    // Islamic Banking fields
    @Column(name = "profit_amount", precision = 19, scale = 2)
    private BigDecimal profitAmount = BigDecimal.ZERO;
    
    @Column(name = "rental_amount", precision = 19, scale = 2)
    private BigDecimal rentalAmount = BigDecimal.ZERO; // For Ijara
}
