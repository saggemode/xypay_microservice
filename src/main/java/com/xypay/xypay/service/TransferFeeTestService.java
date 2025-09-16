package com.xypay.xypay.service;

import com.xypay.xypay.domain.TransferChargeControl;
import com.xypay.xypay.domain.VATCharge;
import com.xypay.xypay.repository.TransferChargeControlRepository;
import com.xypay.xypay.repository.VATChargeRepository;
import com.xypay.xypay.util.BankingConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;

@Service
public class TransferFeeTestService {
    
    @Autowired
    private TransferFeeService transferFeeService;
    
    @Autowired
    private TransferChargeControlRepository transferChargeControlRepository;
    
    @Autowired
    private VATChargeRepository vatChargeRepository;
    
    /**
     * Initialize test data
     */
    @PostConstruct
    public void initTestData() {
        // Create default charge control if none exists
        if (transferChargeControlRepository.count() == 0) {
            TransferChargeControl defaultControl = new TransferChargeControl(true, true, true);
            transferChargeControlRepository.save(defaultControl);
        }
        
        // Create default VAT rate if none exists
        if (vatChargeRepository.count() == 0) {
            VATCharge defaultVat = new VATCharge(new BigDecimal("0.075"), true, java.time.LocalDateTime.now());
            vatChargeRepository.save(defaultVat);
        }
    }
    
    /**
     * Demonstrate the transfer fee calculation
     */
    public void demonstrateFeeCalculation() {
        System.out.println("=== Transfer Fee Calculation Demo ===");
        
        // Test internal transfer
        BigDecimal amount1 = new BigDecimal("50000");
        BigDecimal[] fees1 = transferFeeService.calculateTransferFees(amount1, BankingConstants.TransferType.INTERNAL);
        System.out.println("Internal transfer of " + amount1 + ":");
        System.out.println("  Fee: " + fees1[0]);
        System.out.println("  VAT: " + fees1[1]);
        System.out.println("  Levy: " + fees1[2]);
        System.out.println("  Total: " + fees1[0].add(fees1[1]).add(fees1[2]));
        System.out.println();
        
        // Test external transfer
        BigDecimal amount2 = new BigDecimal("25000");
        BigDecimal[] fees2 = transferFeeService.calculateTransferFees(amount2, BankingConstants.TransferType.EXTERNAL);
        System.out.println("External transfer of " + amount2 + ":");
        System.out.println("  Fee: " + fees2[0]);
        System.out.println("  VAT: " + fees2[1]);
        System.out.println("  Levy: " + fees2[2]);
        System.out.println("  Total: " + fees2[0].add(fees2[1]).add(fees2[2]));
        System.out.println();
        
        // Test high-value transfer with levy
        BigDecimal amount3 = new BigDecimal("150000");
        BigDecimal[] fees3 = transferFeeService.calculateTransferFees(amount3, BankingConstants.TransferType.EXTERNAL);
        System.out.println("High-value external transfer of " + amount3 + ":");
        System.out.println("  Fee: " + fees3[0]);
        System.out.println("  VAT: " + fees3[1]);
        System.out.println("  Levy: " + fees3[2]);
        System.out.println("  Total: " + fees3[0].add(fees3[1]).add(fees3[2]));
    }
}