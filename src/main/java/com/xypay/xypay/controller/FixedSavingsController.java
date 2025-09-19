package com.xypay.xypay.controller;

import com.xypay.xypay.domain.FixedSavingsAccount;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.dto.*;
import com.xypay.xypay.service.FixedSavingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fixed-savings")
@PreAuthorize("hasRole('USER')")
public class FixedSavingsController {
    
    @Autowired
    private FixedSavingsService fixedSavingsService;
    
    /**
     * Get all fixed savings accounts for the current user
     */
    @GetMapping("/accounts")
    public ResponseEntity<ApiResponse<List<FixedSavingsAccountDTO>>> getUserFixedSavingsAccounts(
            Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            List<FixedSavingsAccountDTO> accounts = fixedSavingsService.getUserFixedSavingsAccounts(currentUser);
            return ResponseEntity.ok(ApiResponse.success(accounts));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve fixed savings accounts"));
        }
    }
    
    /**
     * Get a specific fixed savings account detail
     */
    @GetMapping("/accounts/{id}")
    public ResponseEntity<ApiResponse<FixedSavingsAccountDetailDTO>> getFixedSavingsAccountDetail(
            @PathVariable UUID id, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            FixedSavingsAccountDetailDTO account = fixedSavingsService.getFixedSavingsAccountDetail(id, currentUser);
            if (account == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(ApiResponse.success(account));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve fixed savings account"));
        }
    }
    
    /**
     * Create a new fixed savings account
     */
    @PostMapping("/accounts")
    public ResponseEntity<ApiResponse<FixedSavingsAccountDTO>> createFixedSavingsAccount(
            @Valid @RequestBody FixedSavingsAccountCreateDTO createDTO, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            FixedSavingsAccount account = fixedSavingsService.createFixedSavings(
                currentUser,
                createDTO.getAmount(),
                createDTO.getSource(),
                createDTO.getPurpose(),
                createDTO.getPurposeDescription(),
                createDTO.getStartDate(),
                createDTO.getPaybackDate(),
                createDTO.getAutoRenewalEnabled()
            );
            
            FixedSavingsAccountDTO accountDTO = convertToDTO(account);
            return ResponseEntity.ok(ApiResponse.success(accountDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to create fixed savings account: " + e.getMessage()));
        }
    }
    
    
    /**
     * Calculate interest rate information
     */
    @PostMapping("/calculate-interest")
    public ResponseEntity<ApiResponse<FixedSavingsInterestRateDTO>> calculateInterestRate(
            @Valid @RequestBody FixedSavingsInterestRateDTO rateDTO) {
        try {
            FixedSavingsInterestRateDTO result = fixedSavingsService.calculateInterestRate(rateDTO);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to calculate interest rate"));
        }
    }
    
    /**
     * Get fixed savings choices (purposes and sources)
     */
    @GetMapping("/choices")
    public ResponseEntity<ApiResponse<FixedSavingsChoicesDTO>> getFixedSavingsChoices() {
        try {
            FixedSavingsChoicesDTO choices = fixedSavingsService.getFixedSavingsChoices();
            return ResponseEntity.ok(ApiResponse.success(choices));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve fixed savings choices"));
        }
    }
    
    /**
     * Get fixed savings summary for the current user
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<FixedSavingsSummaryDTO>> getFixedSavingsSummary(
            Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            FixedSavingsSummaryDTO summary = fixedSavingsService.getUserFixedSavingsSummary(currentUser);
            return ResponseEntity.ok(ApiResponse.success(summary));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve fixed savings summary"));
        }
    }
    
    /**
     * Payout a fixed savings account
     */
    @PostMapping("/accounts/{id}/payout")
    public ResponseEntity<ApiResponse<String>> payoutFixedSavings(
            @PathVariable UUID id, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            FixedSavingsAccount fixedSavings = fixedSavingsService.getFixedSavingsAccountByIdAndUser(id, currentUser);
            
            if (fixedSavings == null) {
                return ResponseEntity.notFound().build();
            }
            
            if (!fixedSavings.canBePaidOut()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Fixed savings cannot be paid out"));
            }
            
            boolean success = fixedSavingsService.processMaturityPayout(fixedSavings);
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("Fixed savings paid out successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to pay out fixed savings"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to process payout: " + e.getMessage()));
        }
    }
    
    /**
     * Auto-renew a fixed savings account
     */
    @PostMapping("/accounts/{id}/auto-renew")
    public ResponseEntity<ApiResponse<FixedSavingsAccountDTO>> autoRenewFixedSavings(
            @PathVariable UUID id, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            FixedSavingsAccount fixedSavings = fixedSavingsService.getFixedSavingsAccountByIdAndUser(id, currentUser);
            
            if (fixedSavings == null) {
                return ResponseEntity.notFound().build();
            }
            
            if (!fixedSavings.getAutoRenewalEnabled()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Auto-renewal is not enabled for this fixed savings"));
            }
            
            if (!fixedSavings.isMature()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Fixed savings has not matured yet"));
            }
            
            FixedSavingsAccount newFixedSavings = fixedSavingsService.processAutoRenewal(fixedSavings);
            if (newFixedSavings != null) {
                FixedSavingsAccountDTO accountDTO = convertToDTO(newFixedSavings);
                return ResponseEntity.ok(ApiResponse.success(accountDTO));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to auto-renew fixed savings"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to process auto-renewal: " + e.getMessage()));
        }
    }
    
    /**
     * Get active fixed savings accounts
     */
    @GetMapping("/accounts/active")
    public ResponseEntity<ApiResponse<List<FixedSavingsAccountDTO>>> getActiveFixedSavingsAccounts(
            Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            List<FixedSavingsAccountDTO> accounts = fixedSavingsService.getUserFixedSavingsAccounts(currentUser);
            // Filter for active accounts only
            List<FixedSavingsAccountDTO> activeAccounts = accounts.stream()
                .filter(account -> account.getIsActive() && !account.getIsMatured())
                .toList();
            return ResponseEntity.ok(ApiResponse.success(activeAccounts));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve active fixed savings accounts"));
        }
    }
    
    /**
     * Get matured fixed savings accounts
     */
    @GetMapping("/accounts/matured")
    public ResponseEntity<ApiResponse<List<FixedSavingsAccountDTO>>> getMaturedFixedSavingsAccounts(
            Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            List<FixedSavingsAccountDTO> accounts = fixedSavingsService.getUserFixedSavingsAccounts(currentUser);
            // Filter for matured accounts only
            List<FixedSavingsAccountDTO> maturedAccounts = accounts.stream()
                .filter(FixedSavingsAccountDTO::getIsMatured)
                .toList();
            return ResponseEntity.ok(ApiResponse.success(maturedAccounts));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve matured fixed savings accounts"));
        }
    }
    
    /**
     * Get matured but unpaid fixed savings accounts
     */
    @GetMapping("/accounts/matured-unpaid")
    public ResponseEntity<ApiResponse<List<FixedSavingsAccountDTO>>> getMaturedUnpaidFixedSavingsAccounts(
            Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            List<FixedSavingsAccountDTO> accounts = fixedSavingsService.getUserFixedSavingsAccounts(currentUser);
            // Filter for matured but unpaid accounts only
            List<FixedSavingsAccountDTO> maturedUnpaidAccounts = accounts.stream()
                .filter(account -> account.getIsMatured() && !account.getIsPaidOut())
                .toList();
            return ResponseEntity.ok(ApiResponse.success(maturedUnpaidAccounts));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve matured unpaid fixed savings accounts"));
        }
    }
    
    /**
     * Search fixed savings accounts
     */
    @GetMapping("/accounts/search")
    public ResponseEntity<ApiResponse<List<FixedSavingsAccountDTO>>> searchFixedSavingsAccounts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String purpose,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String status,
            Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            List<FixedSavingsAccountDTO> accounts = fixedSavingsService.getUserFixedSavingsAccounts(currentUser);
            
            // Apply filters
            if (q != null && !q.isEmpty()) {
                accounts = accounts.stream()
                    .filter(account -> account.getAccountNumber().contains(q) || 
                            (account.getPurposeDescription() != null && account.getPurposeDescription().contains(q)))
                    .toList();
            }
            
            if (purpose != null && !purpose.isEmpty()) {
                accounts = accounts.stream()
                    .filter(account -> account.getPurpose().equals(purpose))
                    .toList();
            }
            
            if (source != null && !source.isEmpty()) {
                accounts = accounts.stream()
                    .filter(account -> account.getSource().equals(source))
                    .toList();
            }
            
            if (status != null && !status.isEmpty()) {
                switch (status) {
                    case "active":
                        accounts = accounts.stream()
                            .filter(account -> account.getIsActive() && !account.getIsMatured())
                            .toList();
                        break;
                    case "matured":
                        accounts = accounts.stream()
                            .filter(FixedSavingsAccountDTO::getIsMatured)
                            .toList();
                        break;
                    case "paid_out":
                        accounts = accounts.stream()
                            .filter(FixedSavingsAccountDTO::getIsPaidOut)
                            .toList();
                        break;
                }
            }
            
            return ResponseEntity.ok(ApiResponse.success(accounts));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to search fixed savings accounts"));
        }
    }
    
    private User getCurrentUser(Authentication authentication) {
        // Implementation to get current user from authentication
        // This would depend on your security configuration
        return new User(); // Placeholder
    }
    
    private FixedSavingsAccountDTO convertToDTO(FixedSavingsAccount account) {
        FixedSavingsAccountDTO dto = new FixedSavingsAccountDTO();
        dto.setId(account.getId());
        dto.setUser(account.getUser().getUsername());
        dto.setUserId(account.getUser().getId().toString());
        dto.setAccountNumber(account.getAccountNumber());
        dto.setAmount("₦" + account.getAmount().toPlainString());
        dto.setSource(account.getSource().getCode());
        dto.setPurpose(account.getPurpose().getCode());
        dto.setPurposeDescription(account.getPurposeDescription());
        dto.setStartDate(account.getStartDate());
        dto.setPaybackDate(account.getPaybackDate());
        dto.setAutoRenewalEnabled(account.getAutoRenewalEnabled());
        dto.setIsActive(account.getIsActive());
        dto.setIsMatured(account.getIsMatured());
        dto.setIsPaidOut(account.getIsPaidOut());
        dto.setInterestRate(account.getInterestRate());
        if (account.getTotalInterestEarned() != null) {
            dto.setTotalInterestEarned("₦" + account.getTotalInterestEarned().toPlainString());
        }
        if (account.getMaturityAmount() != null) {
            dto.setMaturityAmount("₦" + account.getMaturityAmount().toPlainString());
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
}