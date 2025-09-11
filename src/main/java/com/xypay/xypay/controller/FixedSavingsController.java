package com.xypay.xypay.controller;

import com.xypay.xypay.domain.FixedSavingsAccount;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.dto.*;
import com.xypay.xypay.service.FixedSavingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fixed-savings")
public class FixedSavingsController {
    
    @Autowired
    private FixedSavingsService fixedSavingsService;
    
    /**
     * Get all fixed savings accounts for the current user
     */
    @GetMapping("/accounts")
    public ResponseEntity<List<FixedSavingsAccountDTO>> getUserFixedSavingsAccounts() {
        // In a real implementation, you would get the current user from security context
        User currentUser = new User();
        currentUser.setId(1L);
        
        List<FixedSavingsAccountDTO> accounts = fixedSavingsService.getUserFixedSavingsAccounts(currentUser);
        return ResponseEntity.ok(accounts);
    }
    
    /**
     * Get a specific fixed savings account detail
     */
    @GetMapping("/accounts/{id}")
    public ResponseEntity<FixedSavingsAccountDetailDTO> getFixedSavingsAccountDetail(@PathVariable Long id) {
        // In a real implementation, you would get the current user from security context
        User currentUser = new User();
        currentUser.setId(1L);
        
        FixedSavingsAccountDetailDTO account = fixedSavingsService.getFixedSavingsAccountDetail(id, currentUser);
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(account);
    }
    
    /**
     * Create a new fixed savings account
     */
    @PostMapping("/accounts")
    public ResponseEntity<FixedSavingsAccountDTO> createFixedSavingsAccount(@RequestBody FixedSavingsAccountCreateDTO createDTO) {
        // In a real implementation, you would get the current user from security context
        User currentUser = new User();
        currentUser.setId(1L);
        
        // Create the fixed savings account
        FixedSavingsAccount account = fixedSavingsService.createFixedSavingsAccount(createDTO, currentUser);
        
        // Convert to DTO
        FixedSavingsAccountDTO accountDTO = new FixedSavingsAccountDTO();
        accountDTO.setId(account.getId());
        accountDTO.setUser(account.getUser().getUsername());
        accountDTO.setUserId(account.getUser().getId());
        accountDTO.setAccountNumber(account.getAccountNumber());
        accountDTO.setAmount("NGN" + account.getAmount().toPlainString());
        accountDTO.setSource(account.getSource());
        accountDTO.setPurpose(account.getPurpose());
        accountDTO.setPurposeDescription(account.getPurposeDescription());
        accountDTO.setStartDate(account.getStartDate());
        accountDTO.setPaybackDate(account.getPaybackDate());
        accountDTO.setAutoRenewalEnabled(account.getAutoRenewalEnabled());
        accountDTO.setIsActive(account.getIsActive());
        accountDTO.setIsMatured(account.getIsMatured());
        accountDTO.setIsPaidOut(account.getIsPaidOut());
        accountDTO.setInterestRate(account.getInterestRate());
        if (account.getTotalInterestEarned() != null) {
            accountDTO.setTotalInterestEarned("NGN" + account.getTotalInterestEarned().toPlainString());
        }
        if (account.getMaturityAmount() != null) {
            accountDTO.setMaturityAmount("NGN" + account.getMaturityAmount().toPlainString());
        }
        accountDTO.setDurationDays(account.getDurationDays());
        accountDTO.setDaysRemaining(account.getDaysRemaining());
        accountDTO.setIsMature(account.isMature());
        accountDTO.setCanBePaidOut(account.canBePaidOut());
        accountDTO.setCreatedAt(account.getCreatedAt());
        accountDTO.setUpdatedAt(account.getUpdatedAt());
        accountDTO.setMaturedAt(account.getMaturedAt());
        accountDTO.setPaidOutAt(account.getPaidOutAt());
        
        return ResponseEntity.ok(accountDTO);
    }
    
    /**
     * Get fixed savings settings for the current user
     */
    @GetMapping("/settings")
    public ResponseEntity<FixedSavingsSettingsDTO> getFixedSavingsSettings() {
        // In a real implementation, you would get the current user from security context
        User currentUser = new User();
        currentUser.setId(1L);
        
        FixedSavingsSettingsDTO settings = fixedSavingsService.getFixedSavingsSettings(currentUser);
        return ResponseEntity.ok(settings);
    }
    
