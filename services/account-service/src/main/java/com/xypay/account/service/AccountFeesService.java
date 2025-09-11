package com.xypay.account.service;

import com.xypay.account.domain.Account;
import com.xypay.account.domain.Transaction;
import com.xypay.account.enums.AccountType;
import com.xypay.account.enums.TransactionType;
import com.xypay.account.repository.AccountRepository;
import com.xypay.account.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
public class AccountFeesService {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private CoreBankingService coreBankingService;
    
    // Fee Configuration
    private static final BigDecimal MAINTENANCE_FEE_SAVINGS = BigDecimal.valueOf(50);
    private static final BigDecimal MAINTENANCE_FEE_CHECKING = BigDecimal.valueOf(100);
    private static final BigDecimal MAINTENANCE_FEE_BUSINESS = BigDecimal.valueOf(200);
    private static final BigDecimal MAINTENANCE_FEE_CORPORATE = BigDecimal.valueOf(500);
    
    private static final BigDecimal OVERDRAFT_FEE_RATE = BigDecimal.valueOf(0.05); // 5% per month
    private static final BigDecimal ATM_FEE = BigDecimal.valueOf(35);
    private static final BigDecimal TRANSFER_FEE = BigDecimal.valueOf(25);
    private static final BigDecimal STATEMENT_FEE = BigDecimal.valueOf(100);
    private static final BigDecimal CARD_FEE = BigDecimal.valueOf(50);
    
    // Monthly maintenance fee calculation
    @Scheduled(cron = "0 0 1 1 * ?") // First day of every month at midnight
    public void chargeMonthlyMaintenanceFees() {
        List<Account> activeAccounts = accountRepository.findAll().stream()
            .filter(account -> account.getStatus().isOperational())
            .toList();
        
        for (Account account : activeAccounts) {
            try {
                BigDecimal maintenanceFee = calculateMaintenanceFee(account);
                if (maintenanceFee.compareTo(BigDecimal.ZERO) > 0) {
                    chargeFee(account, TransactionType.MAINTENANCE_FEE, maintenanceFee, 
                             "Monthly maintenance fee");
                }
            } catch (Exception e) {
                // Log error but continue with other accounts
                System.err.println("Error charging maintenance fee for account " + 
                                 account.getAccountNumber() + ": " + e.getMessage());
            }
        }
    }
    
    // Daily overdraft fee calculation
    @Scheduled(cron = "0 0 0 * * ?") // Every day at midnight
    public void chargeDailyOverdraftFees() {
        List<Account> overdrawnAccounts = accountRepository.findAll().stream()
            .filter(account -> account.getOverdraftUsed().compareTo(BigDecimal.ZERO) > 0)
            .toList();
        
        for (Account account : overdrawnAccounts) {
            try {
                BigDecimal overdraftFee = calculateOverdraftFee(account);
                if (overdraftFee.compareTo(BigDecimal.ZERO) > 0) {
                    chargeFee(account, TransactionType.OVERDRAFT_FEE, overdraftFee, 
                             "Daily overdraft fee");
                }
            } catch (Exception e) {
                // Log error but continue with other accounts
                System.err.println("Error charging overdraft fee for account " + 
                                 account.getAccountNumber() + ": " + e.getMessage());
            }
        }
    }
    
    // Charge ATM fee
    public void chargeATMFee(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        chargeFee(account, TransactionType.ATM_FEE, ATM_FEE, "ATM transaction fee");
    }
    
    // Charge transfer fee
    public void chargeTransferFee(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        chargeFee(account, TransactionType.TRANSFER_FEE, TRANSFER_FEE, "Transfer transaction fee");
    }
    
    // Charge statement fee
    public void chargeStatementFee(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        chargeFee(account, TransactionType.STATEMENT_FEE, STATEMENT_FEE, "Account statement fee");
    }
    
    // Charge card fee
    public void chargeCardFee(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        chargeFee(account, TransactionType.CARD_FEE, CARD_FEE, "Card maintenance fee");
    }
    
    // Charge penalty for minimum balance violation
    public void chargeMinimumBalancePenalty(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        if (!account.meetsMinimumBalance()) {
            BigDecimal penalty = calculateMinimumBalancePenalty(account);
            chargeFee(account, TransactionType.PENALTY, penalty, 
                     "Minimum balance violation penalty");
        }
    }
    
    // Charge dormancy fee
    public void chargeDormancyFee(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        if (isAccountDormant(account)) {
            BigDecimal dormancyFee = calculateDormancyFee(account);
            chargeFee(account, TransactionType.PENALTY, dormancyFee, 
                     "Account dormancy fee");
        }
    }
    
