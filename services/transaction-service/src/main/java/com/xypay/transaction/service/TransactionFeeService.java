package com.xypay.transaction.service;

import com.xypay.transaction.domain.Transaction;
import com.xypay.transaction.enums.TransactionChannel;
import com.xypay.transaction.enums.TransactionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Slf4j
public class TransactionFeeService {
    
    // Fee configuration constants
    private static final BigDecimal TRANSFER_FEE_PERCENTAGE = new BigDecimal("0.5"); // 0.5%
    private static final BigDecimal TRANSFER_FIXED_FEE = new BigDecimal("50.00"); // ₦50
    private static final BigDecimal WITHDRAWAL_FEE_PERCENTAGE = new BigDecimal("1.0"); // 1.0%
    private static final BigDecimal WITHDRAWAL_FIXED_FEE = new BigDecimal("100.00"); // ₦100
    private static final BigDecimal BILL_PAYMENT_FEE_PERCENTAGE = new BigDecimal("0.25"); // 0.25%
    private static final BigDecimal BILL_PAYMENT_FIXED_FEE = new BigDecimal("25.00"); // ₦25
    private static final BigDecimal ATM_FEE = new BigDecimal("200.00"); // ₦200
    private static final BigDecimal POS_FEE = new BigDecimal("150.00"); // ₦150
    private static final BigDecimal MOBILE_APP_FEE = new BigDecimal("0.00"); // Free
    private static final BigDecimal WEB_APP_FEE = new BigDecimal("0.00"); // Free
    private static final BigDecimal USSD_FEE = new BigDecimal("10.00"); // ₦10
    
    public BigDecimal calculateFee(Transaction transaction) {
        log.info("Calculating fee for transaction: {} type: {} channel: {}", 
                transaction.getReference(), transaction.getType(), transaction.getChannel());
        
        BigDecimal fee = BigDecimal.ZERO;
        
        // Calculate fee based on transaction type and channel
        switch (transaction.getType()) {
            case TRANSFER:
                fee = calculateTransferFee(transaction);
                break;
            case WITHDRAWAL:
                fee = calculateWithdrawalFee(transaction);
                break;
            case BILL_PAYMENT:
                fee = calculateBillPaymentFee(transaction);
                break;
            case DEPOSIT:
                fee = calculateDepositFee(transaction);
                break;
            case REFUND:
                fee = BigDecimal.ZERO; // Refunds are free
                break;
            case FEE:
                fee = BigDecimal.ZERO; // Fee transactions don't have fees
                break;
            case INTEREST:
                fee = BigDecimal.ZERO; // Interest is free
                break;
            case ADJUSTMENT:
                fee = BigDecimal.ZERO; // Adjustments are free
                break;
            case CASHBACK:
                fee = BigDecimal.ZERO; // Cashback is free
                break;
            case REVERSAL:
                fee = BigDecimal.ZERO; // Reversals are free
                break;
            default:
                fee = BigDecimal.ZERO;
        }
        
        // Apply channel-specific fees
        BigDecimal channelFee = calculateChannelFee(transaction.getChannel());
        fee = fee.add(channelFee);
        
        // Ensure minimum fee of ₦0
        fee = fee.max(BigDecimal.ZERO);
        
        log.info("Calculated fee for transaction {}: ₦{}", transaction.getReference(), fee);
        return fee;
    }
    
    private BigDecimal calculateTransferFee(Transaction transaction) {
        BigDecimal amount = transaction.getAmount();
        
        // Calculate percentage fee
        BigDecimal percentageFee = amount.multiply(TRANSFER_FEE_PERCENTAGE)
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        
        // Take the higher of percentage fee or fixed fee
        BigDecimal fee = percentageFee.max(TRANSFER_FIXED_FEE);
        
        // Cap the fee at ₦2,500
        return fee.min(new BigDecimal("2500.00"));
    }
    
    private BigDecimal calculateWithdrawalFee(Transaction transaction) {
        BigDecimal amount = transaction.getAmount();
        
        // Calculate percentage fee
        BigDecimal percentageFee = amount.multiply(WITHDRAWAL_FEE_PERCENTAGE)
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        
        // Take the higher of percentage fee or fixed fee
        BigDecimal fee = percentageFee.max(WITHDRAWAL_FIXED_FEE);
        
        // Cap the fee at ₦5,000
        return fee.min(new BigDecimal("5000.00"));
    }
    
    private BigDecimal calculateBillPaymentFee(Transaction transaction) {
        BigDecimal amount = transaction.getAmount();
        
        // Calculate percentage fee
        BigDecimal percentageFee = amount.multiply(BILL_PAYMENT_FEE_PERCENTAGE)
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        
        // Take the higher of percentage fee or fixed fee
        BigDecimal fee = percentageFee.max(BILL_PAYMENT_FIXED_FEE);
        
        // Cap the fee at ₦1,000
        return fee.min(new BigDecimal("1000.00"));
    }
    
    private BigDecimal calculateDepositFee(Transaction transaction) {
        // Deposits are generally free, but some channels may have fees
        return BigDecimal.ZERO;
    }
    
    private BigDecimal calculateChannelFee(TransactionChannel channel) {
        switch (channel) {
            case ATM:
                return ATM_FEE;
            case POS:
                return POS_FEE;
            case USSD:
                return USSD_FEE;
            case MOBILE_APP:
            case WEB_APP:
            case API:
                return MOBILE_APP_FEE; // Free
            case SYSTEM:
            case ADMIN:
                return BigDecimal.ZERO; // Free
            case BANK_TRANSFER:
            case CARD:
            case BRANCH:
            case AGENT:
                return BigDecimal.ZERO; // Free
            default:
                return BigDecimal.ZERO;
        }
    }
    
    public String getFeeType(Transaction transaction) {
        if (transaction.getType() == TransactionType.TRANSFER) {
            return "TRANSFER_FEE";
        } else if (transaction.getType() == TransactionType.WITHDRAWAL) {
            return "WITHDRAWAL_FEE";
        } else if (transaction.getType() == TransactionType.BILL_PAYMENT) {
            return "BILL_PAYMENT_FEE";
        } else if (transaction.getChannel() == TransactionChannel.ATM) {
            return "ATM_FEE";
        } else if (transaction.getChannel() == TransactionChannel.POS) {
            return "POS_FEE";
        } else if (transaction.getChannel() == TransactionChannel.USSD) {
            return "USSD_FEE";
        } else {
            return "NO_FEE";
        }
    }
    
    public boolean isFeeApplicable(Transaction transaction) {
        return calculateFee(transaction).compareTo(BigDecimal.ZERO) > 0;
    }
}
