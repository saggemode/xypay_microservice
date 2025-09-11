package com.xypay.xypay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class NIBSSClient {
    
    private static final Logger logger = LoggerFactory.getLogger(NIBSSClient.class);
    
    private final String baseUrl = "https://api.nibss.com"; // Simulated base URL
    private final String apiKey = "simulated_api_key";
    private final Random random = new Random();
    
    /**
     * Simulate account number validation.
     * In production, this would call NIBSS API to validate account details.
     */
    public Map<String, Object> validateAccountNumber(String accountNumber) {
        try {
            // Simulate API delay
            Thread.sleep(500);
            
            // Simulate some validation logic
            if (accountNumber == null || accountNumber.length() != 10) {
                Map<String, Object> result = new HashMap<>();
                result.put("valid", false);
                result.put("error", "Invalid account number format");
                return result;
            }
            
            // Simulate different banks based on account number patterns
            Map<String, String> bankData = getSimulatedBankData(accountNumber);
            
            if (bankData != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("valid", true);
                result.put("accountName", bankData.get("accountName"));
                result.put("bankName", bankData.get("bankName"));
                result.put("bankCode", bankData.get("bankCode"));
                result.put("accountNumber", accountNumber);
                return result;
            } else {
                Map<String, Object> result = new HashMap<>();
                result.put("valid", false);
                result.put("error", "Account not found or invalid");
                return result;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted during account validation", e);
            Map<String, Object> result = new HashMap<>();
            result.put("valid", false);
            result.put("error", "Validation interrupted");
            return result;
        }
    }
    
    /**
     * Simulate bank data based on account number patterns.
     * In production, this would be replaced with real NIBSS API calls.
     */
    private Map<String, String> getSimulatedBankData(String accountNumber) {
        Map<String, String> bankData = new HashMap<>();
        
        // Simulate different banks based on account number
        // This is just for demonstration - real implementation would use actual bank codes
        
        // Access Bank pattern (simulated)
        if (accountNumber.startsWith("00")) {
            bankData.put("accountName", "John Doe");
            bankData.put("bankName", "Access Bank");
            bankData.put("bankCode", "044");
            return bankData;
        }
        // GT Bank pattern (simulated)
        else if (accountNumber.startsWith("01")) {
            bankData.put("accountName", "Jane Smith");
            bankData.put("bankName", "GT Bank");
            bankData.put("bankCode", "058");
            return bankData;
        }
        // First Bank pattern (simulated)
        else if (accountNumber.startsWith("02")) {
            bankData.put("accountName", "Michael Johnson");
            bankData.put("bankName", "First Bank");
            bankData.put("bankCode", "011");
            return bankData;
        }
        // Zenith Bank pattern (simulated)
        else if (accountNumber.startsWith("03")) {
            bankData.put("accountName", "Sarah Wilson");
            bankData.put("bankName", "Zenith Bank");
            bankData.put("bankCode", "057");
            return bankData;
        }
        // UBA pattern (simulated)
        else if (accountNumber.startsWith("04")) {
            bankData.put("accountName", "David Brown");
            bankData.put("bankName", "UBA");
            bankData.put("bankCode", "033");
            return bankData;
        }
        // Ecobank pattern (simulated)
        else if (accountNumber.startsWith("05")) {
            bankData.put("accountName", "Lisa Davis");
            bankData.put("bankName", "Ecobank");
            bankData.put("bankCode", "050");
            return bankData;
        }
        // Union Bank pattern (simulated)
        else if (accountNumber.startsWith("06")) {
            bankData.put("accountName", "Robert Miller");
            bankData.put("bankName", "Union Bank");
            bankData.put("bankCode", "032");
            return bankData;
        }
        // Fidelity Bank pattern (simulated)
        else if (accountNumber.startsWith("07")) {
            bankData.put("accountName", "Emma Wilson");
            bankData.put("bankName", "Fidelity Bank");
            bankData.put("bankCode", "070");
            return bankData;
        }
        // Sterling Bank pattern (simulated)
        else if (accountNumber.startsWith("08")) {
            bankData.put("accountName", "James Taylor");
            bankData.put("bankName", "Sterling Bank");
            bankData.put("bankCode", "232");
            return bankData;
        }
        // Wema Bank pattern (simulated)
        else if (accountNumber.startsWith("09")) {
            bankData.put("accountName", "Mary Anderson");
            bankData.put("bankName", "Wema Bank");
            bankData.put("bankCode", "035");
            return bankData;
        }
        else {
            // Random bank for other patterns
            String[][] banks = {
                {"Polaris Bank", "076"},
                {"Keystone Bank", "082"},
                {"Stanbic IBTC", "221"},
                {"FCMB", "214"},
                {"Heritage Bank", "030"}
            };
            
            String[][] names = {
                {"Alex Johnson"},
                {"Taylor Swift"},
                {"Chris Martin"},
                {"Emma Stone"},
                {"Ryan Reynolds"}
            };
            
            String[] bank = banks[random.nextInt(banks.length)];
            String[] name = names[random.nextInt(names.length)];
            
            bankData.put("accountName", name[0]);
            bankData.put("bankName", bank[0]);
            bankData.put("bankCode", bank[1]);
            return bankData;
        }
    }
    
    /**
     * Simulate sending an interbank transfer via NIBSS.
     * Returns a map with status and transaction reference.
     */
    public Map<String, Object> sendInterbankTransfer(String senderAccount, String recipientBankCode, 
                                                     String recipientAccount, double amount, String narration) {
        try {
            Thread.sleep(1000);
            
            Map<String, Object> result = new HashMap<>();
            if (random.nextDouble() < 0.95) {
                result.put("status", "success");
                result.put("nibssReference", "NIBSS-" + (100000 + random.nextInt(900000)));
                result.put("message", "Transfer successful");
            } else {
                result.put("status", "failed");
                result.put("nibssReference", null);
                result.put("message", "NIBSS transfer failed");
            }
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted during interbank transfer", e);
            Map<String, Object> result = new HashMap<>();
            result.put("status", "failed");
            result.put("nibssReference", null);
            result.put("message", "Transfer interrupted");
            return result;
        }
    }
    
    /**
     * Simulate checking the status of a NIBSS transfer.
     */
    public Map<String, Object> checkTransferStatus(String nibssReference) {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("nibssReference", nibssReference);
        result.put("message", "Transfer completed");
        return result;
    }
    
    /**
     * Simulate a NIBSS e-BillsPay bill payment.
     */
    public Map<String, Object> payBill(String customerAccount, String billerCode, 
                                       double amount, String billReference, String narration) {
        try {
            Thread.sleep(1000);
            
            Map<String, Object> result = new HashMap<>();
            if (random.nextDouble() < 0.97) {
                result.put("status", "success");
                result.put("billReference", billReference != null ? billReference : 
                           "BILL-" + (100000 + random.nextInt(900000)));
                result.put("message", "Bill payment successful");
            } else {
                result.put("status", "failed");
                result.put("billReference", billReference != null ? billReference : null);
                result.put("message", "Bill payment failed");
            }
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted during bill payment", e);
            Map<String, Object> result = new HashMap<>();
            result.put("status", "failed");
            result.put("billReference", null);
            result.put("message", "Bill payment interrupted");
            return result;
        }
    }
    
    /**
     * Simulate BVN verification via NIBSS.
     */
    public Map<String, Object> verifyBVN(String bvn) {
        try {
            Thread.sleep(500);
            
            Map<String, Object> result = new HashMap<>();
            if (bvn != null && bvn.length() == 11 && bvn.matches("\\d+")) {
                result.put("status", "success");
                result.put("bvn", bvn);
                result.put("message", "BVN is valid");
                result.put("customerName", "Simulated User " + bvn.substring(7));
            } else {
                result.put("status", "failed");
                result.put("bvn", bvn);
                result.put("message", "Invalid BVN");
            }
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted during BVN verification", e);
            Map<String, Object> result = new HashMap<>();
            result.put("status", "failed");
            result.put("bvn", bvn);
            result.put("message", "BVN verification interrupted");
            return result;
        }
    }
    
    /**
     * Simulate setting up a NIBSS direct debit mandate.
     */
    public Map<String, Object> setupDirectDebit(String customerAccount, double amount, String mandateReference) {
        try {
            Thread.sleep(1000);
            
            Map<String, Object> result = new HashMap<>();
            if (random.nextDouble() < 0.98) {
                result.put("status", "success");
                result.put("mandateReference", mandateReference != null ? mandateReference : 
                           "MANDATE-" + (100000 + random.nextInt(900000)));
                result.put("message", "Direct debit mandate setup successful");
            } else {
                result.put("status", "failed");
                result.put("mandateReference", mandateReference != null ? mandateReference : null);
                result.put("message", "Direct debit mandate setup failed");
            }
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted during direct debit setup", e);
            Map<String, Object> result = new HashMap<>();
            result.put("status", "failed");
            result.put("mandateReference", null);
            result.put("message", "Direct debit setup interrupted");
            return result;
        }
    }
}