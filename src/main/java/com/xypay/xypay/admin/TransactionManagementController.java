package com.xypay.xypay.admin;

import com.xypay.xypay.domain.Transaction;
import com.xypay.xypay.domain.Wallet;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.domain.XySaveTransaction;
import com.xypay.xypay.domain.FixedSavingsTransaction;
import com.xypay.xypay.domain.SpendAndSaveTransaction;
import com.xypay.xypay.domain.SecurityTransaction;
import com.xypay.xypay.repository.TransactionRepository;
import com.xypay.xypay.repository.WalletRepository;
import com.xypay.xypay.repository.UserRepository;
import com.xypay.xypay.repository.XySaveTransactionRepository;
import com.xypay.xypay.repository.FixedSavingsTransactionRepository;
import com.xypay.xypay.repository.SpendAndSaveTransactionRepository;
import com.xypay.xypay.repository.SecurityTransactionRepository;
import com.xypay.xypay.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERUSER')")
public class TransactionManagementController {
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FixedSavingsTransactionRepository fixedSavingsTransactionRepository;
    
    @Autowired
    private SecurityTransactionRepository securityTransactionRepository;
    
    @Autowired
    private TransactionService transactionService;
    
    @Autowired
    private XySaveTransactionRepository xySaveTransactionRepository;
    
    @Autowired
    private SpendAndSaveTransactionRepository spendAndSaveTransactionRepository;
    
    
    // Main transaction dashboard
    @GetMapping("/transactions/dashboard")
    public String transactionDashboard(Model model) {
        System.out.println("=== Transaction Dashboard called ===");
        
        // Create sample transactions if none exist
        createSampleTransactionsIfNeeded();
        
        // Get transaction statistics
        long totalTransactions = transactionRepository.count();
        long todayTransactions = transactionRepository.countByCreatedAtBetween(
            LocalDate.now().atStartOfDay(), 
            LocalDate.now().atTime(23, 59, 59)
        );
        long pendingTransactions = transactionRepository.countByStatus("PENDING");
        long failedTransactions = transactionRepository.countByStatus("FAILED");
        
        System.out.println("Transaction stats: total=" + totalTransactions + ", today=" + todayTransactions + 
                          ", pending=" + pendingTransactions + ", failed=" + failedTransactions);
        
        // Get recent transactions
        Pageable recentPageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Transaction> recentTransactions = transactionRepository.findAll(recentPageable);
        
        System.out.println("Recent transactions count: " + recentTransactions.getContent().size());
        
        // Get transaction types distribution
        Map<String, Long> typeDistribution = new HashMap<>();
        typeDistribution.put("CREDIT", transactionRepository.countByType("CREDIT"));
        typeDistribution.put("DEBIT", transactionRepository.countByType("DEBIT"));
        
        // Get channel distribution
        Map<String, Long> channelDistribution = new HashMap<>();
        channelDistribution.put("TRANSFER", transactionRepository.countByChannel("TRANSFER"));
        channelDistribution.put("DEPOSIT", transactionRepository.countByChannel("DEPOSIT"));
        channelDistribution.put("BILL", transactionRepository.countByChannel("BILL"));
        
        model.addAttribute("totalTransactions", totalTransactions);
        model.addAttribute("todayTransactions", todayTransactions);
        model.addAttribute("pendingTransactions", pendingTransactions);
        model.addAttribute("failedTransactions", failedTransactions);
        model.addAttribute("recentTransactions", recentTransactions.getContent());
        model.addAttribute("typeDistribution", typeDistribution);
        model.addAttribute("channelDistribution", channelDistribution);
        
        return "admin/transactions";
    }

    // Alias: make /admin/transactions load the dashboard (matches sidebar link)
    @GetMapping("/transactions")
    public String transactionsAlias(Model model) {
        return transactionDashboard(model);
    }
    