    /**
     * Update fixed savings settings for the current user
     */
    @PutMapping("/settings")
    public ResponseEntity<FixedSavingsSettingsDTO> updateFixedSavingsSettings(@RequestBody FixedSavingsSettingsDTO settingsDTO) {
        // In a real implementation, you would get the current user from security context
        User currentUser = new User();
        currentUser.setId(1L);
        
        // Update the settings
        fixedSavingsService.updateFixedSavingsSettings(settingsDTO, currentUser);
        
        return ResponseEntity.ok(settingsDTO);
    }
    
    /**
     * Calculate interest rate information
     */
    @PostMapping("/calculate-interest")
    public ResponseEntity<FixedSavingsInterestRateDTO> calculateInterestRate(@RequestBody FixedSavingsInterestRateDTO rateDTO) {
        FixedSavingsInterestRateDTO result = fixedSavingsService.calculateInterestRate(rateDTO);
        return ResponseEntity.ok(result);
    }
    
    /**
     * Get fixed savings choices (purposes and sources)
     */
    @GetMapping("/choices")
    public ResponseEntity<FixedSavingsChoicesDTO> getFixedSavingsChoices() {
        FixedSavingsChoicesDTO choices = fixedSavingsService.getFixedSavingsChoices();
        return ResponseEntity.ok(choices);
    }
    
    /**
     * Get fixed savings summary for the current user
     */
    @GetMapping("/summary")
    public ResponseEntity<FixedSavingsSummaryDTO> getFixedSavingsSummary() {
        // In a real implementation, you would get the current user from security context
        User currentUser = new User();
        currentUser.setId(1L);
        
        FixedSavingsSummaryDTO summary = fixedSavingsService.getUserFixedSavingsSummary(currentUser);
        return ResponseEntity.ok(summary);
    }
    
    /**
     * Payout a fixed savings account
     */
    @PostMapping("/accounts/{id}/payout")
    public ResponseEntity<?> payoutFixedSavings(@PathVariable Long id) {
        // In a real implementation, you would get the current user from security context
        User currentUser = new User();
        currentUser.setId(1L);
        
        // In a real implementation, you would retrieve the fixed savings account
        // and check if it belongs to the current user
        FixedSavingsAccount fixedSavings = new FixedSavingsAccount(); // Placeholder
        fixedSavings.setId(id);
        
        if (!fixedSavings.canBePaidOut()) {
            return ResponseEntity.badRequest().body("{\"error\": \"Fixed savings cannot be paid out\"}");
        }
        
        boolean success = fixedSavingsService.processMaturityPayout(fixedSavings);
        if (success) {
            return ResponseEntity.ok("{\"message\": \"Fixed savings paid out successfully\"}");
        } else {
            return ResponseEntity.status(500).body("{\"error\": \"Failed to pay out fixed savings\"}");
        }
    }
    
