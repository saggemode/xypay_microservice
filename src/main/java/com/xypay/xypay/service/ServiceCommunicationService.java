package com.xypay.xypay.service;

import com.xypay.xypay.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import io.micrometer.core.instrument.Timer;

@Service
public class ServiceCommunicationService {
    
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
    
    @Autowired
    private CircuitBreakerService circuitBreakerService;
    
    @Autowired
    private MonitoringService monitoringService;
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    
    /**
     * Process transaction with all related services
     */
    public Map<String, Object> processTransactionWithServices(Map<String, Object> transactionData) {
        Timer.Sample sample = monitoringService.startServiceTimer("transaction-processing", "process-transaction");
        
        try {
            // 1. Validate customer
            Long customerId = Long.valueOf(transactionData.get("customerId").toString());
            Map<String, Object> customer = circuitBreakerService.executeWithRetryAndCircuitBreaker(
                "customer-service", 
                () -> customerServiceClient.getCustomerById(customerId)
            );
            
            // 2. Validate account
            String accountNumber = transactionData.get("accountNumber").toString();
            Map<String, Object> account = circuitBreakerService.executeWithRetryAndCircuitBreaker(
                "account-service",
                () -> accountServiceClient.getAccountByNumber(accountNumber)
            );
            
            // 3. Create transaction
            Map<String, Object> transaction = circuitBreakerService.executeWithRetryAndCircuitBreaker(
                "transaction-service",
                () -> transactionServiceClient.createTransaction(transactionData)
            );
            
            // 4. Update account balance (async)
            CompletableFuture.runAsync(() -> {
                try {
                    circuitBreakerService.executeWithRetryAndCircuitBreaker(
                        "account-service",
                        () -> {
                            if ("DEBIT".equals(transactionData.get("type"))) {
                                return accountServiceClient.debitAccount(
                                    Long.valueOf(account.get("id").toString()),
                                    java.math.BigDecimal.valueOf(Double.parseDouble(transactionData.get("amount").toString()))
                                );
                            } else {
                                return accountServiceClient.creditAccount(
                                    Long.valueOf(account.get("id").toString()),
                                    java.math.BigDecimal.valueOf(Double.parseDouble(transactionData.get("amount").toString()))
                                );
                            }
                        }
                    );
                } catch (Exception e) {
                    monitoringService.recordError("account-update-failed", "account-service", "update-balance");
                }
            }, executorService);
            
            // 5. Send notification (async)
            CompletableFuture.runAsync(() -> {
                try {
                    circuitBreakerService.executeWithRetryAndCircuitBreaker(
                        "notification-service",
                        () -> notificationServiceClient.sendNotification(
                            customerId,
                            "Transaction Completed",
                            "Your transaction has been processed successfully",
                            "TRANSACTION_SUCCESS",
                            Map.of("transactionId", transaction.get("id"))
                        )
                    );
                } catch (Exception e) {
                    monitoringService.recordError("notification-failed", "notification-service", "send-notification");
                }
            }, executorService);
            
            // 6. Update analytics (async)
            CompletableFuture.runAsync(() -> {
                try {
                    circuitBreakerService.executeWithRetryAndCircuitBreaker(
                        "analytics-service",
                        () -> analyticsServiceClient.detectFraud(transactionData)
                    );
                } catch (Exception e) {
                    monitoringService.recordError("analytics-update-failed", "analytics-service", "fraud-detection");
                }
            }, executorService);
            
            // 7. Update treasury (async)
            CompletableFuture.runAsync(() -> {
                try {
                    circuitBreakerService.executeWithRetryAndCircuitBreaker(
                        "treasury-service",
                        () -> treasuryServiceClient.getLiquidityStatus()
                    );
                } catch (Exception e) {
                    monitoringService.recordError("treasury-update-failed", "treasury-service", "liquidity-check");
                }
            }, executorService);
            
            monitoringService.recordServiceCall("transaction-processing", "process-transaction", true);
            return transaction;
            
        } catch (Exception e) {
            monitoringService.recordServiceCall("transaction-processing", "process-transaction", false);
            monitoringService.recordError("transaction-processing-failed", "transaction-service", "process-transaction");
            throw new RuntimeException("Failed to process transaction", e);
        } finally {
            monitoringService.recordServiceDuration(sample, "transaction-processing", "process-transaction");
        }
    }
    
