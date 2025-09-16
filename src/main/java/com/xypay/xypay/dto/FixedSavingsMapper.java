package com.xypay.xypay.dto;

import com.xypay.xypay.domain.FixedSavingsAccount;
import com.xypay.xypay.domain.FixedSavingsSettings;
import com.xypay.xypay.domain.FixedSavingsTransaction;
import com.xypay.xypay.domain.FixedSavingsPurpose;
import com.xypay.xypay.domain.FixedSavingsSource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FixedSavingsMapper {
    
    public FixedSavingsAccountDTO toDTO(FixedSavingsAccount account) {
        if (account == null) return null;
        
        FixedSavingsAccountDTO dto = new FixedSavingsAccountDTO();
        dto.setId(account.getId());
        dto.setUser(account.getUser().getUsername());
        dto.setUserId(account.getUser().getId().toString());
        dto.setAccountNumber(account.getAccountNumber());
        dto.setAmount(formatMoney(account.getAmount()));
        dto.setSource(account.getSource().getCode());
        dto.setSourceDisplay(account.getSource().getDescription());
        dto.setPurpose(account.getPurpose().getCode());
        dto.setPurposeDisplay(account.getPurpose().getDescription());
        dto.setPurposeDescription(account.getPurposeDescription());
        dto.setStartDate(account.getStartDate());
        dto.setPaybackDate(account.getPaybackDate());
        dto.setAutoRenewalEnabled(account.getAutoRenewalEnabled());
        dto.setIsActive(account.getIsActive());
        dto.setIsMatured(account.getIsMatured());
        dto.setIsPaidOut(account.getIsPaidOut());
        dto.setInterestRate(account.getInterestRate());
        dto.setTotalInterestEarned(formatMoney(account.getTotalInterestEarned()));
        dto.setMaturityAmount(formatMoney(account.getMaturityAmount()));
        dto.setDurationDays(account.getDurationDays());
        dto.setDaysRemaining(account.getDaysRemaining());
        dto.setIsMature(account.isMature());
        dto.setCanBePaidOut(account.canBePaidOut());
        dto.setCreatedAt(account.getCreatedAt());
        dto.setUpdatedAt(account.getUpdatedAt());
        dto.setMaturedAt(account.getMaturedAt());
        dto.setPaidOutAt(account.getPaidOutAt());
        
        return dto;
    }
    
    public FixedSavingsAccountDetailDTO toDetailDTO(FixedSavingsAccount account) {
        if (account == null) return null;
        
        FixedSavingsAccountDetailDTO dto = new FixedSavingsAccountDetailDTO();
        dto.setId(account.getId());
        dto.setUser(account.getUser().getUsername());
        dto.setUserId(account.getUser().getId().toString());
        dto.setAccountNumber(account.getAccountNumber());
        dto.setAmount(formatMoney(account.getAmount()));
        dto.setSource(account.getSource().getCode());
        dto.setSourceDisplay(account.getSource().getDescription());
        dto.setPurpose(account.getPurpose().getCode());
        dto.setPurposeDisplay(account.getPurpose().getDescription());
        dto.setPurposeDescription(account.getPurposeDescription());
        dto.setStartDate(account.getStartDate());
        dto.setPaybackDate(account.getPaybackDate());
        dto.setAutoRenewalEnabled(account.getAutoRenewalEnabled());
        dto.setIsActive(account.getIsActive());
        dto.setIsMatured(account.getIsMatured());
        dto.setIsPaidOut(account.getIsPaidOut());
        dto.setInterestRate(account.getInterestRate());
        dto.setTotalInterestEarned(formatMoney(account.getTotalInterestEarned()));
        dto.setMaturityAmount(formatMoney(account.getMaturityAmount()));
        dto.setDurationDays(account.getDurationDays());
        dto.setDaysRemaining(account.getDaysRemaining());
        dto.setIsMature(account.isMature());
        dto.setCanBePaidOut(account.canBePaidOut());
        dto.setCreatedAt(account.getCreatedAt());
        dto.setUpdatedAt(account.getUpdatedAt());
        dto.setMaturedAt(account.getMaturedAt());
        dto.setPaidOutAt(account.getPaidOutAt());
        
        // Add transactions
        if (account.getTransactions() != null) {
            dto.setTransactions(account.getTransactions().stream()
                .map(this::toTransactionDTO)
                .collect(Collectors.toList()));
        }
        
        return dto;
    }
    
    public FixedSavingsTransactionDTO toTransactionDTO(FixedSavingsTransaction transaction) {
        if (transaction == null) return null;
        
        FixedSavingsTransactionDTO dto = new FixedSavingsTransactionDTO();
        dto.setId(transaction.getId());
        dto.setFixedSavingsAccountId(transaction.getFixedSavingsAccount().getId());
        dto.setTransactionType(transaction.getTransactionType().name());
        dto.setTransactionTypeDisplay(transaction.getTransactionType().name().replace("_", " "));
        dto.setAmount(formatMoney(transaction.getAmount()));
        dto.setBalanceBefore(formatMoney(transaction.getBalanceBefore()));
        dto.setBalanceAfter(formatMoney(transaction.getBalanceAfter()));
        dto.setReference(transaction.getReference());
        dto.setDescription(transaction.getDescription());
        dto.setInterestEarned(formatMoney(transaction.getInterestEarned()));
        dto.setInterestRateApplied(transaction.getInterestRateApplied());
        dto.setSourceAccount(transaction.getSourceAccount() != null ? transaction.getSourceAccount().name() : null);
        dto.setSourceAccountDisplay(transaction.getSourceAccount() != null ? transaction.getSourceAccount().name() : null);
        dto.setSourceTransactionId(transaction.getSourceTransactionId());
        dto.setMetadata(transaction.getMetadata());
        dto.setCreatedAt(transaction.getCreatedAt());
        
        return dto;
    }
    
    public FixedSavingsSettingsDTO toDTO(FixedSavingsSettings settings) {
        if (settings == null) return null;
        
        FixedSavingsSettingsDTO dto = new FixedSavingsSettingsDTO();
        dto.setId(settings.getId());
        dto.setUser(settings.getUser().getUsername());
        dto.setMaturityNotifications(settings.getMaturityNotifications());
        dto.setInterestNotifications(settings.getInterestNotifications());
        dto.setAutoRenewalNotifications(settings.getAutoRenewalNotifications());
        dto.setDefaultAutoRenewal(settings.getDefaultAutoRenewal());
        dto.setDefaultRenewalDuration(settings.getDefaultRenewalDuration());
        dto.setDefaultSource(settings.getDefaultSource().name());
        dto.setDefaultSourceDisplay(settings.getDefaultSource().name());
        dto.setCreatedAt(settings.getCreatedAt());
        dto.setUpdatedAt(settings.getUpdatedAt());
        
        return dto;
    }
    
    public List<FixedSavingsChoicesDTO.ChoiceDTO> getPurposeChoices() {
        return Arrays.stream(FixedSavingsPurpose.values())
            .map(purpose -> new FixedSavingsChoicesDTO.ChoiceDTO(purpose.getCode(), purpose.getDescription()))
            .collect(Collectors.toList());
    }
    
    public List<FixedSavingsChoicesDTO.ChoiceDTO> getSourceChoices() {
        return Arrays.stream(FixedSavingsSource.values())
            .map(source -> new FixedSavingsChoicesDTO.ChoiceDTO(source.getCode(), source.getDescription()))
            .collect(Collectors.toList());
    }
    
    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "₦0.00";
        return String.format("₦%,.2f", amount);
    }
}