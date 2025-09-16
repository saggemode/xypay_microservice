package com.xypay.xypay;

import com.xypay.xypay.domain.FixedSavingsAccount;
import com.xypay.xypay.domain.FixedSavingsPurpose;
import com.xypay.xypay.domain.FixedSavingsSource;
import com.xypay.xypay.dto.FixedSavingsAccountCreateDTO;
import com.xypay.xypay.dto.FixedSavingsInterestRateDTO;
import com.xypay.xypay.validation.FixedSavingsValidation;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FixedSavingsConversionTest {

    @Test
    public void testFixedSavingsAccountCreation() {
        // Test that the FixedSavingsAccount entity can be created
        FixedSavingsAccount account = new FixedSavingsAccount();
        account.setAmount(new BigDecimal("10000.00"));
        account.setSource(FixedSavingsSource.WALLET);
        account.setPurpose(FixedSavingsPurpose.EDUCATION);
        account.setStartDate(LocalDate.now().plusDays(1));
        account.setPaybackDate(LocalDate.now().plusDays(31));
        
        // Test interest rate calculation
        BigDecimal interestRate = account.calculateInterestRate();
        assertNotNull(interestRate);
        assertTrue(interestRate.compareTo(BigDecimal.ZERO) > 0);
        
        // Test maturity amount calculation
        BigDecimal maturityAmount = account.calculateMaturityAmount();
        assertNotNull(maturityAmount);
        assertTrue(maturityAmount.compareTo(account.getAmount()) > 0);
        
        // Test duration calculation
        int durationDays = account.getDurationDays();
        assertEquals(30, durationDays);
        
        // Test days remaining calculation
        int daysRemaining = account.getDaysRemaining();
        assertTrue(daysRemaining > 0);
    }

    @Test
    public void testFixedSavingsValidation() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        
        // Test valid data
        FixedSavingsAccountCreateDTO validDTO = new FixedSavingsAccountCreateDTO();
        validDTO.setAmount(new BigDecimal("10000.00"));
        validDTO.setSource(FixedSavingsSource.WALLET);
        validDTO.setPurpose(FixedSavingsPurpose.EDUCATION);
        validDTO.setStartDate(LocalDate.now().plusDays(1));
        validDTO.setPaybackDate(LocalDate.now().plusDays(31));
        
        Set<ConstraintViolation<FixedSavingsAccountCreateDTO>> violations = validator.validate(validDTO);
        assertTrue(violations.isEmpty(), "Valid DTO should have no validation errors");
        
        // Test invalid data - start date in past
        FixedSavingsAccountCreateDTO invalidDTO = new FixedSavingsAccountCreateDTO();
        invalidDTO.setAmount(new BigDecimal("10000.00"));
        invalidDTO.setSource(FixedSavingsSource.WALLET);
        invalidDTO.setPurpose(FixedSavingsPurpose.EDUCATION);
        invalidDTO.setStartDate(LocalDate.now().minusDays(1)); // Past date
        invalidDTO.setPaybackDate(LocalDate.now().plusDays(31));
        
        violations = validator.validate(invalidDTO);
        assertFalse(violations.isEmpty(), "Invalid DTO should have validation errors");
        
        // Test invalid data - payback date before start date
        invalidDTO.setStartDate(LocalDate.now().plusDays(1));
        invalidDTO.setPaybackDate(LocalDate.now().minusDays(1)); // Before start date
        
        violations = validator.validate(invalidDTO);
        assertFalse(violations.isEmpty(), "Invalid DTO should have validation errors");
    }

    @Test
    public void testInterestRateCalculation() {
        FixedSavingsInterestRateDTO rateDTO = new FixedSavingsInterestRateDTO();
        rateDTO.setAmount(new BigDecimal("10000.00"));
        rateDTO.setStartDate(LocalDate.now().plusDays(1));
        rateDTO.setPaybackDate(LocalDate.now().plusDays(91)); // 90 days
        
        // Test that the DTO can be created and validated
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        
        Set<ConstraintViolation<FixedSavingsInterestRateDTO>> violations = validator.validate(rateDTO);
        assertTrue(violations.isEmpty(), "Valid interest rate DTO should have no validation errors");
    }

    @Test
    public void testFixedSavingsEnums() {
        // Test that all enum values are accessible
        assertNotNull(FixedSavingsPurpose.EDUCATION);
        assertNotNull(FixedSavingsPurpose.BUSINESS);
        assertNotNull(FixedSavingsPurpose.INVESTMENT);
        
        assertNotNull(FixedSavingsSource.WALLET);
        assertNotNull(FixedSavingsSource.XYSAVE);
        assertNotNull(FixedSavingsSource.BOTH);
        
        // Test enum methods
        assertEquals("education", FixedSavingsPurpose.EDUCATION.getCode());
        assertEquals("Education", FixedSavingsPurpose.EDUCATION.getDescription());
        
        assertEquals("wallet", FixedSavingsSource.WALLET.getCode());
        assertEquals("Wallet", FixedSavingsSource.WALLET.getDescription());
    }
}
