package com.xypay.xypay.admin;

import com.xypay.xypay.domain.User;
import com.xypay.xypay.domain.Wallet;
import com.xypay.xypay.domain.Transaction;
import com.xypay.xypay.repository.UserRepository;
import com.xypay.xypay.repository.WalletRepository;
import com.xypay.xypay.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
public class AdminDashboardController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        // Users
        long totalUsers = userRepository.count();
        model.addAttribute("totalUsers", totalUsers);

        // Accounts/Wallets
        java.util.List<Wallet> wallets = walletRepository.findAllWithUser();
        long totalAccounts = wallets.size();
        java.math.BigDecimal totalBalance = wallets.stream()
            .map(Wallet::getBalance)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        long activeAccounts = wallets.stream()
            .filter(w -> w.getBalance() != null && w.getBalance().compareTo(java.math.BigDecimal.ZERO) > 0)
            .count();
        model.addAttribute("totalAccounts", totalAccounts);
        model.addAttribute("totalBalance", totalBalance);
        model.addAttribute("activeAccounts", activeAccounts);

        // Transactions
        long totalTransactions = transactionRepository.count();
        java.time.LocalDate today = java.time.LocalDate.now();
        long todayTransactions = transactionRepository.countByCreatedAtBetween(
            today.atStartOfDay(), today.atTime(23, 59, 59));
        long pendingTransactions = transactionRepository.countByStatus("PENDING");
        long failedTransactions = transactionRepository.countByStatus("FAILED");
        model.addAttribute("todayTransactions", todayTransactions);
        model.addAttribute("totalTransactions", totalTransactions);
        model.addAttribute("pendingTransactions", pendingTransactions);
        model.addAttribute("failedTransactions", failedTransactions);

        // Recent transactions list for dashboard widgets
        org.springframework.data.domain.Pageable recentPageable = org.springframework.data.domain.PageRequest.of(
            0, 10, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        java.util.List<Transaction> recentTransactions = transactionRepository.findAll(recentPageable).getContent();
        model.addAttribute("recentTransactions", recentTransactions);

        // Placeholders for other dashboard cards without backing entities
        model.addAttribute("xySaveAccounts", 0);
        model.addAttribute("securityAlerts", 0);
        model.addAttribute("activeStaff", 0);
        model.addAttribute("reversalsToday", 0);
        model.addAttribute("totalMerchants", 0);
        model.addAttribute("totalBranches", 0);
        model.addAttribute("activeAlerts", 0);

        return "admin/dashboard";
    }

    @GetMapping("/admin/user-registration")
    public String userRegistrationMonitor() {
        return "admin-user-registration";
    }

    // Core Banking Templates
    @GetMapping("/admin/accounts")
    public String accounts(Model model) {
        // Fetch real account data from database with user data
        List<Wallet> wallets = walletRepository.findAllWithUser();
        List<User> users = userRepository.findAll();
        
        // Calculate statistics
        long totalAccounts = wallets.size();
        BigDecimal totalBalance = wallets.stream()
            .map(Wallet::getBalance)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        long activeAccounts = wallets.stream()
            .filter(w -> w.getBalance().compareTo(BigDecimal.ZERO) > 0)
            .count();
        
        // Add data to model
        model.addAttribute("wallets", wallets);
        model.addAttribute("users", users);
        model.addAttribute("totalAccounts", totalAccounts);
        model.addAttribute("totalBalance", totalBalance);
        model.addAttribute("activeAccounts", activeAccounts);
        
        return "admin/accounts";
    }
    
    @GetMapping("/admin/accounts/{id}")
    @ResponseBody
    public ResponseEntity<Wallet> getAccount(@PathVariable Long id) {
        Optional<Wallet> wallet = walletRepository.findByIdWithUser(id);
        return wallet.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/admin/accounts/{id}/update")
    @ResponseBody
    public ResponseEntity<String> updateAccount(@PathVariable Long id, 
                                              @RequestParam String phoneAlias,
                                              @RequestParam BigDecimal balance) {
        Optional<Wallet> walletOpt = walletRepository.findById(id);
        if (walletOpt.isPresent()) {
            Wallet wallet = walletOpt.get();
            wallet.setPhoneAlias(phoneAlias);
            wallet.setBalance(balance);
            walletRepository.save(wallet);
            return ResponseEntity.ok("Account updated successfully");
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/admin/customers")
    public String customers() {
        return "admin/customers";
    }

    // Legacy route removed to avoid conflict with TransactionManagementController.
    // Keep explicit dashboard at /admin/dashboard and use /admin/transactions from TransactionManagementController.

    @GetMapping("/admin/branches")
    public String branches() {
        return "admin/branches";
    }

    @GetMapping("/admin/balances")
    public String balances() {
        return "admin/balances";
    }

    // Loan Management Templates
    @GetMapping("/admin/loans")
    public String loans() {
        return "admin/loans";
    }

    @GetMapping("/admin/loan-applications")
    public String loanApplications() {
        return "admin/loan-applications";
    }

    @GetMapping("/admin/repayments")
    public String repayments() {
        return "admin/repayments";
    }

    @GetMapping("/admin/amortization")
    public String amortization() {
        return "admin/amortization";
    }

    @GetMapping("/admin/interest-calculator")
    public String interestCalculator() {
        return "admin/interest-calculator";
    }

    // Trade Finance Templates
    @GetMapping("/admin/trade-documents")
    public String tradeDocuments() {
        return "admin/trade-documents";
    }

    @GetMapping("/admin/trade-amendments")
    public String tradeAmendments() {
        return "admin/trade-amendments";
    }

    @GetMapping("/admin/swift-messages")
    public String swiftMessages() {
        return "admin/swift-messages";
    }

    // Treasury Operations Templates
    @GetMapping("/admin/treasury-positions")
    public String treasuryPositions() {
        return "admin/treasury-positions";
    }

    @GetMapping("/admin/treasury-settlements")
    public String treasurySettlements() {
        return "admin/treasury-settlements";
    }

    @GetMapping("/admin/fx-rates")
    public String fxRates() {
        return "admin/fx-rates";
    }

    @GetMapping("/admin/money-market")
    public String moneyMarket() {
        return "admin/money-market";
    }

    // Investment Banking Templates
    @GetMapping("/admin/securities")
    public String securities() {
        return "admin/securities";
    }

    @GetMapping("/admin/portfolios")
    public String portfolios() {
        return "admin/portfolios";
    }

    @GetMapping("/admin/holdings")
    public String holdings() {
        return "admin/holdings";
    }

    @GetMapping("/admin/trades")
    public String trades() {
        return "admin/trades";
    }

    @GetMapping("/admin/market-data")
    public String marketData() {
        return "admin/market-data";
    }

    // Islamic Banking Templates
    @GetMapping("/admin/islamic-products")
    public String islamicProducts() {
        return "admin/islamic-products";
    }

    @GetMapping("/admin/islamic-contracts")
    public String islamicContracts() {
        return "admin/islamic-contracts";
    }

    @GetMapping("/admin/islamic-payments")
    public String islamicPayments() {
        return "admin/islamic-payments";
    }

    @GetMapping("/admin/profit-distribution")
    public String profitDistribution() {
        return "admin/profit-distribution";
    }

    @GetMapping("/admin/sharia-compliance")
    public String shariaCompliance() {
        return "admin/sharia-compliance";
    }

    // Compliance & Risk Templates
    @GetMapping("/admin/kyc")
    public String kyc() {
        return "admin/kyc";
    }
    
    @GetMapping("/admin/kyc-management")
    public String kycManagement() {
        return "admin/kyc-management";
    }

    @GetMapping("/admin/aml-monitoring")
    public String amlMonitoring() {
        return "admin/aml-monitoring";
    }

    @GetMapping("/admin/fraud-detection")
    public String fraudDetection() {
        return "admin/fraud-detection";
    }

    @GetMapping("/admin/risk-assessment")
    public String riskAssessment() {
        return "admin/risk-assessment";
    }

    // Payment Systems Templates
    @GetMapping("/admin/payment-gateways")
    public String paymentGateways() {
        return "admin/payment-gateways";
    }

    @GetMapping("/admin/payment-processing")
    public String paymentProcessing() {
        return "admin/payment-processing";
    }

    @GetMapping("/admin/settlement-management")
    public String settlementManagement() {
        return "admin/settlement-management";
    }

    @GetMapping("/admin/mobile-payments")
    public String mobilePayments() {
        return "admin/mobile-payments";
    }

    // System Administration Templates
    @GetMapping("/admin/system-monitoring")
    public String systemMonitoring() {
        return "admin/system-monitoring";
    }

    @GetMapping("/admin/user-management")
    public String userManagement() {
        return "admin/user-management";
    }

    @GetMapping("/admin/backup-recovery")
    public String backupRecovery() {
        return "admin/backup-recovery";
    }

    // Additional Admin Templates
    @GetMapping("/admin/alerts")
    public String alerts() {
        return "admin/alerts";
    }

    @GetMapping("/admin/branch-entity")
    public String branchEntity() {
        return "admin/branch-entity";
    }

    @GetMapping("/admin/customer-account")
    public String customerAccount() {
        return "admin/customer-account";
    }

    @GetMapping("/admin/database")
    public String database() {
        return "admin/database";
    }

    @GetMapping("/admin/integrations")
    public String integrations() {
        return "admin/integrations";
    }

    @GetMapping("/admin/interest-charges")
    public String interestCharges() {
        return "admin/interest-charges";
    }

    @GetMapping("/admin/loan-products")
    public String loanProducts() {
        return "admin/loan-products";
    }


    @GetMapping("/admin/products")
    public String products() {
        return "admin/products";
    }

    @GetMapping("/admin/reporting")
    public String reporting() {
        return "admin/reporting";
    }

    @GetMapping("/admin/risk-compliance")
    public String riskCompliance() {
        return "admin/risk-compliance";
    }

    @GetMapping("/admin/scripting")
    public String scripting() {
        return "admin/scripting";
    }


    @GetMapping("/admin/workflow")
    public String workflow() {
        return "admin/workflow";
    }

    @GetMapping("/admin/notifications")
    public String notifications() {
        return "admin/notifications";
    }
}