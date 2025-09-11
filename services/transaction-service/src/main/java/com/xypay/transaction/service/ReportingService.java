package com.xypay.transaction.service;

import com.xypay.transaction.domain.Transaction;
import com.xypay.transaction.enums.TransactionStatus;
import com.xypay.transaction.enums.TransactionType;
import com.xypay.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportingService {
    
    private final TransactionRepository transactionRepository;
    
    public DailyTransactionReport generateDailyReport(LocalDate date) {
        log.info("Generating daily transaction report for date: {}", date);
        
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        
        // Get all transactions for the day
        List<Transaction> transactions = transactionRepository.findByTimestampBetweenOrderByTimestampDesc(startOfDay, endOfDay);
        
        // Calculate statistics
        DailyTransactionReport report = new DailyTransactionReport();
        report.setDate(date);
        report.setTotalTransactions(transactions.size());
        
        // Calculate totals by type
        Map<TransactionType, BigDecimal> totalsByType = new HashMap<>();
        Map<TransactionStatus, Integer> countsByStatus = new HashMap<>();
        
        for (Transaction transaction : transactions) {
            // Sum amounts by type
            totalsByType.merge(transaction.getType(), transaction.getAmount(), BigDecimal::add);
            
            // Count by status
            countsByStatus.merge(transaction.getStatus(), 1, Integer::sum);
        }
        
        report.setTotalsByType(totalsByType);
        report.setCountsByStatus(countsByStatus);
        
        // Calculate total volume
        BigDecimal totalVolume = transactions.stream()
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setTotalVolume(totalVolume);
        
        // Calculate success rate
        int successfulTransactions = countsByStatus.getOrDefault(TransactionStatus.SUCCESS, 0);
        double successRate = transactions.size() > 0 ? (double) successfulTransactions / transactions.size() * 100 : 0;
        report.setSuccessRate(successRate);
        
        log.info("Daily report generated: {} transactions, ₦{} volume, {:.2f}% success rate", 
                report.getTotalTransactions(), report.getTotalVolume(), report.getSuccessRate());
        
        return report;
    }
    
    public MonthlyTransactionReport generateMonthlyReport(int year, int month) {
        log.info("Generating monthly transaction report for {}-{}", year, month);
        
        LocalDateTime startOfMonth = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime endOfMonth = LocalDate.of(year, month, 1).plusMonths(1).atStartOfDay();
        
        // Get all transactions for the month
        List<Transaction> transactions = transactionRepository.findByTimestampBetweenOrderByTimestampDesc(startOfMonth, endOfMonth);
        
        // Calculate statistics
        MonthlyTransactionReport report = new MonthlyTransactionReport();
        report.setYear(year);
        report.setMonth(month);
        report.setTotalTransactions(transactions.size());
        
        // Calculate daily breakdown
        Map<LocalDate, Integer> dailyCounts = new HashMap<>();
        Map<LocalDate, BigDecimal> dailyVolumes = new HashMap<>();
        
        for (Transaction transaction : transactions) {
            LocalDate transactionDate = transaction.getTimestamp().toLocalDate();
            dailyCounts.merge(transactionDate, 1, Integer::sum);
            dailyVolumes.merge(transactionDate, transaction.getAmount(), BigDecimal::add);
        }
        
        report.setDailyCounts(dailyCounts);
        report.setDailyVolumes(dailyVolumes);
        
        // Calculate totals by type
        Map<TransactionType, BigDecimal> totalsByType = new HashMap<>();
        for (Transaction transaction : transactions) {
            totalsByType.merge(transaction.getType(), transaction.getAmount(), BigDecimal::add);
        }
        report.setTotalsByType(totalsByType);
        
        // Calculate total volume
        BigDecimal totalVolume = transactions.stream()
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setTotalVolume(totalVolume);
        
        log.info("Monthly report generated: {} transactions, ₦{} volume", 
                report.getTotalTransactions(), report.getTotalVolume());
        
        return report;
    }
    
    public ReconciliationReport generateReconciliationReport(LocalDate date) {
        log.info("Generating reconciliation report for date: {}", date);
        
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        
        // Get all successful transactions for the day
        List<Transaction> successfulTransactions = transactionRepository.findByTimestampBetweenAndStatusOrderByTimestampDesc(
            startOfDay, endOfDay, TransactionStatus.SUCCESS);
        
        ReconciliationReport report = new ReconciliationReport();
        report.setDate(date);
        report.setTotalTransactions(successfulTransactions.size());
        
        // Calculate totals by type
        Map<TransactionType, BigDecimal> totalsByType = new HashMap<>();
        for (Transaction transaction : successfulTransactions) {
            totalsByType.merge(transaction.getType(), transaction.getAmount(), BigDecimal::add);
        }
        report.setTotalsByType(totalsByType);
        
        // Calculate total volume
        BigDecimal totalVolume = successfulTransactions.stream()
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setTotalVolume(totalVolume);
        
        // Calculate debit/credit totals
        BigDecimal totalDebits = successfulTransactions.stream()
            .filter(t -> t.getType().isDebit())
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCredits = successfulTransactions.stream()
            .filter(t -> t.getType().isCredit())
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        report.setTotalDebits(totalDebits);
        report.setTotalCredits(totalCredits);
        
        log.info("Reconciliation report generated: {} transactions, ₦{} debits, ₦{} credits", 
                report.getTotalTransactions(), report.getTotalDebits(), report.getTotalCredits());
        
        return report;
    }
    
    public static class DailyTransactionReport {
        private LocalDate date;
        private int totalTransactions;
        private BigDecimal totalVolume;
        private double successRate;
        private Map<TransactionType, BigDecimal> totalsByType;
        private Map<TransactionStatus, Integer> countsByStatus;
        
        // Getters and setters
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        
        public int getTotalTransactions() { return totalTransactions; }
        public void setTotalTransactions(int totalTransactions) { this.totalTransactions = totalTransactions; }
        
        public BigDecimal getTotalVolume() { return totalVolume; }
        public void setTotalVolume(BigDecimal totalVolume) { this.totalVolume = totalVolume; }
        
        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double successRate) { this.successRate = successRate; }
        
        public Map<TransactionType, BigDecimal> getTotalsByType() { return totalsByType; }
        public void setTotalsByType(Map<TransactionType, BigDecimal> totalsByType) { this.totalsByType = totalsByType; }
        
        public Map<TransactionStatus, Integer> getCountsByStatus() { return countsByStatus; }
        public void setCountsByStatus(Map<TransactionStatus, Integer> countsByStatus) { this.countsByStatus = countsByStatus; }
    }
    
    public static class MonthlyTransactionReport {
        private int year;
        private int month;
        private int totalTransactions;
        private BigDecimal totalVolume;
        private Map<LocalDate, Integer> dailyCounts;
        private Map<LocalDate, BigDecimal> dailyVolumes;
        private Map<TransactionType, BigDecimal> totalsByType;
        
        // Getters and setters
        public int getYear() { return year; }
        public void setYear(int year) { this.year = year; }
        
        public int getMonth() { return month; }
        public void setMonth(int month) { this.month = month; }
        
        public int getTotalTransactions() { return totalTransactions; }
        public void setTotalTransactions(int totalTransactions) { this.totalTransactions = totalTransactions; }
        
        public BigDecimal getTotalVolume() { return totalVolume; }
        public void setTotalVolume(BigDecimal totalVolume) { this.totalVolume = totalVolume; }
        
        public Map<LocalDate, Integer> getDailyCounts() { return dailyCounts; }
        public void setDailyCounts(Map<LocalDate, Integer> dailyCounts) { this.dailyCounts = dailyCounts; }
        
        public Map<LocalDate, BigDecimal> getDailyVolumes() { return dailyVolumes; }
        public void setDailyVolumes(Map<LocalDate, BigDecimal> dailyVolumes) { this.dailyVolumes = dailyVolumes; }
        
        public Map<TransactionType, BigDecimal> getTotalsByType() { return totalsByType; }
        public void setTotalsByType(Map<TransactionType, BigDecimal> totalsByType) { this.totalsByType = totalsByType; }
    }
    
    public static class ReconciliationReport {
        private LocalDate date;
        private int totalTransactions;
        private BigDecimal totalVolume;
        private BigDecimal totalDebits;
        private BigDecimal totalCredits;
        private Map<TransactionType, BigDecimal> totalsByType;
        
        // Getters and setters
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        
        public int getTotalTransactions() { return totalTransactions; }
        public void setTotalTransactions(int totalTransactions) { this.totalTransactions = totalTransactions; }
        
        public BigDecimal getTotalVolume() { return totalVolume; }
        public void setTotalVolume(BigDecimal totalVolume) { this.totalVolume = totalVolume; }
        
        public BigDecimal getTotalDebits() { return totalDebits; }
        public void setTotalDebits(BigDecimal totalDebits) { this.totalDebits = totalDebits; }
        
        public BigDecimal getTotalCredits() { return totalCredits; }
        public void setTotalCredits(BigDecimal totalCredits) { this.totalCredits = totalCredits; }
        
        public Map<TransactionType, BigDecimal> getTotalsByType() { return totalsByType; }
        public void setTotalsByType(Map<TransactionType, BigDecimal> totalsByType) { this.totalsByType = totalsByType; }
    }
}
