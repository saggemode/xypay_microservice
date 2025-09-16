package com.xypay.xypay.scheduler;

import com.xypay.xypay.domain.User;
import com.xypay.xypay.domain.Wallet;
import com.xypay.xypay.domain.Transaction;
import com.xypay.xypay.repository.UserRepository;
import com.xypay.xypay.repository.WalletRepository;
import com.xypay.xypay.repository.TransactionRepository;
import com.xypay.xypay.service.EmailService;
import com.xypay.xypay.service.StatementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler for sending weekly account statements
 * Equivalent to Django's send_weekly_statements Celery task
 */
@Component
@Slf4j
public class WeeklyStatementScheduler {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private StatementService statementService;
    
    /**
     * Send weekly statements every Monday at 9 AM
     * Equivalent to Django's @shared_task(bind=True, ignore_result=True) send_weekly_statements
     */
    @Scheduled(cron = "0 0 9 * * MON")
    @Transactional
    public void sendWeeklyStatements() {
        log.info("Starting weekly statement processing...");
        
        try {
            LocalDate today = LocalDate.now();
            LocalDate weekAgo = today.minusDays(7);
            LocalDateTime weekAgoStart = weekAgo.atStartOfDay();
            LocalDateTime todayEnd = today.atTime(23, 59, 59);
            
            // Get all active users
            List<User> activeUsers = userRepository.findAll().stream()
                .filter(User::isEnabled)
                .toList();
            
            int processedCount = 0;
            int skippedCount = 0;
            
            for (User user : activeUsers) {
                try {
                    // Get user's wallet
                    List<Wallet> userWallets = walletRepository.findByUser(user);
                    if (userWallets.isEmpty()) {
                        skippedCount++;
                        continue;
                    }
                    
                    Wallet wallet = userWallets.get(0); // Get first wallet
                    
                    // Get transactions for the week
                    List<Transaction> transactions = transactionRepository.findByWalletAndChannelAndTimestampBetweenAndStatus(
                        wallet, null, weekAgoStart, todayEnd, "SUCCESS");
                    
                    if (transactions.isEmpty()) {
                        skippedCount++;
                        continue;
                    }
                    
                    // Sort transactions by timestamp
                    transactions.sort((t1, t2) -> t1.getTimestamp().compareTo(t2.getTimestamp()));
                    
                    // Calculate statement data
                    Transaction firstTx = transactions.get(0);
                    Transaction lastTx = transactions.get(transactions.size() - 1);
                    
                    BigDecimal openingBalance = BigDecimal.ZERO;
                    if (firstTx.getBalanceAfter() != null) {
                        openingBalance = firstTx.getBalanceAfter().subtract(firstTx.getAmount());
                    }
                    
                    BigDecimal closingBalance = lastTx.getBalanceAfter() != null ? 
                        lastTx.getBalanceAfter() : BigDecimal.ZERO;
                    
                    BigDecimal totalCredits = transactions.stream()
                        .filter(tx -> "CREDIT".equals(tx.getType()))
                        .map(Transaction::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    BigDecimal totalDebits = transactions.stream()
                        .filter(tx -> "DEBIT".equals(tx.getType()))
                        .map(Transaction::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    // Generate statement data
                    StatementData statementData = StatementData.builder()
                        .user(user)
                        .wallet(wallet)
                        .transactions(transactions)
                        .dateFrom(weekAgo)
                        .dateTo(today)
                        .openingBalance(openingBalance)
                        .closingBalance(closingBalance)
                        .totalCredits(totalCredits)
                        .totalDebits(totalDebits)
                        .generatedAt(LocalDateTime.now())
                        .build();
                    
                    // Generate PDF statement
                    byte[] pdfBytes = statementService.generateStatementPdf(statementData);
                    
                    // Send email with PDF attachment
                    emailService.sendWeeklyStatement(user, statementData, pdfBytes);
                    
                    processedCount++;
                    log.info("Sent weekly statement for user: {}", user.getUsername());
                    
                } catch (Exception e) {
                    log.error("Error processing weekly statement for user {}: {}", 
                        user.getUsername(), e.getMessage());
                    skippedCount++;
                }
            }
            
            log.info("Weekly statement processing completed. Processed: {}, Skipped: {}", 
                processedCount, skippedCount);
            
        } catch (Exception e) {
            log.error("Error during weekly statement processing: {}", e.getMessage());
        }
    }
    
    /**
     * Data class for statement information
     */
    public static class StatementData {
        private User user;
        private Wallet wallet;
        private List<Transaction> transactions;
        private LocalDate dateFrom;
        private LocalDate dateTo;
        private BigDecimal openingBalance;
        private BigDecimal closingBalance;
        private BigDecimal totalCredits;
        private BigDecimal totalDebits;
        private LocalDateTime generatedAt;
        
        // Builder pattern
        public static StatementDataBuilder builder() {
            return new StatementDataBuilder();
        }
        
        public static class StatementDataBuilder {
            private StatementData data = new StatementData();
            
            public StatementDataBuilder user(User user) { data.user = user; return this; }
            public StatementDataBuilder wallet(Wallet wallet) { data.wallet = wallet; return this; }
            public StatementDataBuilder transactions(List<Transaction> transactions) { data.transactions = transactions; return this; }
            public StatementDataBuilder dateFrom(LocalDate dateFrom) { data.dateFrom = dateFrom; return this; }
            public StatementDataBuilder dateTo(LocalDate dateTo) { data.dateTo = dateTo; return this; }
            public StatementDataBuilder openingBalance(BigDecimal openingBalance) { data.openingBalance = openingBalance; return this; }
            public StatementDataBuilder closingBalance(BigDecimal closingBalance) { data.closingBalance = closingBalance; return this; }
            public StatementDataBuilder totalCredits(BigDecimal totalCredits) { data.totalCredits = totalCredits; return this; }
            public StatementDataBuilder totalDebits(BigDecimal totalDebits) { data.totalDebits = totalDebits; return this; }
            public StatementDataBuilder generatedAt(LocalDateTime generatedAt) { data.generatedAt = generatedAt; return this; }
            
            public StatementData build() { return data; }
        }
        
        // Getters
        public User getUser() { return user; }
        public Wallet getWallet() { return wallet; }
        public List<Transaction> getTransactions() { return transactions; }
        public LocalDate getDateFrom() { return dateFrom; }
        public LocalDate getDateTo() { return dateTo; }
        public BigDecimal getOpeningBalance() { return openingBalance; }
        public BigDecimal getClosingBalance() { return closingBalance; }
        public BigDecimal getTotalCredits() { return totalCredits; }
        public BigDecimal getTotalDebits() { return totalDebits; }
        public LocalDateTime getGeneratedAt() { return generatedAt; }
    }
}
