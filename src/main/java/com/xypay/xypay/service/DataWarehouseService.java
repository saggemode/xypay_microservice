package com.xypay.xypay.service;

import com.xypay.xypay.domain.DataWarehouse;
import com.xypay.xypay.repository.DataWarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class DataWarehouseService {

    private static final Logger logger = LoggerFactory.getLogger(DataWarehouseService.class);
    
    @Autowired
    private DataWarehouseRepository dataWarehouseRepository;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * ETL Process - Extract, Transform, Load data into warehouse
     */
    public void performDailyETL(LocalDate processDate) {
        logger.info("Starting ETL process for date: {}", processDate);
        
        try {
            // Check if data already exists for this date
            Optional<DataWarehouse> existing = dataWarehouseRepository.findByFactDate(processDate);
            DataWarehouse fact = existing.orElse(new DataWarehouse());
            
            // Set date dimensions
            fact.setFactDate(processDate);
            fact.setYear(processDate.getYear());
            fact.setMonth(processDate.getMonthValue());
            fact.setQuarter((processDate.getMonthValue() - 1) / 3 + 1);
            fact.setDayOfWeek(processDate.getDayOfWeek().getValue());
            
            // Extract and transform transaction data
            extractTransactionFacts(fact, processDate);
            
            // Extract and transform customer data
            extractCustomerFacts(fact, processDate);
            
            // Extract and transform wallet data
            extractWalletFacts(fact, processDate);
            
            // Save to warehouse
            dataWarehouseRepository.save(fact);
            
            logger.info("ETL process completed successfully for date: {}", processDate);
            
        } catch (Exception e) {
            logger.error("ETL process failed for date {}: {}", processDate, e.getMessage());
            throw new RuntimeException("ETL process failed", e);
        }
    }

    /**
     * Extract transaction facts
     */
    private void extractTransactionFacts(DataWarehouse fact, LocalDate processDate) {
        String sql = """
            SELECT 
                COUNT(*) as total_transactions,
                COALESCE(SUM(amount), 0) as total_amount,
                COALESCE(SUM(CASE WHEN type = 'credit' THEN 1 ELSE 0 END), 0) as credit_transactions,
                COALESCE(SUM(CASE WHEN type = 'credit' THEN amount ELSE 0 END), 0) as credit_amount,
                COALESCE(SUM(CASE WHEN type = 'debit' THEN 1 ELSE 0 END), 0) as debit_transactions,
                COALESCE(SUM(CASE WHEN type = 'debit' THEN amount ELSE 0 END), 0) as debit_amount,
                COALESCE(SUM(CASE WHEN channel = 'mobile' THEN 1 ELSE 0 END), 0) as mobile_transactions,
                COALESCE(SUM(CASE WHEN channel = 'web' THEN 1 ELSE 0 END), 0) as web_transactions,
                COALESCE(SUM(CASE WHEN channel = 'api' THEN 1 ELSE 0 END), 0) as api_transactions,
                COALESCE(SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END), 0) as successful_transactions,
                COALESCE(SUM(CASE WHEN status = 'failed' THEN 1 ELSE 0 END), 0) as failed_transactions,
                COALESCE(SUM(CASE WHEN status = 'pending' THEN 1 ELSE 0 END), 0) as pending_transactions
            FROM transactions 
            WHERE DATE(created_at) = ?
            """;
        
        Map<String, Object> result = jdbcTemplate.queryForMap(sql, processDate);
        
        fact.setTotalTransactions(((Number) result.get("total_transactions")).longValue());
        fact.setTotalAmount(new BigDecimal(result.get("total_amount").toString()));
        fact.setCreditTransactions(((Number) result.get("credit_transactions")).longValue());
        fact.setCreditAmount(new BigDecimal(result.get("credit_amount").toString()));
        fact.setDebitTransactions(((Number) result.get("debit_transactions")).longValue());
        fact.setDebitAmount(new BigDecimal(result.get("debit_amount").toString()));
        fact.setMobileTransactions(((Number) result.get("mobile_transactions")).longValue());
        fact.setWebTransactions(((Number) result.get("web_transactions")).longValue());
        fact.setApiTransactions(((Number) result.get("api_transactions")).longValue());
        fact.setSuccessfulTransactions(((Number) result.get("successful_transactions")).longValue());
        fact.setFailedTransactions(((Number) result.get("failed_transactions")).longValue());
        fact.setPendingTransactions(((Number) result.get("pending_transactions")).longValue());
    }

    /**
     * Extract customer facts
     */
    private void extractCustomerFacts(DataWarehouse fact, LocalDate processDate) {
        // New customers on this date
        String newCustomersSql = "SELECT COUNT(*) FROM users WHERE DATE(created_at) = ?";
        Long newCustomers = jdbcTemplate.queryForObject(newCustomersSql, Long.class, processDate);
        fact.setNewCustomers(newCustomers != null ? newCustomers : 0L);
        
        // Total customers up to this date
        String totalCustomersSql = "SELECT COUNT(*) FROM users WHERE DATE(created_at) <= ?";
        Long totalCustomers = jdbcTemplate.queryForObject(totalCustomersSql, Long.class, processDate);
        fact.setTotalCustomers(totalCustomers != null ? totalCustomers : 0L);
        
        // Active customers (had transactions in last 30 days)
        String activeCustomersSql = """
            SELECT COUNT(DISTINCT w.user_id) 
            FROM wallets w 
            JOIN transactions t ON w.id = t.wallet_id 
            WHERE t.created_at BETWEEN ? AND ?
            """;
        LocalDate thirtyDaysAgo = processDate.minusDays(30);
        Long activeCustomers = jdbcTemplate.queryForObject(activeCustomersSql, Long.class, thirtyDaysAgo, processDate);
        fact.setActiveCustomers(activeCustomers != null ? activeCustomers : 0L);
        
        // Verified customers
        String verifiedCustomersSql = """
            SELECT COUNT(*) FROM users u 
            LEFT JOIN user_profiles up ON u.id = up.user_id 
            WHERE up.kyc_status = 'VERIFIED' AND DATE(u.created_at) <= ?
            """;
        Long verifiedCustomers = jdbcTemplate.queryForObject(verifiedCustomersSql, Long.class, processDate);
        fact.setVerifiedCustomers(verifiedCustomers != null ? verifiedCustomers : 0L);
    }

    /**
     * Extract wallet facts
     */
    private void extractWalletFacts(DataWarehouse fact, LocalDate processDate) {
        String walletSql = """
            SELECT 
                COALESCE(SUM(balance), 0) as total_balance,
                COALESCE(AVG(balance), 0) as average_balance
            FROM wallets w
            JOIN users u ON w.user_id = u.id
            WHERE DATE(u.created_at) <= ?
            """;
        
        Map<String, Object> result = jdbcTemplate.queryForMap(walletSql, processDate);
        
        fact.setTotalWalletBalance(new BigDecimal(result.get("total_balance").toString()));
        fact.setAverageWalletBalance(new BigDecimal(result.get("average_balance").toString()));
    }

    /**
     * Get business intelligence analytics
     */
    public Map<String, Object> getBusinessIntelligence(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> analytics = new HashMap<>();
        
        // Get data for date range
        List<DataWarehouse> facts = dataWarehouseRepository.findByFactDateBetweenOrderByFactDate(startDate, endDate);
        
        // Calculate totals
        BigDecimal totalRevenue = facts.stream()
            .map(DataWarehouse::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Long totalTransactions = facts.stream()
            .mapToLong(DataWarehouse::getTotalTransactions)
            .sum();
        
        Long totalNewCustomers = facts.stream()
            .mapToLong(DataWarehouse::getNewCustomers)
            .sum();
        
        // Calculate averages
        double avgDailyTransactions = facts.stream()
            .mapToLong(DataWarehouse::getTotalTransactions)
            .average()
            .orElse(0.0);
        
        BigDecimal avgDailyRevenue = totalRevenue.divide(
            BigDecimal.valueOf(facts.size()), 2, BigDecimal.ROUND_HALF_UP);
        
        // Growth trends
        Map<String, Object> trends = calculateTrends(facts);
        
        analytics.put("period", Map.of("start", startDate, "end", endDate));
        analytics.put("totals", Map.of(
            "revenue", totalRevenue,
            "transactions", totalTransactions,
            "new_customers", totalNewCustomers
        ));
        analytics.put("averages", Map.of(
            "daily_transactions", avgDailyTransactions,
            "daily_revenue", avgDailyRevenue
        ));
        analytics.put("trends", trends);
        
        return analytics;
    }

    /**
     * Calculate growth trends
     */
    private Map<String, Object> calculateTrends(List<DataWarehouse> facts) {
        Map<String, Object> trends = new HashMap<>();
        
        if (facts.size() < 2) {
            return trends;
        }
        
        // Transaction trend
        Long firstDayTxns = facts.get(0).getTotalTransactions();
        Long lastDayTxns = facts.get(facts.size() - 1).getTotalTransactions();
        double txnGrowth = firstDayTxns > 0 ? 
            ((double) (lastDayTxns - firstDayTxns) / firstDayTxns) * 100 : 0;
        
        // Revenue trend
        BigDecimal firstDayRevenue = facts.get(0).getTotalAmount();
        BigDecimal lastDayRevenue = facts.get(facts.size() - 1).getTotalAmount();
        double revenueGrowth = firstDayRevenue.compareTo(BigDecimal.ZERO) > 0 ?
            lastDayRevenue.subtract(firstDayRevenue)
                .divide(firstDayRevenue, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue() : 0;
        
        trends.put("transaction_growth_percent", txnGrowth);
        trends.put("revenue_growth_percent", revenueGrowth);
        
        return trends;
    }

    /**
     * Get monthly analytics
     */
    public Map<String, Object> getMonthlyAnalytics(Integer year, Integer month) {
        List<DataWarehouse> facts = dataWarehouseRepository.findByYearAndMonthOrderByFactDate(year, month);
        
        Map<String, Object> analytics = new HashMap<>();
        
        // Daily breakdown
        List<Map<String, Object>> dailyData = new ArrayList<>();
        for (DataWarehouse fact : facts) {
            Map<String, Object> day = new HashMap<>();
            day.put("date", fact.getFactDate());
            day.put("transactions", fact.getTotalTransactions());
            day.put("amount", fact.getTotalAmount());
            day.put("new_customers", fact.getNewCustomers());
            dailyData.add(day);
        }
        
        // Channel distribution
        Long totalMobile = facts.stream().mapToLong(DataWarehouse::getMobileTransactions).sum();
        Long totalWeb = facts.stream().mapToLong(DataWarehouse::getWebTransactions).sum();
        Long totalApi = facts.stream().mapToLong(DataWarehouse::getApiTransactions).sum();
        
        Map<String, Object> channels = new HashMap<>();
        channels.put("mobile", totalMobile);
        channels.put("web", totalWeb);
        channels.put("api", totalApi);
        
        analytics.put("daily_data", dailyData);
        analytics.put("channel_distribution", channels);
        analytics.put("period", Map.of("year", year, "month", month));
        
        return analytics;
    }

    /**
     * Run ETL for date range
     */
    public void runETLForDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            try {
                performDailyETL(currentDate);
                logger.info("ETL completed for {}", currentDate);
            } catch (Exception e) {
                logger.error("ETL failed for {}: {}", currentDate, e.getMessage());
            }
            currentDate = currentDate.plusDays(1);
        }
    }
}
