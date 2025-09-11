package com.xypay.xypay.service;

import com.xypay.xypay.domain.TransferChargeControl;
import com.xypay.xypay.domain.VATCharge;
import com.xypay.xypay.repository.TransferChargeControlRepository;
import com.xypay.xypay.repository.VATChargeRepository;
import com.xypay.xypay.util.BankingConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
public class TransferFeeService {
    
    // VAT rate default (7.5%)
    private static final BigDecimal DEFAULT_VAT_RATE = new BigDecimal("0.075");
    
    // Levy constants
    private static final BigDecimal LEVY_THRESHOLD = new BigDecimal("10000");
    private static final BigDecimal LEVY_AMOUNT = new BigDecimal("50");
    
    @Autowired
    private TransferChargeControlRepository transferChargeControlRepository;
    
    @Autowired
    private VATChargeRepository vatChargeRepository;
    
    /**
     * Get the active charge control settings
     * @return TransferChargeControl with active settings or default settings
     */
    public TransferChargeControl getChargeControl() {
        try {
            Optional<TransferChargeControl> control = transferChargeControlRepository.findFirstByOrderByUpdatedAtDesc();
            return control.orElse(new TransferChargeControl(true, true, true));
        } catch (Exception e) {
            // Return default charge control if there's an error
            return new TransferChargeControl(true, true, true);
        }
    }
    
    /**
     * Get the active VAT rate
     * @return BigDecimal representing the active VAT rate
     */
    public BigDecimal getActiveVatRate() {
        try {
            Optional<VATCharge> vat = vatChargeRepository.findFirstByActiveTrueOrderByUpdatedAtDesc();
            if (vat.isPresent() && vat.get().getRate() != null) {
                return vat.get().getRate();
            }
            return DEFAULT_VAT_RATE;
        } catch (Exception e) {
            return DEFAULT_VAT_RATE;
        }
    }
    
    /**
     * Calculate transfer fees based on amount and transfer type
     * @param amount The transfer amount
     * @param transferType The type of transfer (intra/inter)
     * @return Array of [fee, vat, levy] as BigDecimal values
     */
    public BigDecimal[] calculateTransferFees(BigDecimal amount, String transferType) {
        // Validate inputs
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be a positive value");
        }
        
        if (transferType == null || transferType.isEmpty()) {
            transferType = BankingConstants.TransferType.INTERNAL;
        }
        
        // Get VAT rate and charge control settings
        BigDecimal vatRate = getActiveVatRate();
        TransferChargeControl chargeControl = getChargeControl();
        
        boolean levyActive = chargeControl.getLevyActive() != null ? chargeControl.getLevyActive() : true;
        boolean vatActive = chargeControl.getVatActive() != null ? chargeControl.getVatActive() : true;
        boolean feeActive = chargeControl.getFeeActive() != null ? chargeControl.getFeeActive() : true;
        
        BigDecimal fee;
        BigDecimal vat;
        BigDecimal levy;
        
        // Calculate fee based on transfer type
        if (transferType.equals(BankingConstants.TransferType.INTERNAL)) {
            // Internal transfers have no fee
            fee = BigDecimal.ZERO;
            // VAT is calculated on the transfer amount for internal transfers
            vat = vatActive ? amount.multiply(vatRate).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        } else {
            // External transfers have fees based on amount
            if (!feeActive) {
                fee = BigDecimal.ZERO;
            } else if (amount.compareTo(new BigDecimal("5000")) <= 0) {
                fee = new BigDecimal("10.00");
            } else if (amount.compareTo(new BigDecimal("50000")) <= 0) {
                fee = new BigDecimal("25.00");
            } else {
                fee = new BigDecimal("50.00");
            }
            // VAT is calculated on the fee for external transfers
            vat = vatActive ? fee.multiply(vatRate).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        }
        
        // Calculate levy
        if (levyActive && amount.compareTo(LEVY_THRESHOLD) >= 0) {
            // Calculate number of blocks (ceil(amount / LEVY_THRESHOLD))
            BigDecimal blocks = new BigDecimal(
                Math.ceil(amount.divide(LEVY_THRESHOLD, 2, RoundingMode.HALF_UP).doubleValue())
            );
            levy = LEVY_AMOUNT.multiply(blocks).setScale(2, RoundingMode.HALF_UP);
        } else {
            levy = BigDecimal.ZERO;
        }
        
        return new BigDecimal[]{fee, vat, levy};
    }
    
    /**
     * Calculate total charges (fee + vat + levy)
     * @param amount The transfer amount
     * @param transferType The type of transfer (intra/inter)
     * @return Total charges as BigDecimal
     */
    public BigDecimal calculateTotalCharges(BigDecimal amount, String transferType) {
        BigDecimal[] charges = calculateTransferFees(amount, transferType);
        BigDecimal fee = charges[0];
        BigDecimal vat = charges[1];
        BigDecimal levy = charges[2];
        
        return fee.add(vat).add(levy);
    }
}