package com.xypay.xypay.service;

import com.xypay.xypay.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class DashboardService {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private LoanRepository loanRepository;
    
    // Real-time metrics (in-memory for demo)
    private final AtomicLong transactionCount = new AtomicLong(0);
    private final AtomicLong activeUsers = new AtomicLong(0);
    private final AtomicLong currentTPS = new AtomicLong(0);
    private final AtomicLong avgResponseTime = new AtomicLong(0);
    
    /**
     * Get comprehensive dashboard metrics
     */
    public Map<String, Object> getDashboardMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // Basic counts
            long totalUsers = userRepository.count();
            long totalWallets = walletRepository.count();
            long totalTransactions = transactionRepository.count();
            long totalLoans = loanRepository.count();
            
            // Transaction volume (last 30 days)
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            BigDecimal transactionVolume = transactionRepository.getTotalVolumeBetween(thirtyDaysAgo, LocalDateTime.now());
            if (transactionVolume == null) {
                transactionVolume = BigDecimal.ZERO;
            }
            
            // Active users (last 24 hours) - using existing method
            LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
            long activeUsers24h = userRepository.countByCreatedAtBetween(twentyFourHoursAgo, LocalDateTime.now());
            
            // System performance
            long avgResponseTimeMs = getAverageResponseTime();
            String riskScore = calculateRiskScore();
            
            metrics.put("totalUsers", totalUsers);
            metrics.put("totalWallets", totalWallets);
            metrics.put("totalTransactions", totalTransactions);
            metrics.put("totalLoans", totalLoans);
            metrics.put("transactionVolume", transactionVolume);
            metrics.put("activeCustomers", activeUsers24h);
            metrics.put("avgResponseTime", avgResponseTimeMs);
            metrics.put("riskScore", riskScore);
            metrics.put("systemUptime", "99.99%");
            metrics.put("lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
            
            // Growth metrics
            metrics.put("transactionVolumeChange", "+12.5%");
            metrics.put("customerGrowth", "+8.2%");
            metrics.put("responseTimeChange", "-15.3%");
            metrics.put("riskStatus", "All systems normal");
            
            logger.info("Dashboard metrics generated successfully");
            
        } catch (Exception e) {
            logger.error("Error generating dashboard metrics: {}", e.getMessage());
            metrics.put("error", "Unable to load metrics");
        }
        
        return metrics;
    }
    
    /**
     * Get real-time metrics
     */
    public Map<String, Object> getRealTimeMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Simulate real-time data
        long tps = currentTPS.get();
        long responseTime = avgResponseTime.get();
        long activeUsersCount = activeUsers.get();
        
        // Generate realistic variations
        tps = Math.max(100, tps + (long)(Math.random() * 200 - 100));
        responseTime = Math.max(20, responseTime + (long)(Math.random() * 20 - 10));
        activeUsersCount = Math.max(1000, activeUsersCount + (long)(Math.random() * 1000 - 500));
        
        // Update atomic values
        currentTPS.set(tps);
        avgResponseTime.set(responseTime);
        activeUsers.set(activeUsersCount);
        
        metrics.put("tps", tps);
        metrics.put("responseTime", responseTime);
        metrics.put("activeUsers", activeUsersCount);
        metrics.put("timestamp", System.currentTimeMillis());
        
        return metrics;
    }
    
    /**
     * Get performance metrics
     */
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // System performance metrics
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        double memoryUsage = (double) usedMemory / totalMemory * 100;
        
        // CPU usage (simulated)
        double cpuUsage = 40 + Math.random() * 30; // 40-70%
        
        // Database connections (simulated)
        int dbConnections = 15 + (int)(Math.random() * 10); // 15-25
        
        metrics.put("cpuUsage", Math.round(cpuUsage * 100.0) / 100.0);
        metrics.put("memoryUsage", Math.round(memoryUsage * 100.0) / 100.0);
        metrics.put("dbConnections", dbConnections);
        metrics.put("diskUsage", 45 + Math.random() * 20); // 45-65%
        metrics.put("networkLatency", 5 + Math.random() * 10); // 5-15ms
        
        return metrics;
    }
    
    /**
     * Calculate system risk score
     */
    private String calculateRiskScore() {
        // Simple risk calculation based on various factors
        long totalTransactions = transactionRepository.count();
        long failedTransactions = transactionRepository.countFailedTransactions();
        
        if (totalTransactions == 0) {
            return "Low";
        }
        
        double failureRate = (double) failedTransactions / totalTransactions;
        
        if (failureRate < 0.01) {
            return "Low";
        } else if (failureRate < 0.05) {
            return "Medium";
        } else {
            return "High";
        }
    }
    
    /**
     * Get average response time
     */
    private long getAverageResponseTime() {
        // Simulate response time calculation
        return 25 + (long)(Math.random() * 30); // 25-55ms
    }
    
    /**
     * Update real-time metrics every 5 seconds
     */
    @Scheduled(fixedRate = 5000)
    public void updateRealTimeMetrics() {
        try {
            // Update transaction count
            long newTransactionCount = transactionRepository.count();
            long oldCount = transactionCount.get();
            long tps = (newTransactionCount - oldCount) / 5; // Transactions per second
            currentTPS.set(Math.max(0, tps));
            transactionCount.set(newTransactionCount);
            
            // Update active users (last hour)
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            long activeUsersCount = userRepository.countByCreatedAtBetween(oneHourAgo, LocalDateTime.now());
            activeUsers.set(activeUsersCount);
            
            // Update response time
            long responseTime = getAverageResponseTime();
            avgResponseTime.set(responseTime);
            
        } catch (Exception e) {
            logger.error("Error updating real-time metrics: {}", e.getMessage());
        }
    }
    
    /**
     * Get transaction trends data
     */
    public Map<String, Object> getTransactionTrends() {
        Map<String, Object> trends = new HashMap<>();
        
        // Generate hourly data for last 24 hours
        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();
        
        for (int i = 23; i >= 0; i--) {
            LocalDateTime hour = LocalDateTime.now().minusHours(i);
            LocalDateTime nextHour = hour.plusHours(1);
            labels.add(hour.getHour() + ":00");
            
            // Get transaction count for this hour using existing method
            long count = transactionRepository.countByCreatedAtBetween(hour, nextHour);
            data.add(count);
        }
        
        trends.put("labels", labels);
        trends.put("data", data);
        
        return trends;
    }
    
    /**
     * Get system alerts
     */
    public List<Map<String, Object>> getSystemAlerts() {
        List<Map<String, Object>> alerts = new ArrayList<>();
        
        // Check for various system conditions
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        double memoryUsage = (double) (totalMemory - freeMemory) / totalMemory * 100;
        
        if (memoryUsage > 80) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("type", "warning");
            alert.put("message", "High memory usage: " + Math.round(memoryUsage) + "%");
            alert.put("timestamp", LocalDateTime.now());
            alerts.add(alert);
        }
        
        // Check transaction failure rate
        long totalTransactions = transactionRepository.count();
        long failedTransactions = transactionRepository.countFailedTransactions();
        
        if (totalTransactions > 0) {
            double failureRate = (double) failedTransactions / totalTransactions;
            if (failureRate > 0.05) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("type", "error");
                alert.put("message", "High transaction failure rate: " + Math.round(failureRate * 100) + "%");
                alert.put("timestamp", LocalDateTime.now());
                alerts.add(alert);
            }
        }
        
        return alerts;
    }
}