    // Private helper methods
    private BigDecimal calculateMaintenanceFee(Account account) {
        return switch (account.getAccountType()) {
            case SAVINGS -> MAINTENANCE_FEE_SAVINGS;
            case CHECKING, CURRENT -> MAINTENANCE_FEE_CHECKING;
            case BUSINESS -> MAINTENANCE_FEE_BUSINESS;
            case CORPORATE -> MAINTENANCE_FEE_CORPORATE;
            default -> BigDecimal.ZERO;
        };
    }
    
    private BigDecimal calculateOverdraftFee(Account account) {
        if (account.getOverdraftUsed().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        // Calculate daily overdraft fee (monthly rate / 30)
        BigDecimal dailyRate = OVERDRAFT_FEE_RATE.divide(BigDecimal.valueOf(30));
        return account.getOverdraftUsed().multiply(dailyRate);
    }
    
    private BigDecimal calculateMinimumBalancePenalty(Account account) {
        BigDecimal shortfall = account.getMinimumBalance().subtract(account.getLedgerBalance());
        return shortfall.multiply(BigDecimal.valueOf(0.01)); // 1% of shortfall
    }
    
    private BigDecimal calculateDormancyFee(Account account) {
        return BigDecimal.valueOf(100); // Fixed dormancy fee
    }
    
    private boolean isAccountDormant(Account account) {
        if (account.getLastTransactionDate() == null) {
            return ChronoUnit.DAYS.between(account.getCreatedAt(), LocalDateTime.now()) > 365;
        }
        return ChronoUnit.DAYS.between(account.getLastTransactionDate(), LocalDateTime.now()) > 365;
    }
    
    private void chargeFee(Account account, TransactionType feeType, BigDecimal feeAmount, String description) {
        try {
            // Check if account can bear the fee
            if (account.canDebit(feeAmount)) {
                account.debit(feeAmount);
                accountRepository.save(account);
                
                // Create fee transaction record
                Transaction feeTransaction = new Transaction(
                    account.getId(), 
                    account.getAccountNumber(), 
                    feeType, 
                    feeAmount,
                    account.getAvailableBalance().add(feeAmount), // balance before
                    account.getAvailableBalance() // balance after
                );
                
                feeTransaction.setDescription(description);
                feeTransaction.setChannel("SYSTEM");
                feeTransaction.setReference("FEE_" + System.currentTimeMillis());
                
                transactionRepository.save(feeTransaction);
                
            } else {
                // Account cannot bear the fee - mark as overdraft if possible
                if (account.isOverdraftAvailable() && 
                    feeAmount.compareTo(account.getOverdraftLimit().subtract(account.getOverdraftUsed())) <= 0) {
                    
                    account.debit(feeAmount);
                    accountRepository.save(account);
                    
                    // Create fee transaction record
                    Transaction feeTransaction = new Transaction(
                        account.getId(), 
                        account.getAccountNumber(), 
                        feeType, 
                        feeAmount,
                        account.getAvailableBalance().add(feeAmount), // balance before
                        account.getAvailableBalance() // balance after
                    );
                    
                    feeTransaction.setDescription(description + " (Overdraft)");
                    feeTransaction.setChannel("SYSTEM");
                    feeTransaction.setReference("FEE_" + System.currentTimeMillis());
                    
                    transactionRepository.save(feeTransaction);
                } else {
                    // Cannot charge fee - account would be over limit
                    System.err.println("Cannot charge fee " + feeAmount + " to account " + 
                                     account.getAccountNumber() + " - insufficient funds and overdraft");
                }
            }
        } catch (Exception e) {
            System.err.println("Error charging fee to account " + account.getAccountNumber() + ": " + e.getMessage());
            throw e;
        }
    }
    
    // Fee waiver methods
    public void waiveMaintenanceFee(String accountNumber, String reason) {
        // Implementation for fee waiver
        System.out.println("Maintenance fee waived for account " + accountNumber + " - Reason: " + reason);
    }
    
    public void waiveOverdraftFee(String accountNumber, String reason) {
        // Implementation for overdraft fee waiver
        System.out.println("Overdraft fee waived for account " + accountNumber + " - Reason: " + reason);
    }
    
    // Fee calculation for specific account
    public BigDecimal calculateTotalFeesForAccount(String accountNumber, LocalDateTime startDate, LocalDateTime endDate) {
        List<Transaction> feeTransactions = transactionRepository.findFeeTransactionsByAccountIdAndDateRange(
            accountRepository.findByAccountNumber(accountNumber).orElseThrow().getId(), 
            startDate, endDate);
        
        return feeTransactions.stream()
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