    /**
     * Get comprehensive customer data from all services
     */
    public Map<String, Object> getComprehensiveCustomerData(Long customerId) {
        Timer.Sample sample = monitoringService.startServiceTimer("customer-data", "get-comprehensive-data");
        
        try {
            // Execute all service calls in parallel
            CompletableFuture<Map<String, Object>> customerFuture = CompletableFuture.supplyAsync(() ->
                circuitBreakerService.executeWithRetryAndCircuitBreaker(
                    "customer-service",
                    () -> customerServiceClient.getCustomerById(customerId)
                ), executorService);
            
            CompletableFuture<List<Map<String, Object>>> accountsFuture = CompletableFuture.supplyAsync(() ->
                circuitBreakerService.executeWithRetryAndCircuitBreaker(
                    "account-service",
                    () -> accountServiceClient.getAccountsByUserId(customerId)
                ), executorService);
            
            CompletableFuture<List<Map<String, Object>>> transactionsFuture = CompletableFuture.supplyAsync(() ->
                circuitBreakerService.executeWithRetryAndCircuitBreaker(
                    "transaction-service",
                    () -> transactionServiceClient.getTransactionsByUserId(customerId)
                ), executorService);
            
            CompletableFuture<List<Map<String, Object>>> notificationsFuture = CompletableFuture.supplyAsync(() ->
                circuitBreakerService.executeWithRetryAndCircuitBreaker(
                    "notification-service",
                    () -> notificationServiceClient.getUserNotifications(customerId)
                ), executorService);
            
            CompletableFuture<Map<String, Object>> analyticsFuture = CompletableFuture.supplyAsync(() ->
                circuitBreakerService.executeWithRetryAndCircuitBreaker(
                    "analytics-service",
                    () -> analyticsServiceClient.getCustomerRiskScore(customerId)
                ), executorService);
            
            // Wait for all futures to complete
            CompletableFuture.allOf(customerFuture, accountsFuture, transactionsFuture, 
                                  notificationsFuture, analyticsFuture).join();
            
            Map<String, Object> result = Map.of(
                "customer", customerFuture.get(),
                "accounts", accountsFuture.get(),
                "transactions", transactionsFuture.get(),
                "notifications", notificationsFuture.get(),
                "analytics", analyticsFuture.get()
            );
            
            monitoringService.recordServiceCall("customer-data", "get-comprehensive-data", true);
            return result;
            
        } catch (Exception e) {
            monitoringService.recordServiceCall("customer-data", "get-comprehensive-data", false);
            monitoringService.recordError("customer-data-failed", "customer-service", "get-comprehensive-data");
            throw new RuntimeException("Failed to get comprehensive customer data", e);
        } finally {
            monitoringService.recordServiceDuration(sample, "customer-data", "get-comprehensive-data");
        }
    }
    
    /**
     * Health check for all services
     */
    public Map<String, Object> getServicesHealthStatus() {
        Map<String, Object> healthStatus = new java.util.HashMap<>();
        
        String[] services = {"customer-service", "account-service", "transaction-service", 
                           "notification-service", "treasury-service", "analytics-service"};
        
        for (String service : services) {
            try {
                io.github.resilience4j.circuitbreaker.CircuitBreaker.State state = 
                    circuitBreakerService.getCircuitBreakerState(service);
                io.github.resilience4j.circuitbreaker.CircuitBreaker.Metrics metrics = 
                    circuitBreakerService.getCircuitBreakerMetrics(service);
                
                healthStatus.put(service, Map.of(
                    "state", state.toString(),
                    "failureRate", metrics.getFailureRate(),
                    "numberOfBufferedCalls", metrics.getNumberOfBufferedCalls(),
                    "numberOfFailedCalls", metrics.getNumberOfFailedCalls(),
                    "numberOfSuccessfulCalls", metrics.getNumberOfSuccessfulCalls()
                ));
            } catch (Exception e) {
                healthStatus.put(service, Map.of(
                    "state", "UNKNOWN",
                    "error", e.getMessage()
                ));
            }
        }
        
        return healthStatus;
    }
}
