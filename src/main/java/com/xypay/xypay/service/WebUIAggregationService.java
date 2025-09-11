package com.xypay.xypay.service;

import com.xypay.xypay.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WebUIAggregationService {
    
    @Autowired
    private CustomerServiceClient customerServiceClient;
    
    @Autowired
    private AccountServiceClient accountServiceClient;
    
    @Autowired
    private TransactionServiceClient transactionServiceClient;
    
    @Autowired
    private NotificationServiceClient notificationServiceClient;
    
    @Autowired
    private TreasuryServiceClient treasuryServiceClient;
    
    @Autowired
    private AnalyticsServiceClient analyticsServiceClient;
    
    /**
     * Get comprehensive dashboard data from all microservices
     */
    public Map<String, Object> getDashboardData() {
        Map<String, Object> dashboardData = new HashMap<>();
        
        try {
            // Get analytics data
            Map<String, Object> analytics = analyticsServiceClient.getDashboardAnalytics();
            dashboardData.put("analytics", analytics);
            
            // Get treasury data
            Map<String, Object> treasury = treasuryServiceClient.getLiquidityStatus();
            dashboardData.put("treasury", treasury);
            
            // Get recent transactions
            List<Map<String, Object>> recentTransactions = transactionServiceClient.getAllTransactions();
            dashboardData.put("recentTransactions", recentTransactions.size() > 10 ? 
                recentTransactions.subList(0, 10) : recentTransactions);
            
            // Get customer count
            List<Map<String, Object>> customers = customerServiceClient.getAllCustomers();
            dashboardData.put("totalCustomers", customers.size());
            
            // Get account count
            List<Map<String, Object>> accounts = accountServiceClient.getAllAccounts();
            dashboardData.put("totalAccounts", accounts.size());
            
        } catch (Exception e) {
            // Handle service failures gracefully
            dashboardData.put("error", "Some services are temporarily unavailable");
            dashboardData.put("analytics", new HashMap<>());
            dashboardData.put("treasury", new HashMap<>());
            dashboardData.put("recentTransactions", List.of());
            dashboardData.put("totalCustomers", 0);
            dashboardData.put("totalAccounts", 0);
        }
        
        return dashboardData;
    }
    
    /**
     * Get customer details with accounts and transactions
     */
    public Map<String, Object> getCustomerDetails(Long customerId) {
        Map<String, Object> customerDetails = new HashMap<>();
        
        try {
            // Get customer info
            Map<String, Object> customer = customerServiceClient.getCustomerById(customerId);
            customerDetails.put("customer", customer);
            
            // Get customer accounts
            List<Map<String, Object>> accounts = accountServiceClient.getAccountsByUserId(customerId);
            customerDetails.put("accounts", accounts);
            
            // Get customer transactions
            List<Map<String, Object>> transactions = transactionServiceClient.getTransactionsByUserId(customerId);
            customerDetails.put("transactions", transactions);
            
            // Get customer notifications
            List<Map<String, Object>> notifications = notificationServiceClient.getUserNotifications(customerId);
            customerDetails.put("notifications", notifications);
            
        } catch (Exception e) {
            customerDetails.put("error", "Failed to load customer details");
            customerDetails.put("customer", new HashMap<>());
            customerDetails.put("accounts", List.of());
            customerDetails.put("transactions", List.of());
            customerDetails.put("notifications", List.of());
        }
        
        return customerDetails;
    }
    
    /**
     * Get transaction details with related data
     */
    public Map<String, Object> getTransactionDetails(Long transactionId) {
        Map<String, Object> transactionDetails = new HashMap<>();
        
        try {
            // Get transaction info
            Map<String, Object> transaction = transactionServiceClient.getTransactionById(transactionId);
            transactionDetails.put("transaction", transaction);
            
            // Get related account details if available
            if (transaction.containsKey("accountId")) {
                Long accountId = Long.valueOf(transaction.get("accountId").toString());
                Map<String, Object> account = accountServiceClient.getAccountByNumber(accountId.toString());
                transactionDetails.put("account", account);
            }
            
            // Get related customer details if available
            if (transaction.containsKey("userId")) {
                Long userId = Long.valueOf(transaction.get("userId").toString());
                Map<String, Object> customer = customerServiceClient.getCustomerById(userId);
                transactionDetails.put("customer", customer);
            }
            
        } catch (Exception e) {
            transactionDetails.put("error", "Failed to load transaction details");
            transactionDetails.put("transaction", new HashMap<>());
            transactionDetails.put("account", new HashMap<>());
            transactionDetails.put("customer", new HashMap<>());
        }
        
        return transactionDetails;
    }
    
    /**
     * Get treasury dashboard data
     */
    public Map<String, Object> getTreasuryDashboard() {
        Map<String, Object> treasuryData = new HashMap<>();
        
        try {
            // Get treasury positions
            List<Map<String, Object>> positions = treasuryServiceClient.getTreasuryPositions();
            treasuryData.put("positions", positions);
            
            // Get liquidity status
            Map<String, Object> liquidity = treasuryServiceClient.getLiquidityStatus();
            treasuryData.put("liquidity", liquidity);
            
            // Get risk metrics
            Map<String, Object> riskMetrics = treasuryServiceClient.getRiskMetrics();
            treasuryData.put("riskMetrics", riskMetrics);
            
        } catch (Exception e) {
            treasuryData.put("error", "Failed to load treasury data");
            treasuryData.put("positions", List.of());
            treasuryData.put("liquidity", new HashMap<>());
            treasuryData.put("riskMetrics", new HashMap<>());
        }
        
        return treasuryData;
    }
}
