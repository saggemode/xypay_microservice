package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "islamic_payments")
public class IslamicPayment extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "islamic_contract_id", nullable = false)
    private IslamicBankingContract islamicContract;
    
    @Column(name = "payment_number")
    private Integer paymentNumber;
    
    @Column(name = "payment_type")
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;
    
    @Column(name = "due_date")
    private LocalDateTime dueDate;
    
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;
    
    @Column(name = "principal_amount", precision = 19, scale = 2)
    private BigDecimal principalAmount = BigDecimal.ZERO;
    
    @Column(name = "profit_amount", precision = 19, scale = 2)
    private BigDecimal profitAmount = BigDecimal.ZERO;
    
    @Column(name = "rental_amount", precision = 19, scale = 2)
    private BigDecimal rentalAmount = BigDecimal.ZERO;
    
    @Column(name = "total_amount", precision = 19, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "amount_paid", precision = 19, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;
    
    @Column(name = "outstanding_amount", precision = 19, scale = 2)
    private BigDecimal outstandingAmount;
    
    @Column(name = "payment_status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    
    @Column(name = "payment_method")
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    
    @Column(name = "payment_reference", length = 100)
    private String paymentReference;
    
    @Column(name = "days_overdue")
    private Integer daysOverdue = 0;
    
    @Column(name = "charity_amount", precision = 19, scale = 2)
    private BigDecimal charityAmount = BigDecimal.ZERO;
    
    @Column(name = "waived_amount", precision = 19, scale = 2)
    private BigDecimal waivedAmount = BigDecimal.ZERO;
    
    @Column(name = "notes", length = 500)
    private String notes;
    
    public enum PaymentType {
        INSTALLMENT, RENTAL, PROFIT_DISTRIBUTION, PRINCIPAL_REPAYMENT, 
        BALLOON_PAYMENT, EARLY_SETTLEMENT, PARTIAL_PAYMENT
    }
    
    public enum PaymentStatus {
        PENDING, PAID, PARTIAL, OVERDUE, WAIVED, CANCELLED
    }
    
    public enum PaymentMethod {
        BANK_TRANSFER, DIRECT_DEBIT, CASH, CHEQUE, ONLINE, MOBILE
    }
}
