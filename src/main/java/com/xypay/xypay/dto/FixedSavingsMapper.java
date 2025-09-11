package com.xypay.xypay.dto;

import com.xypay.xypay.domain.FixedSavingsAccount;
import com.xypay.xypay.domain.FixedSavingsSettings;
import com.xypay.xypay.domain.FixedSavingsTransaction;
import com.xypay.xypay.domain.FixedSavingsPurpose;
import com.xypay.xypay.domain.FixedSavingsSource;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class FixedSavingsMapper {

    public FixedSavingsAccountDTO toDTO(FixedSavingsAccount account) {
        if (account == null) {
            return null;
        }

        FixedSavingsAccountDTO dto = new FixedSavingsAccountDTO();
        dto.setId(account.getId());
        if (account.getUser() != null) {
            dto.setUser(account.getUser().getUsername());
            dto.setUserId(account.getUser().getId());
        }
        dto.setAccountNumber(account.getAccountNumber());
        if (account.getAmount() != null) {
            dto.setAmount("NGN" + account.getAmount().toPlainString());
        }
        dto.setSource(account.getSource());
        dto.setSourceDisplay(getSourceDisplay(account.getSource()));
        dto.setPurpose(account.getPurpose());
        dto.setPurposeDisplay(getPurposeDisplay(account.getPurpose()));
        dto.setPurposeDescription(account.getPurposeDescription());
        dto.setStartDate(account.getStartDate());
        dto.setPaybackDate(account.getPaybackDate());
        dto.setAutoRenewalEnabled(account.getAutoRenewalEnabled());
        dto.setIsActive(account.getIsActive());
        dto.setIsMatured(account.getIsMatured());
        dto.setIsPaidOut(account.getIsPaidOut());
        dto.setInterestRate(account.getInterestRate());
        if (account.getTotalInterestEarned() != null) {
            dto.setTotalInterestEarned("NGN" + account.getTotalInterestEarned().toPlainString());
        }
        if (account.getMaturityAmount() != null) {
            dto.setMaturityAmount("NGN" + account.getMaturityAmount().toPlainString());
        }
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
        if (account == null) {
            return null;
        }

        FixedSavingsAccountDetailDTO dto = new FixedSavingsAccountDetailDTO();
        dto.setId(account.getId());
        if (account.getUser() != null) {
            dto.setUser(account.getUser().getUsername());
            dto.setUserId(account.getUser().getId());
        }
        dto.setAccountNumber(account.getAccountNumber());
        if (account.getAmount() != null) {
            dto.setAmount("NGN" + account.getAmount().toPlainString());
        }
        dto.setSource(account.getSource());
        dto.setSourceDisplay(getSourceDisplay(account.getSource()));
        dto.setPurpose(account.getPurpose());
        dto.setPurposeDisplay(getPurposeDisplay(account.getPurpose()));
        dto.setPurposeDescription(account.getPurposeDescription());
        dto.setStartDate(account.getStartDate());
        dto.setPaybackDate(account.getPaybackDate());
        dto.setAutoRenewalEnabled(account.getAutoRenewalEnabled());
        dto.setIsActive(account.getIsActive());
        dto.setIsMatured(account.getIsMatured());
        dto.setIsPaidOut(account.getIsPaidOut());
        dto.setInterestRate(account.getInterestRate());
        if (account.getTotalInterestEarned() != null) {
            dto.setTotalInterestEarned("NGN" + account.getTotalInterestEarned().toPlainString());
        }
        if (account.getMaturityAmount() != null) {
            dto.setMaturityAmount("NGN" + account.getMaturityAmount().toPlainString());
        }
        dto.setDurationDays(account.getDurationDays());
        dto.setDaysRemaining(account.getDaysRemaining());
        dto.setIsMature(account.isMature());
        dto.setCanBePaidOut(account.canBePaidOut());
        dto.setCreatedAt(account.getCreatedAt());
        dto.setUpdatedAt(account.getUpdatedAt());
        dto.setMaturedAt(account.getMaturedAt());
        dto.setPaidOutAt(account.getPaidOutAt());

        // Convert transactions if they exist
        if (account.getTransactions() != null) {
            List<FixedSavingsTransactionDTO> transactionDTOs = new ArrayList<>();
            for (FixedSavingsTransaction transaction : account.getTransactions()) {
                transactionDTOs.add(toDTO(transaction));
            }
            dto.setTransactions(transactionDTOs);
        }

        return dto;
    }

    public FixedSavingsTransactionDTO toDTO(FixedSavingsTransaction transaction) {
        if (transaction == null) {
            return null;
        }

        FixedSavingsTransactionDTO dto = new FixedSavingsTransactionDTO();
        dto.setId(transaction.getId());
        if (transaction.getFixedSavingsAccount() != null) {
            dto.setFixedSavingsAccountId(transaction.getFixedSavingsAccount().getId());
        }
        dto.setTransactionType(transaction.getTransactionType() != null ? 
            transaction.getTransactionType().name() : null);
        dto.setTransactionTypeDisplay(getTransactionTypeDisplay(transaction.getTransactionType()));
        dto.setAmount(transaction.getAmount());
        dto.setBalanceBefore(transaction.getBalanceBefore());
        dto.setBalanceAfter(transaction.getBalanceAfter());
        dto.setReference(transaction.getReference());
        dto.setDescription(transaction.getDescription());
        dto.setInterestEarned(transaction.getInterestEarned());
        dto.setInterestRateApplied(transaction.getInterestRateApplied());
        dto.setSourceAccount(transaction.getSourceAccount() != null ? 
            transaction.getSourceAccount().name() : null);
        dto.setSourceAccountDisplay(getSourceAccountDisplay(transaction.getSourceAccount()));
        dto.setSourceTransactionId(transaction.getSourceTransactionId());
        dto.setMetadata(transaction.getMetadata());
        dto.setCreatedAt(transaction.getCreatedAt());

        return dto;
    }

    public FixedSavingsSettingsDTO toDTO(FixedSavingsSettings settings) {
        if (settings == null) {
            return null;
        }

        FixedSavingsSettingsDTO dto = new FixedSavingsSettingsDTO();
        dto.setId(settings.getId());
        if (settings.getUser() != null) {
            dto.setUser(settings.getUser().getUsername());
        }
        dto.setMaturityNotifications(settings.getMaturityNotifications());
        dto.setInterestNotifications(settings.getInterestNotifications());
        dto.setAutoRenewalNotifications(settings.getAutoRenewalNotifications());
        dto.setDefaultAutoRenewal(settings.getDefaultAutoRenewal());
        dto.setDefaultRenewalDuration(settings.getDefaultRenewalDuration());
        dto.setDefaultSource(settings.getDefaultSource() != null ? 
            settings.getDefaultSource().name() : null);
        dto.setDefaultSourceDisplay(getDefaultSourceDisplay(settings.getDefaultSource()));
        dto.setCreatedAt(settings.getCreatedAt());
        dto.setUpdatedAt(settings.getUpdatedAt());

        return dto;
    }

    public List<FixedSavingsPurposeDTO> getPurposeChoices() {
        List<FixedSavingsPurposeDTO> purposes = new ArrayList<>();
        for (FixedSavingsPurpose purpose : FixedSavingsPurpose.values()) {
            purposes.add(new FixedSavingsPurposeDTO(purpose.getCode(), purpose.getDescription()));
        }
        return purposes;
    }

    public List<FixedSavingsSourceDTO> getSourceChoices() {
        List<FixedSavingsSourceDTO> sources = new ArrayList<>();
        for (FixedSavingsSource source : FixedSavingsSource.values()) {
            sources.add(new FixedSavingsSourceDTO(source.getCode(), source.getDescription()));
        }
        return sources;
    }

    private String getPurposeDisplay(String purposeCode) {
        if (purposeCode == null) return null;
        for (FixedSavingsPurpose purpose : FixedSavingsPurpose.values()) {
            if (purpose.getCode().equals(purposeCode)) {
                return purpose.getDescription();
            }
        }
        return purposeCode;
    }

    private String getSourceDisplay(String sourceCode) {
        if (sourceCode == null) return null;
        for (FixedSavingsSource source : FixedSavingsSource.values()) {
            if (source.getCode().equals(sourceCode)) {
                return source.getDescription();
            }
        }
        return sourceCode;
    }

    private String getTransactionTypeDisplay(FixedSavingsTransaction.TransactionType transactionType) {
        if (transactionType == null) return null;
        switch (transactionType) {
            case INITIAL_DEPOSIT: return "Initial Deposit";
            case MATURITY_PAYOUT: return "Maturity Payout";
            case EARLY_WITHDRAWAL: return "Early Withdrawal";
            case INTEREST_CREDIT: return "Interest Credit";
            case AUTO_RENEWAL: return "Auto Renewal";
            default: return transactionType.name();
        }
    }

    private String getSourceAccountDisplay(FixedSavingsTransaction.Source source) {
        if (source == null) return null;
        switch (source) {
            case WALLET: return "Wallet";
            case XYSAVE: return "XySave";
            case BOTH: return "Both Wallet and XySave";
            default: return source.name();
        }
    }

    private String getDefaultSourceDisplay(FixedSavingsSettings.Source source) {
        if (source == null) return null;
        switch (source) {
            case WALLET: return "Wallet";
            case XYSAVE: return "XySave";
            case BOTH: return "Both Wallet and XySave";
            default: return source.name();
        }
    }
}