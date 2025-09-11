package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "loan_repayments")
public class LoanRepayment extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;
    
    @Column(name = "payment_number")
    private Integer paymentNumber;
    
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;
    
    @Column(name = "due_date")
    private LocalDateTime dueDate;
    
    @Column(name = "principal_amount", precision = 19, scale = 2)
    private BigDecimal principalAmount = BigDecimal.ZERO;
    
    @Column(name = "interest_amount", precision = 19, scale = 2)
    private BigDecimal interestAmount = BigDecimal.ZERO;
    
    @Column(name = "penalty_amount", precision = 19, scale = 2)
    private BigDecimal penaltyAmount = BigDecimal.ZERO;
    
    @Column(name = "total_amount", precision = 19, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    @Column(name = "paid_amount", precision = 19, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;
    
    @Column(name = "outstanding_amount", precision = 19, scale = 2)
    private BigDecimal outstandingAmount = BigDecimal.ZERO;
    
    @Column(name = "payment_status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;
    
    @Column(name = "transaction_reference", length = 100)
    private String transactionReference;
    
    @Column(name = "days_overdue")
    private Integer daysOverdue = 0;
    
    @Column(name = "late_fee", precision = 19, scale = 2)
    private BigDecimal lateFee = BigDecimal.ZERO;
    
    @Column(name = "processed_by")
    private Long processedBy;
    
    @Column(name = "notes", length = 500)
    private String notes;
    
    public enum PaymentStatus {
        PENDING, PARTIAL, PAID, OVERDUE, WAIVED, WRITTEN_OFF
    }
}
