package com.xypay.xypay.service;

import com.xypay.xypay.domain.Bank;
import com.xypay.xypay.repository.BankRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for handling bank account verification and search operations.
 */
@Service
public class BankAccountService {
    
    private static final Logger logger = LoggerFactory.getLogger(BankAccountService.class);
    
    @Autowired
    private BankRepository bankRepository;
    
    /**
     * Search for banks that have a specific account number.
     * 
     * @param accountNumber The account number to search for
     * @return List of banks with account details
     */
    public List<Map<String, Object>> searchBanksByAccountNumber(String accountNumber) {
        if (accountNumber == null || !accountNumber.matches("\\d+") || accountNumber.length() < 10) {
            return new ArrayList<>();
        }
        
        try {
            // In production, you would integrate with NIBSS API or individual bank APIs
            // For now, we'll use a mock implementation
            
            // Get all available banks from database
            List<Bank> banks = bankRepository.findAll();
            
            List<Map<String, Object>> matchingBanks = new ArrayList<>();
            
            for (Bank bank : banks) {
                // Mock verification - in real scenario, call bank API
                if (verifyAccountWithBank(bank.getCode(), accountNumber)) {
                    Map<String, Object> bankInfo = new HashMap<>();
                    bankInfo.put("bank_code", bank.getCode());
                    bankInfo.put("bank_name", bank.getName());
                    bankInfo.put("account_number", accountNumber);
                    bankInfo.put("account_name", getAccountName(bank.getCode(), accountNumber));
                    bankInfo.put("is_verified", true);
                    bankInfo.put("verification_method", "api"); // In production: settings.DEBUG ? "api" : "nibss"
                    
                    matchingBanks.add(bankInfo);
                }
            }
            
            return matchingBanks;
            
        } catch (Exception e) {
            logger.error("Error searching banks by account number: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Verify if account exists in a specific bank.
     * 
     * @param bankCode Bank code
     * @param accountNumber Account number
     * @return True if account exists, False otherwise
     */
    private boolean verifyAccountWithBank(String bankCode, String accountNumber) {
        try {
            // Mock verification logic
            // In production, this would call the actual bank API or NIBSS API
            
            // Simple mock: if account number ends with certain digits, it exists in certain banks
            int lastDigit = Integer.parseInt(accountNumber.substring(accountNumber.length() - 1));
            
            // Mock mapping: certain account numbers exist in certain banks
            Map<String, Set<Integer>> bankVerificationMap = new HashMap<>();
            bankVerificationMap.put("044", Set.of(0, 1, 2)); // Access Bank
            bankVerificationMap.put("058", Set.of(3, 4, 5)); // GT Bank
            bankVerificationMap.put("011", Set.of(6, 7));    // First Bank
            bankVerificationMap.put("032", Set.of(8));       // Union Bank
            bankVerificationMap.put("033", Set.of(9));       // UBA
            bankVerificationMap.put("057", Set.of(0, 5));    // Zenith Bank
            bankVerificationMap.put("030", Set.of(1, 6));    // Heritage Bank
            bankVerificationMap.put("070", Set.of(2, 7));    // Fidelity Bank
            bankVerificationMap.put("050", Set.of(0, 3, 6)); // Ecobank
            bankVerificationMap.put("076", Set.of(1, 4, 7)); // Polaris Bank
            bankVerificationMap.put("082", Set.of(2, 5, 8)); // Keystone Bank
            bankVerificationMap.put("084", Set.of(0, 2, 4)); // Unity Bank
            bankVerificationMap.put("085", Set.of(1, 3, 5)); // First City Monument Bank
            bankVerificationMap.put("100", Set.of(6, 7, 8)); // Suntrust Bank
            bankVerificationMap.put("101", Set.of(9, 0, 1)); // Providus Bank
            bankVerificationMap.put("102", Set.of(2, 3, 4)); // Titan Trust Bank
            bankVerificationMap.put("103", Set.of(5, 6, 7)); // Globus Bank
            bankVerificationMap.put("104", Set.of(8, 9, 0)); // Parallex Bank
            bankVerificationMap.put("105", Set.of(1, 2, 3)); // Premium Trust Bank
            bankVerificationMap.put("106", Set.of(4, 5, 6)); // Keystone Bank
            
            return bankVerificationMap.getOrDefault(bankCode, Collections.emptySet()).contains(lastDigit);
            
        } catch (Exception e) {
            logger.error("Error verifying account with bank {}: {}", bankCode, e.getMessage());
            return false;
        }
    }
    
    /**
     * Get account holder name for a given bank and account number.
     * 
     * @param bankCode Bank code
     * @param accountNumber Account number
     * @return Account holder name (mock implementation)
     */
    private String getAccountName(String bankCode, String accountNumber) {
        try {
            // Mock account names - in production, this would come from bank API
            // For now, we'll generate a mock name based on account number
            
            // Use last 4 digits to create a mock name
            String lastFour = accountNumber.substring(accountNumber.length() - 4);
            
            // Mock names based on account number pattern
            String[] mockNames = {
                "John Doe",
                "Jane Smith",
                "Michael Johnson",
                "Sarah Williams",
                "David Brown",
                "Lisa Davis",
                "Robert Wilson",
                "Mary Miller",
                "James Taylor",
                "Patricia Anderson"
            };
            
            // Use account number to select a name
            int nameIndex = Integer.parseInt(lastFour) % mockNames.length;
            return mockNames[nameIndex];
            
        } catch (Exception e) {
            logger.error("Error getting account name: {}", e.getMessage());
            return "Account Holder " + accountNumber.substring(accountNumber.length() - 4);
        }
    }
    
    /**
     * Verify account with NIBSS API (production implementation).
     * 
     * @param bankCode Bank code
     * @param accountNumber Account number
     * @return Verification result
     */
    public Map<String, Object> verifyAccountWithNibss(String bankCode, String accountNumber) {
        try {
            // This is where you would integrate with NIBSS API
            // For now, return mock data
            
            // In production, you would:
            // 1. Call NIBSS API with bank_code and account_number
            // 2. Get real account holder name
            // 3. Verify account exists
            
            Map<String, Object> result = new HashMap<>();
            result.put("is_valid", true);
            result.put("account_name", getAccountName(bankCode, accountNumber));
            result.put("bank_code", bankCode);
            result.put("account_number", accountNumber);
            result.put("verification_method", "nibss");
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error verifying account with NIBSS: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("is_valid", false);
            result.put("error", e.getMessage());
            result.put("verification_method", "nibss");
            return result;
        }
    }
    
    /**
     * Get all available banks.
     * 
     * @return List of all banks
     */
    public List<Map<String, Object>> getAllBanks() {
        try {
            List<Bank> banks = bankRepository.findAll().stream()
                .sorted(Comparator.comparing(Bank::getName))
                .collect(Collectors.toList());
            
            return banks.stream()
                .map(bank -> {
                    Map<String, Object> bankInfo = new HashMap<>();
                    bankInfo.put("bank_code", bank.getCode());
                    bankInfo.put("bank_name", bank.getName());
                    bankInfo.put("is_active", bank.getActive());
                    return bankInfo;
                })
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            logger.error("Error getting all banks: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
