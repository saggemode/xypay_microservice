package com.xypay.xypay.controller;

import com.xypay.xypay.domain.SpendAndSaveAccount;
import com.xypay.xypay.domain.SpendAndSaveTransaction;
import com.xypay.xypay.domain.Transaction;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.dto.ApiResponse;
import com.xypay.xypay.service.SpendAndSaveInterestService;
import com.xypay.xypay.service.SpendAndSaveNotificationService;
import com.xypay.xypay.repository.SpendAndSaveAccountRepository;
import com.xypay.xypay.repository.SpendAndSaveTransactionRepository;
import com.xypay.xypay.repository.TransactionRepository;
import com.xypay.xypay.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST controller for Spend and Save statistics (admin/staff only)
 */
@RestController
@RequestMapping("/api/spend-and-save/statistics")
@Slf4j
public class SpendAndSaveStatisticsController {
    
    @Autowired
    private SpendAndSaveAccountRepository spendAndSaveAccountRepository;
    
    @Autowired
    private SpendAndSaveTransactionRepository spendAndSaveTransactionRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SpendAndSaveInterestService interestService;
    
    @Autowired
    private SpendAndSaveNotificationService notificationService;
    
    /**
     * Get overview statistics for Spend and Save
     */
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOverview(Authentication authentication) {
        try {
            User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Check if user is staff
            if (!user.getRoles().contains("ROLE_ADMIN") && !user.getRoles().contains("ROLE_STAFF")) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("Access denied"));
            }
            
            long totalUsers = spendAndSaveAccountRepository.count();
            long activeAccounts = spendAndSaveAccountRepository.countByIsActiveTrue();
            
            // Calculate totals
            List<SpendAndSaveAccount> allAccounts = spendAndSaveAccountRepository.findAll();
            BigDecimal totalSavedAmount = allAccounts.stream()
                .map(SpendAndSaveAccount::getTotalSavedFromSpending)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal totalInterestPaid = allAccounts.stream()
                .map(SpendAndSaveAccount::getTotalInterestEarned)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            int totalTransactionsProcessed = allAccounts.stream()
                .mapToInt(SpendAndSaveAccount::getTotalTransactionsProcessed)
                .sum();
            
            // Calculate average savings percentage
            List<SpendAndSaveAccount> activeAccountsWithPercentage = allAccounts.stream()
                .filter(account -> account.getIsActive() && account.getSavingsPercentage() != null)
                .collect(Collectors.toList());
            
            double averageSavingsPercentage = activeAccountsWithPercentage.isEmpty() ? 0.0 :
                activeAccountsWithPercentage.stream()
                    .mapToDouble(account -> account.getSavingsPercentage().doubleValue())
                    .average()
                    .orElse(0.0);
            
            Map<String, Object> statistics = Map.of(
                "total_users", totalUsers,
                "active_accounts", activeAccounts,
                "total_saved_amount", totalSavedAmount.toString(),
                "total_interest_paid", totalInterestPaid.toString(),
                "average_savings_percentage", Math.round(averageSavingsPercentage * 100.0) / 100.0,
                "total_transactions_processed", totalTransactionsProcessed,
                "daily_interest_payouts", activeAccounts, // Each active account gets daily interest
                "monthly_growth_rate", 0.0 // Would need historical data to calculate
            );
            