    /**
     * Auto-renew a fixed savings account
     */
    @PostMapping("/accounts/{id}/auto-renew")
    public ResponseEntity<?> autoRenewFixedSavings(@PathVariable Long id) {
        // In a real implementation, you would get the current user from security context
        User currentUser = new User();
        currentUser.setId(1L);
        
        // In a real implementation, you would retrieve the fixed savings account
        // and check if it belongs to the current user
        FixedSavingsAccount fixedSavings = new FixedSavingsAccount(); // Placeholder
        fixedSavings.setId(id);
        fixedSavings.setAutoRenewalEnabled(true); // Placeholder
        
        if (!fixedSavings.getAutoRenewalEnabled()) {
            return ResponseEntity.badRequest().body("{\"error\": \"Auto-renewal is not enabled for this fixed savings\"}");
        }
        
        if (!fixedSavings.isMature()) {
            return ResponseEntity.badRequest().body("{\"error\": \"Fixed savings has not matured yet\"}");
        }
        
        FixedSavingsAccount newFixedSavings = fixedSavingsService.processAutoRenewal(fixedSavings);
        if (newFixedSavings != null) {
            FixedSavingsAccountDTO accountDTO = new FixedSavingsAccountDTO();
            accountDTO.setId(newFixedSavings.getId());
            accountDTO.setUser(newFixedSavings.getUser().getUsername());
            accountDTO.setUserId(newFixedSavings.getUser().getId());
            accountDTO.setAccountNumber(newFixedSavings.getAccountNumber());
            accountDTO.setAmount("NGN" + newFixedSavings.getAmount().toPlainString());
            accountDTO.setSource(newFixedSavings.getSource());
            accountDTO.setPurpose(newFixedSavings.getPurpose());
            accountDTO.setPurposeDescription(newFixedSavings.getPurposeDescription());
            accountDTO.setStartDate(newFixedSavings.getStartDate());
            accountDTO.setPaybackDate(newFixedSavings.getPaybackDate());
            accountDTO.setAutoRenewalEnabled(newFixedSavings.getAutoRenewalEnabled());
            accountDTO.setIsActive(newFixedSavings.getIsActive());
            accountDTO.setIsMatured(newFixedSavings.getIsMatured());
            accountDTO.setIsPaidOut(newFixedSavings.getIsPaidOut());
            accountDTO.setInterestRate(newFixedSavings.getInterestRate());
            if (newFixedSavings.getTotalInterestEarned() != null) {
                accountDTO.setTotalInterestEarned("NGN" + newFixedSavings.getTotalInterestEarned().toPlainString());
            }
            if (newFixedSavings.getMaturityAmount() != null) {
                accountDTO.setMaturityAmount("NGN" + newFixedSavings.getMaturityAmount().toPlainString());
            }
            accountDTO.setDurationDays(newFixedSavings.getDurationDays());
            accountDTO.setDaysRemaining(newFixedSavings.getDaysRemaining());
            accountDTO.setIsMature(newFixedSavings.isMature());
            accountDTO.setCanBePaidOut(newFixedSavings.canBePaidOut());
            accountDTO.setCreatedAt(newFixedSavings.getCreatedAt());
            accountDTO.setUpdatedAt(newFixedSavings.getUpdatedAt());
            accountDTO.setMaturedAt(newFixedSavings.getMaturedAt());
            accountDTO.setPaidOutAt(newFixedSavings.getPaidOutAt());
            
            return ResponseEntity.ok("{\"message\": \"Fixed savings auto-renewed successfully\", \"new_fixed_savings\": " + accountDTO.toString() + "}");
        } else {
            return ResponseEntity.status(500).body("{\"error\": \"Failed to auto-renew fixed savings\"}");
        }
    }
    
    /**
     * Get active fixed savings accounts
     */
    @GetMapping("/accounts/active")
    public ResponseEntity<List<FixedSavingsAccountDTO>> getActiveFixedSavingsAccounts() {
        // In a real implementation, you would get the current user from security context
        User currentUser = new User();
        currentUser.setId(1L);
        
        List<FixedSavingsAccountDTO> accounts = fixedSavingsService.getUserFixedSavingsAccounts(currentUser);
        // In a real implementation, you would filter for active accounts only
        return ResponseEntity.ok(accounts);
    }
    
    /**
     * Get matured fixed savings accounts
     */
    @GetMapping("/accounts/matured")
    public ResponseEntity<List<FixedSavingsAccountDTO>> getMaturedFixedSavingsAccounts() {
        // In a real implementation, you would get the current user from security context
        User currentUser = new User();
        currentUser.setId(1L);
        
        List<FixedSavingsAccountDTO> accounts = fixedSavingsService.getUserFixedSavingsAccounts(currentUser);
        // In a real implementation, you would filter for matured accounts only
        return ResponseEntity.ok(accounts);
    }
    
    /**
     * Get matured but unpaid fixed savings accounts
     */
    @GetMapping("/accounts/matured-unpaid")
    public ResponseEntity<List<FixedSavingsAccountDTO>> getMaturedUnpaidFixedSavingsAccounts() {
        // In a real implementation, you would get the current user from security context
        User currentUser = new User();
        currentUser.setId(1L);
        
        List<FixedSavingsAccountDTO> accounts = fixedSavingsService.getUserFixedSavingsAccounts(currentUser);
        // In a real implementation, you would filter for matured but unpaid accounts only
        return ResponseEntity.ok(accounts);
    }
    
    /**
     * Search fixed savings accounts
     */
    @GetMapping("/accounts/search")
    public ResponseEntity<List<FixedSavingsAccountDTO>> searchFixedSavingsAccounts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String purpose,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String status) {
        // In a real implementation, you would get the current user from security context
        User currentUser = new User();
        currentUser.setId(1L);
        
        List<FixedSavingsAccountDTO> accounts = fixedSavingsService.getUserFixedSavingsAccounts(currentUser);
        // In a real implementation, you would apply the search filters
        return ResponseEntity.ok(accounts);
    }
}