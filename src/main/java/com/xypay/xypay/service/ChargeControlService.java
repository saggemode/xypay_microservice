package com.xypay.xypay.service;

import com.xypay.xypay.domain.TransferChargeControl;
import com.xypay.xypay.domain.VATCharge;
import com.xypay.xypay.domain.RateConfiguration;
import com.xypay.xypay.repository.TransferChargeControlRepository;
import com.xypay.xypay.repository.VATChargeRepository;
import com.xypay.xypay.repository.RateConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ChargeControlService {

    @Autowired
    private TransferChargeControlRepository transferChargeControlRepository;

    @Autowired
    private VATChargeRepository vatChargeRepository;

    @Autowired
    private RateConfigurationRepository rateConfigurationRepository;

    /**
     * Get current charge control settings
     */
    public TransferChargeControl getCurrentChargeControls() {
        return transferChargeControlRepository.findFirstByOrderByUpdatedAtDesc()
                .orElse(new TransferChargeControl(true, true, true));
    }

    /**
     * Update charge control settings
     */
    public TransferChargeControl updateChargeControls(Boolean levyActive, Boolean vatActive, Boolean feeActive) {
        TransferChargeControl control = getCurrentChargeControls();
        control.setLevyActive(levyActive);
        control.setVatActive(vatActive);
        control.setFeeActive(feeActive);
        control.setUpdatedAt(LocalDateTime.now());
        return transferChargeControlRepository.save(control);
    }

    /**
     * Get current VAT rate
     */
    public BigDecimal getCurrentVATRate() {
        return vatChargeRepository.findFirstByActiveTrueOrderByUpdatedAtDesc()
                .map(VATCharge::getRate)
                .orElse(new BigDecimal("7.5")); // Default 7.5%
    }

    /**
     * Update VAT rate
     */
    public VATCharge updateVATRate(BigDecimal rate) {
        // Deactivate current VAT charge
        vatChargeRepository.findFirstByActiveTrueOrderByUpdatedAtDesc()
                .ifPresent(vat -> {
                    vat.setActive(false);
                    vatChargeRepository.save(vat);
                });

        // Create new VAT charge
        VATCharge newVAT = new VATCharge(rate, true, LocalDateTime.now());
        return vatChargeRepository.save(newVAT);
    }

    /**
     * Get current transfer fee
     */
    public BigDecimal getCurrentTransferFee() {
        Optional<RateConfiguration> transferFeeConfig = rateConfigurationRepository
                .findByRateCode("TRANSFER_FEE");
        return transferFeeConfig.filter(config -> config.getIsActive())
                .map(RateConfiguration::getBaseRate)
                .orElse(new BigDecimal("50")); // Default ₦50
    }

    /**
     * Update transfer fee
     */
    public RateConfiguration updateTransferFee(BigDecimal fee) {
        Optional<RateConfiguration> existingConfig = rateConfigurationRepository
                .findByRateCode("TRANSFER_FEE");

        if (existingConfig.isPresent()) {
            RateConfiguration config = existingConfig.get();
            config.setBaseRate(fee);
            config.setUpdatedAt(LocalDateTime.now());
            return rateConfigurationRepository.save(config);
        } else {
            // Create new configuration
            RateConfiguration newConfig = new RateConfiguration();
            newConfig.setRateCode("TRANSFER_FEE");
            newConfig.setRateName("Transfer Fee");
            newConfig.setRateType("FEE");
            newConfig.setBaseRate(fee);
            newConfig.setIsActive(true);
            newConfig.setCreatedAt(LocalDateTime.now());
            newConfig.setUpdatedAt(LocalDateTime.now());
            return rateConfigurationRepository.save(newConfig);
        }
    }

    /**
     * Get current CBN levy
     */
    public BigDecimal getCurrentCBNLevy() {
        Optional<RateConfiguration> cbnLevyConfig = rateConfigurationRepository
                .findByRateCode("CBN_LEVY");
        return cbnLevyConfig.filter(config -> config.getIsActive())
                .map(RateConfiguration::getBaseRate)
                .orElse(new BigDecimal("10")); // Default ₦10
    }

    /**
     * Update CBN levy
     */
    public RateConfiguration updateCBNLevy(BigDecimal levy) {
        Optional<RateConfiguration> existingConfig = rateConfigurationRepository
                .findByRateCode("CBN_LEVY");

        if (existingConfig.isPresent()) {
            RateConfiguration config = existingConfig.get();
            config.setBaseRate(levy);
            config.setUpdatedAt(LocalDateTime.now());
            return rateConfigurationRepository.save(config);
        } else {
            // Create new configuration
            RateConfiguration newConfig = new RateConfiguration();
            newConfig.setRateCode("CBN_LEVY");
            newConfig.setRateName("CBN Levy");
            newConfig.setRateType("LEVY");
            newConfig.setBaseRate(levy);
            newConfig.setIsActive(true);
            newConfig.setCreatedAt(LocalDateTime.now());
            newConfig.setUpdatedAt(LocalDateTime.now());
            return rateConfigurationRepository.save(newConfig);
        }
    }

    /**
     * Get tiered fee structure
     */
    public Map<String, BigDecimal> getTieredFees() {
        Map<String, BigDecimal> tieredFees = new HashMap<>();
        
        // Tier 1: 0 - ₦5,000
        Optional<RateConfiguration> tier1Config = rateConfigurationRepository
                .findByRateCode("TIER_1_FEE");
        tieredFees.put("tier1", tier1Config.filter(config -> config.getIsActive())
                .map(RateConfiguration::getBaseRate)
                .orElse(new BigDecimal("25")));

        // Tier 2: ₦5,001 - ₦50,000
        Optional<RateConfiguration> tier2Config = rateConfigurationRepository
                .findByRateCode("TIER_2_FEE");
        tieredFees.put("tier2", tier2Config.filter(config -> config.getIsActive())
                .map(RateConfiguration::getBaseRate)
                .orElse(new BigDecimal("50")));

        // Tier 3: Above ₦50,000
        Optional<RateConfiguration> tier3Config = rateConfigurationRepository
                .findByRateCode("TIER_3_FEE");
        tieredFees.put("tier3", tier3Config.filter(config -> config.getIsActive())
                .map(RateConfiguration::getBaseRate)
                .orElse(new BigDecimal("100")));

        return tieredFees;
    }

    /**
     * Update tiered fees
     */
    public void updateTieredFees(BigDecimal tier1, BigDecimal tier2, BigDecimal tier3) {
        updateTieredFee("TIER_1_FEE", "Tier 1 Fee (0-₦5,000)", tier1);
        updateTieredFee("TIER_2_FEE", "Tier 2 Fee (₦5,001-₦50,000)", tier2);
        updateTieredFee("TIER_3_FEE", "Tier 3 Fee (Above ₦50,000)", tier3);
    }

    private void updateTieredFee(String rateCode, String rateName, BigDecimal fee) {
        Optional<RateConfiguration> existingConfig = rateConfigurationRepository
                .findByRateCode(rateCode);

        if (existingConfig.isPresent()) {
            RateConfiguration config = existingConfig.get();
            config.setBaseRate(fee);
            config.setUpdatedAt(LocalDateTime.now());
            rateConfigurationRepository.save(config);
        } else {
            // Create new configuration
            RateConfiguration newConfig = new RateConfiguration();
            newConfig.setRateCode(rateCode);
            newConfig.setRateName(rateName);
            newConfig.setRateType("FEE");
            newConfig.setBaseRate(fee);
            newConfig.setIsActive(true);
            newConfig.setCreatedAt(LocalDateTime.now());
            newConfig.setUpdatedAt(LocalDateTime.now());
            rateConfigurationRepository.save(newConfig);
        }
    }

    /**
     * Calculate total charges for a transaction
     */
    public Map<String, BigDecimal> calculateCharges(BigDecimal transactionAmount) {
        Map<String, BigDecimal> charges = new HashMap<>();
        TransferChargeControl controls = getCurrentChargeControls();

        BigDecimal transferFee = BigDecimal.ZERO;
        BigDecimal vatAmount = BigDecimal.ZERO;
        BigDecimal levyAmount = BigDecimal.ZERO;

        // Calculate transfer fee based on tiered structure
        if (controls.getFeeActive()) {
            Map<String, BigDecimal> tieredFees = getTieredFees();
            if (transactionAmount.compareTo(new BigDecimal("5000")) <= 0) {
                transferFee = tieredFees.get("tier1");
            } else if (transactionAmount.compareTo(new BigDecimal("50000")) <= 0) {
                transferFee = tieredFees.get("tier2");
            } else {
                transferFee = tieredFees.get("tier3");
            }
        }

        // Calculate VAT on transfer fee
        if (controls.getVatActive()) {
            BigDecimal vatRate = getCurrentVATRate();
            vatAmount = transferFee.multiply(vatRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        }

        // Calculate CBN levy
        if (controls.getLevyActive()) {
            levyAmount = getCurrentCBNLevy();
        }

        charges.put("transferFee", transferFee);
        charges.put("vatAmount", vatAmount);
        charges.put("levyAmount", levyAmount);
        charges.put("totalCharge", transferFee.add(vatAmount).add(levyAmount));

        return charges;
    }

    /**
     * Get charge analytics data
     */
    public Map<String, Object> getChargeAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        // This would typically query the database for real analytics
        // For now, returning mock data
        analytics.put("todayRevenue", new BigDecimal("45230"));
        analytics.put("monthlyRevenue", new BigDecimal("1234567"));
        analytics.put("totalTransactions", 12456);
        analytics.put("averageFee", new BigDecimal("99.12"));
        
        return analytics;
    }

    /**
     * Get all current charge settings
     */
    public Map<String, Object> getAllChargeSettings() {
        Map<String, Object> settings = new HashMap<>();
        
        TransferChargeControl controls = getCurrentChargeControls();
        settings.put("chargeControls", controls);
        settings.put("vatRate", getCurrentVATRate());
        settings.put("transferFee", getCurrentTransferFee());
        settings.put("cbnLevy", getCurrentCBNLevy());
        settings.put("tieredFees", getTieredFees());
        
        return settings;
    }
}