    // Transaction list with filtering and pagination
    @GetMapping("/transactions/list")
    public String transactionList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {
        
        // Create sample transactions if none exist
        createSampleTransactionsIfNeeded();
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(Sort.Direction.DESC, sortBy) : 
            Sort.by(Sort.Direction.ASC, sortBy);
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Transaction> transactions;
        
        // Apply filters
        if (type != null || channel != null || status != null || search != null || startDate != null || endDate != null) {
            transactions = transactionRepository.findWithFilters(
                type, channel, status, search, 
                startDate != null ? startDate.atStartOfDay() : null,
                endDate != null ? endDate.atTime(23, 59, 59) : null,
                pageable
            );
        } else {
            transactions = transactionRepository.findAll(pageable);
        }
        
        // Summary stats (overall)
        long totalTransactions = transactionRepository.count();
        long successfulTransactions = transactionRepository.countByStatus("SUCCESS");
        long pendingTransactions = transactionRepository.countByStatus("PENDING");
        long failedTransactions = transactionRepository.countByStatus("FAILED");
        
        model.addAttribute("transactions", transactions);
        model.addAttribute("currentPage", page + 1); // 1-based for template
        model.addAttribute("totalPages", transactions.getTotalPages());
        model.addAttribute("totalElements", transactions.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        
        // Pass filter values to maintain state in the UI
        model.addAttribute("type", type);
        model.addAttribute("channel", channel);
        model.addAttribute("status", status);
        model.addAttribute("search", search);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        
        // Provide stats for cards
        model.addAttribute("totalTransactions", totalTransactions);
        model.addAttribute("successfulTransactions", successfulTransactions);
        model.addAttribute("pendingTransactions", pendingTransactions);
        model.addAttribute("failedTransactions", failedTransactions);
        
        return "admin/transaction-list";
    }
    
    // Transaction detail view
    @GetMapping("/transactions/{id}")
    public String transactionDetail(@PathVariable Long id, Model model) {
        Optional<Transaction> transactionOpt = transactionRepository.findById(id);
        if (transactionOpt.isEmpty()) {
            return "redirect:/admin/transactions";
        }
        
        Transaction transaction = transactionOpt.get();
        model.addAttribute("transaction", transaction);
        
        // Get related transactions (parent/child)
        if (transaction.getParent() != null) {
            model.addAttribute("parentTransaction", transaction.getParent());
        }
        
        List<Transaction> childTransactions = transactionRepository.findByParent(transaction);
        model.addAttribute("childTransactions", childTransactions);
        
        return "admin/transaction-detail";
    }
    
    // Test endpoint to check authentication
    @GetMapping("/api/test-auth")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testAuth() {
        Map<String, Object> response = new HashMap<>();
        response.put("authenticated", true);
        response.put("message", "Authentication successful");
        return ResponseEntity.ok(response);
    }
    
    // API endpoint to get transactions as JSON (for AJAX calls)
    @GetMapping("/api/transactions")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String hasReversal,
            @RequestParam(required = false) String large,
            @RequestParam(required = false) String suspicious,
            @RequestParam(required = false) String q) {
        
        try {
            // Debug logging
            System.out.println("=== API /admin/api/transactions called ===");
            System.out.println("Parameters: page=" + page + ", size=" + size + ", sortBy=" + sortBy + ", sortDir=" + sortDir);
            System.out.println("Filters: type=" + type + ", channel=" + channel + ", status=" + status + ", currency=" + currency);
            System.out.println("Additional filters: hasReversal=" + hasReversal + ", large=" + large + ", suspicious=" + suspicious + ", q=" + q);
            
            // Check total transaction count
            long totalCount = transactionRepository.count();
            System.out.println("Total transactions in database: " + totalCount);
                
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(Sort.Direction.DESC, sortBy) : 
                Sort.by(Sort.Direction.ASC, sortBy);
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Transaction> transactions;
            
            // Convert empty strings to null to avoid query issues
            String cleanType = (type != null && !type.isEmpty() && !type.equals("")) ? type : null;
            String cleanChannel = (channel != null && !channel.isEmpty() && !channel.equals("")) ? channel : null;
            String cleanStatus = (status != null && !status.isEmpty() && !status.equals("")) ? status : null;
            String cleanQ = (q != null && !q.isEmpty() && !q.equals("")) ? q : null;
            
            // Convert "null" strings to actual null values (from JavaScript JSON.stringify of undefined values)
            cleanType = "null".equals(cleanType) ? null : cleanType;
            cleanChannel = "null".equals(cleanChannel) ? null : cleanChannel;
            cleanStatus = "null".equals(cleanStatus) ? null : cleanStatus;
            cleanQ = "null".equals(cleanQ) ? null : cleanQ;
            
            // Apply filters if any are provided
            if (cleanType != null || cleanChannel != null || cleanStatus != null || cleanQ != null) {
                System.out.println("Using filtered query");
                transactions = transactionRepository.findWithFilters(
                    cleanType, cleanChannel, cleanStatus, cleanQ, null, null, pageable
                );
            } else {
                System.out.println("Using findAll query");
                transactions = transactionRepository.findAll(pageable);
            }
            
            System.out.println("Found " + transactions.getContent().size() + " transactions on page " + page);
            
            // Transform transactions to include computed fields
            List<Map<String, Object>> transactionData = transactions.getContent().stream()
                .map(this::transformTransactionForFrontend)
                .collect(java.util.stream.Collectors.toList());
                
            Map<String, Object> response = new HashMap<>();
            response.put("content", transactionData);
            response.put("totalElements", transactions.getTotalElements());
            response.put("totalPages", transactions.getTotalPages());
            response.put("currentPage", transactions.getNumber());
            response.put("size", transactions.getSize());
            
            System.out.println("Returning response with " + transactionData.size() + " transactions");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Log the exception for debugging
            System.err.println("Error in getTransactions: " + e.getMessage());
            e.printStackTrace();
            // Return an error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Transform Transaction entity to include computed fields expected by frontend
     */
    private Map<String, Object> transformTransactionForFrontend(Transaction transaction) {
        System.out.println("Transforming transaction: ID=" + transaction.getId() + ", Reference=" + transaction.getReference());
        
        Map<String, Object> data = new HashMap<>();
        data.put("id", transaction.getId());
        data.put("reference", transaction.getReference());
        data.put("amount", transaction.getAmount());
        data.put("type", transaction.getType());
        data.put("channel", transaction.getChannel());
        data.put("status", transaction.getStatus());
        data.put("currency", transaction.getCurrency());
        data.put("balanceAfter", transaction.getBalanceAfter());
        data.put("createdAt", transaction.getCreatedAt());
        data.put("description", transaction.getDescription());
        
        // Add computed fields
        if (transaction.getWallet() != null) {
            data.put("walletLabel", transaction.getWallet().getAccountNumber());
            System.out.println("  Wallet: " + transaction.getWallet().getAccountNumber());
        } else {
            data.put("walletLabel", "N/A");
            System.out.println("  Wallet: null");
        }
        
        if (transaction.getParent() != null) {
            data.put("parentReference", transaction.getParent().getReference());
            System.out.println("  Parent: " + transaction.getParent().getReference());
        } else {
            data.put("parentReference", null);
            System.out.println("  Parent: null");
        }
        
        return data;
    }
    
    /**
     * Create sample transactions if none exist in the database
     */
    private void createSampleTransactionsIfNeeded() {
        long transactionCount = transactionRepository.count();
        System.out.println("Current transaction count: " + transactionCount);
        
        if (transactionCount == 0) {
            // Create sample wallets if none exist
            List<Wallet> wallets = walletRepository.findAll();
            if (wallets.isEmpty()) {
                // Create sample users first
                List<User> users = userRepository.findAll();
                User user1, user2;
                if (users.size() >= 2) {
                    user1 = users.get(0);
                    user2 = users.get(1);
                } else {
                    user1 = new User();
                    user1.setUsername("sampleuser1");
                    user1.setEmail("user1@example.com");
                    user1.setFirstName("Sample");
                    user1.setLastName("User1");
                    user1.setPassword("password");
                    user1.setEnabled(true);
                    user1 = userRepository.save(user1);
                    
                    user2 = new User();
                    user2.setUsername("sampleuser2");
                    user2.setEmail("user2@example.com");
                    user2.setFirstName("Sample");
                    user2.setLastName("User2");
                    user2.setPassword("password");
                    user2.setEnabled(true);
                    user2 = userRepository.save(user2);
                }
                
                // Create sample wallets
                Wallet wallet1 = new Wallet();
                wallet1.setAccountNumber("7038655955");
                wallet1.setBalance(new BigDecimal("100000.0000"));
                wallet1.setCurrency("NGN");
                wallet1.setUser(user1);
                wallet1 = walletRepository.save(wallet1);
                
                Wallet wallet2 = new Wallet();
                wallet2.setAccountNumber("7038655954");
                wallet2.setBalance(new BigDecimal("50000.0000"));
                wallet2.setCurrency("NGN");
                wallet2.setUser(user2);
                wallet2 = walletRepository.save(wallet2);
                
                wallets = List.of(wallet1, wallet2);
            }
            
            // Create sample transactions
            Wallet senderWallet = wallets.get(0);
            Wallet receiverWallet = wallets.get(1);
            
            Transaction tx1 = new Transaction();
            tx1.setWallet(senderWallet);
            tx1.setReceiver(receiverWallet);
            tx1.setReference("TXN-001");
            tx1.setAmount(new BigDecimal("20000.0000"));
            tx1.setType("DEBIT");
            tx1.setChannel("TRANSFER");
            tx1.setDescription("Transfer to " + receiverWallet.getAccountNumber());
            tx1.setStatus("SUCCESS");
            tx1.setBalanceAfter(senderWallet.getBalance().subtract(new BigDecimal("20000.0000")));
            tx1.setDirection("DEBIT");
            tx1.setProcessedAt(LocalDateTime.now());
            transactionRepository.save(tx1);
            
            Transaction tx2 = new Transaction();
            tx2.setWallet(receiverWallet);
            tx2.setReceiver(senderWallet);
            tx2.setReference("TXN-002");
            tx2.setAmount(new BigDecimal("20000.0000"));
            tx2.setType("CREDIT");
            tx2.setChannel("TRANSFER");
            tx2.setDescription("Transfer from " + senderWallet.getAccountNumber());
            tx2.setStatus("SUCCESS");
            tx2.setBalanceAfter(receiverWallet.getBalance().add(new BigDecimal("20000.0000")));
            tx2.setDirection("CREDIT");
            tx2.setProcessedAt(LocalDateTime.now());
            transactionRepository.save(tx2);
        }
    }
}