            return ResponseEntity.ok(ApiResponse.success(statistics));
            
        } catch (Exception e) {
            log.error("Error getting overview statistics: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error getting overview statistics: " + e.getMessage()));
        }
    }
    
    /**
     * Get transaction analytics for Spend and Save
     */
    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAnalytics(
            @RequestParam(defaultValue = "30") int days,
            Authentication authentication) {
        try {
            User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Optional<SpendAndSaveAccount> accountOpt = spendAndSaveAccountRepository.findByUser(user);
            if (accountOpt.isEmpty()) {
                return ResponseEntity.status(404)
                    .body(ApiResponse.error("Spend and Save account not found"));
            }
            
            SpendAndSaveAccount account = accountOpt.get();
            
            // Calculate date range
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minus(days, ChronoUnit.DAYS);
            
            // Get transactions in date range
            List<SpendAndSaveTransaction> transactions = spendAndSaveTransactionRepository
                .findBySpendAndSaveAccountAndCreatedAtBetween(account, startDate, endDate);
            
            // Calculate analytics
            BigDecimal totalSaved = transactions.stream()
                .filter(tx -> "AUTO_SAVE".equals(tx.getTransactionType().toString()))
                .map(SpendAndSaveTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal totalWithdrawn = transactions.stream()
                .filter(tx -> "WITHDRAWAL".equals(tx.getTransactionType().toString()))
                .map(SpendAndSaveTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal totalInterest = transactions.stream()
                .filter(tx -> "INTEREST_CREDIT".equals(tx.getTransactionType().toString()))
                .map(SpendAndSaveTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Daily breakdown
            Map<LocalDateTime, Map<String, Object>> dailyData = new HashMap<>();
            for (SpendAndSaveTransaction tx : transactions) {
                LocalDateTime date = tx.getCreatedAt().toLocalDate().atStartOfDay();
                dailyData.computeIfAbsent(date, k -> {
                    Map<String, Object> dayData = new HashMap<>();
                    dayData.put("saved", BigDecimal.ZERO);
                    dayData.put("withdrawn", BigDecimal.ZERO);
                    dayData.put("interest", BigDecimal.ZERO);
                    dayData.put("transactions", 0);
                    return dayData;
                });
                
                Map<String, Object> dayData = dailyData.get(date);
                if ("AUTO_SAVE".equals(tx.getTransactionType().toString())) {
                    dayData.put("saved", ((BigDecimal) dayData.get("saved")).add(tx.getAmount()));
                } else if ("WITHDRAWAL".equals(tx.getTransactionType().toString())) {
                    dayData.put("withdrawn", ((BigDecimal) dayData.get("withdrawn")).add(tx.getAmount()));
                } else if ("INTEREST_CREDIT".equals(tx.getTransactionType().toString())) {
                    dayData.put("interest", ((BigDecimal) dayData.get("interest")).add(tx.getAmount()));
                }
                dayData.put("transactions", (Integer) dayData.get("transactions") + 1);
            }
            
            Map<String, Object> analytics = Map.of(
                "period", Map.of(
                    "start_date", startDate.toLocalDate(),
                    "end_date", endDate.toLocalDate(),
                    "days", days
                ),
                "summary", Map.of(
                    "total_saved", totalSaved,
                    "total_withdrawn", totalWithdrawn,
                    "total_interest", totalInterest,
                    "net_savings", totalSaved.add(totalInterest).subtract(totalWithdrawn),
                    "total_transactions", transactions.size()
                ),
                "daily_breakdown", dailyData.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> Map.of(
                        "date", entry.getKey().toLocalDate().toString(),
                        "saved", entry.getValue().get("saved"),
                        "withdrawn", entry.getValue().get("withdrawn"),
                        "interest", entry.getValue().get("interest"),
                        "transactions", entry.getValue().get("transactions")
                    ))
                    .collect(Collectors.toList())
            );
            
            return ResponseEntity.ok(ApiResponse.success(analytics));
            
        } catch (Exception e) {
            log.error("Error getting analytics: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error getting analytics: " + e.getMessage()));
        }
    }
    
    /**
     * Get user's Spend and Save related notifications
     */
    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getNotifications(Authentication authentication) {
        try {
            // This would require a notification service implementation
            // For now, return empty notifications
            Map<String, Object> result = Map.of(
                "notifications", Collections.emptyList(),
                "unread_count", 0
            );
            
            return ResponseEntity.ok(ApiResponse.success(result));
            
        } catch (Exception e) {
            log.error("Error getting notifications: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error getting notifications: " + e.getMessage()));
        }
    }
    
    /**
     * Get weekly savings summary
     */
    @GetMapping("/weekly-summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getWeeklySummary(Authentication authentication) {
        try {
            User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Optional<SpendAndSaveAccount> accountOpt = spendAndSaveAccountRepository.findByUser(user);
            if (accountOpt.isEmpty()) {
                return ResponseEntity.status(404)
                    .body(ApiResponse.error("Spend and Save account not found"));
            }
            
            SpendAndSaveAccount account = accountOpt.get();
            
            // Calculate weekly stats
            LocalDateTime weekAgo = LocalDateTime.now().minus(7, ChronoUnit.DAYS);
            List<SpendAndSaveTransaction> weeklyTransactions = spendAndSaveTransactionRepository
                .findBySpendAndSaveAccountAndTransactionTypeAndCreatedAtAfter(
                    account, 
                    SpendAndSaveTransaction.TransactionType.AUTO_SAVE, 
                    weekAgo
                );
            
            BigDecimal totalSaved = weeklyTransactions.stream()
                .map(SpendAndSaveTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            int transactionsCount = weeklyTransactions.size();
            
            // Get spending transactions that triggered saves
            List<Transaction> originalTransactions = transactionRepository
                .findByWalletUserAndTypeAndCreatedAtAfter(user, "debit", weekAgo);
            BigDecimal totalSpent = originalTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            Map<String, Object> weeklyStats = Map.of(
                "total_spent", totalSpent,
                "total_saved", totalSaved,
                "transactions_count", transactionsCount,
                "savings_rate", totalSpent.compareTo(BigDecimal.ZERO) > 0 ? 
                    totalSaved.divide(totalSpent, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal("100")) : BigDecimal.ZERO,
                "week_start", weekAgo.toLocalDate(),
                "week_end", LocalDateTime.now().toLocalDate()
            );
            
            return ResponseEntity.ok(ApiResponse.success(weeklyStats));
            
        } catch (Exception e) {
            log.error("Error getting weekly summary: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error getting weekly summary: " + e.getMessage()));
        }
    }
    
    /**
     * Get user's savings goals and progress
     */
    @GetMapping("/savings-goals")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSavingsGoals(Authentication authentication) {
        try {
            User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Optional<SpendAndSaveAccount> accountOpt = spendAndSaveAccountRepository.findByUser(user);
            if (accountOpt.isEmpty()) {
                return ResponseEntity.status(404)
                    .body(ApiResponse.error("Spend and Save account not found"));
            }
            
            SpendAndSaveAccount account = accountOpt.get();
            
            // Define milestone goals
            List<Map<String, Object>> milestones = List.of(
                Map.of("amount", 100, "name", "First ₦100", "icon", "💰"),
                Map.of("amount", 500, "name", "₦500 Milestone", "icon", "🎯"),
                Map.of("amount", 1000, "name", "₦1,000 Goal", "icon", "🏆"),
                Map.of("amount", 5000, "name", "₦5,000 Target", "icon", "💎"),
                Map.of("amount", 10000, "name", "₦10,000 Achievement", "icon", "👑"),
                Map.of("amount", 50000, "name", "₦50,000 Dream", "icon", "🚀"),
                Map.of("amount", 100000, "name", "₦100,000 Master", "icon", "💫")
            );
            
            BigDecimal currentSaved = account.getTotalSavedFromSpending();
            List<Map<String, Object>> goals = milestones.stream()
                .map(milestone -> {
                    BigDecimal targetAmount = new BigDecimal(milestone.get("amount").toString());
                    double progress = Math.min(100, currentSaved.divide(targetAmount, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal("100")).doubleValue());
                    boolean achieved = currentSaved.compareTo(targetAmount) >= 0;
                    
                    return Map.of(
                        "name", milestone.get("name"),
                        "icon", milestone.get("icon"),
                        "target_amount", targetAmount,
                        "current_amount", currentSaved,
                        "progress", Math.round(progress * 10.0) / 10.0,
                        "achieved", achieved,
                        "remaining", currentSaved.compareTo(targetAmount) >= 0 ? BigDecimal.ZERO : targetAmount.subtract(currentSaved)
                    );
                })
                .collect(Collectors.toList());
            
            Optional<Map<String, Object>> nextGoal = goals.stream()
                .filter(goal -> !(Boolean) goal.get("achieved"))
                .findFirst();
            
            Map<String, Object> result = Map.of(
                "goals", goals,
                "total_saved", currentSaved,
                "next_goal", nextGoal.orElse(null)
            );
            
            return ResponseEntity.ok(ApiResponse.success(result));
            
        } catch (Exception e) {
            log.error("Error getting savings goals: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error getting savings goals: " + e.getMessage()));
        }
    }
    
    /**
     * Send weekly savings summary notification
     */
    @PostMapping("/send-weekly-summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendWeeklySummary(Authentication authentication) {
        try {
            User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Optional<SpendAndSaveAccount> accountOpt = spendAndSaveAccountRepository.findByUser(user);
            if (accountOpt.isEmpty()) {
                return ResponseEntity.status(404)
                    .body(ApiResponse.error("Spend and Save account not found"));
            }
            
            SpendAndSaveAccount account = accountOpt.get();
            
            // Calculate weekly stats
            LocalDateTime weekAgo = LocalDateTime.now().minus(7, ChronoUnit.DAYS);
            List<SpendAndSaveTransaction> weeklyTransactions = spendAndSaveTransactionRepository
                .findBySpendAndSaveAccountAndTransactionTypeAndCreatedAtAfter(
                    account, 
                    SpendAndSaveTransaction.TransactionType.AUTO_SAVE, 
                    weekAgo
                );
            
            BigDecimal totalSaved = weeklyTransactions.stream()
                .map(SpendAndSaveTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            int transactionsCount = weeklyTransactions.size();
            
            List<Transaction> originalTransactions = transactionRepository
                .findByWalletUserAndTypeAndCreatedAtAfter(user, "debit", weekAgo);
            BigDecimal totalSpent = originalTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            Map<String, Object> weeklyStats = Map.of(
                "total_spent", totalSpent,
                "total_saved", totalSaved,
                "transactions_count", transactionsCount
            );
            
            // Send weekly summary notification
            notificationService.sendWeeklySavingsSummary(user, account, weeklyStats);
            
            return ResponseEntity.ok(ApiResponse.success(weeklyStats, "Weekly summary notification sent successfully"));
            
        } catch (Exception e) {
            log.error("Error sending weekly summary: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error sending weekly summary: " + e.getMessage()));
        }
    }
    
    /**
     * Process daily interest payout for all active accounts (admin only)
     */
    @PostMapping("/process-daily-interest")
    public ResponseEntity<ApiResponse<Map<String, Object>>> processDailyInterest(Authentication authentication) {
        try {
            User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Check if user is staff
            if (!user.getRoles().contains("ROLE_ADMIN") && !user.getRoles().contains("ROLE_STAFF")) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("Access denied"));
            }
            
            int processedCount = interestService.processDailyInterestPayout();
            
            return ResponseEntity.ok(ApiResponse.success(
                Map.of("processed_count", processedCount),
                String.format("Processed daily interest for %d accounts", processedCount)
            ));
            
        } catch (Exception e) {
            log.error("Error processing daily interest: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error processing daily interest: " + e.getMessage()));
        }
    }
}
