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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
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
        // Get transaction statistics
        long totalTransactions = transactionRepository.count();
        long todayTransactions = transactionRepository.countByCreatedAtBetween(
            LocalDate.now().atStartOfDay(), 
            LocalDate.now().atTime(23, 59, 59)
        );
        long pendingTransactions = transactionRepository.countByStatus("PENDING");
        long failedTransactions = transactionRepository.countByStatus("FAILED");
        
        // Get recent transactions
        Pageable recentPageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Transaction> recentTransactions = transactionRepository.findAll(recentPageable);
        
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
        model.addAttribute("totalElements", transactions.getTotalElements()); // filtered total
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("filters", Map.of(
            "type", type != null ? type : "",
            "channel", channel != null ? channel : "",
            "status", status != null ? status : "",
            "search", search != null ? search : "",
            "startDate", startDate != null ? startDate.toString() : "",
            "endDate", endDate != null ? endDate.toString() : ""
        ));
        
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
    
    // Create new transaction form
    @GetMapping("/transactions/new")
    public String newTransactionForm(Model model) {
        model.addAttribute("transaction", new Transaction());
        List<Wallet> wallets = walletRepository.findAll();
        model.addAttribute("wallets", wallets);
        return "admin/transaction-new";
    }
    
    // Process new transaction
    @PostMapping("/transactions")
    public String createTransaction(@ModelAttribute Transaction transaction,
                                    @RequestParam(value = "action", required = false) String action) {
        try {
            // Generate reference if not provided
            if (transaction.getReference() == null || transaction.getReference().isEmpty()) {
                transaction.setReference("TXN-" + System.currentTimeMillis());
            }
            
            // Set default values
            if (transaction.getStatus() == null) {
                transaction.setStatus("PENDING");
            }
            if (transaction.getCurrency() == null) {
                transaction.setCurrency("NGN");
            }
            
            transactionRepository.save(transaction);
            if ("save_add".equalsIgnoreCase(action)) {
                return "redirect:/admin/transactions/new?success=1";
            }
            if ("save_continue".equalsIgnoreCase(action)) {
                return "redirect:/admin/transactions/" + transaction.getId() + "/edit";
            }
            return "redirect:/admin/transactions/" + transaction.getId();
        } catch (Exception e) {
            return "redirect:/admin/transactions/new?error=" + e.getMessage();
        }
    }
    
    // Edit transaction form
    @GetMapping("/transactions/{id}/edit")
    public String editTransactionForm(@PathVariable Long id, Model model) {
        Optional<Transaction> transactionOpt = transactionRepository.findById(id);
        if (transactionOpt.isEmpty()) {
            return "redirect:/admin/transactions";
        }
        
        model.addAttribute("transaction", transactionOpt.get());
        List<Wallet> wallets = walletRepository.findAll();
        model.addAttribute("wallets", wallets);
        return "admin/transaction-new";
    }
    
    // Update transaction
    @PostMapping("/transactions/{id}")
    public String updateTransaction(@PathVariable Long id, @ModelAttribute Transaction transaction) {
        Optional<Transaction> existingOpt = transactionRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return "redirect:/admin/transactions";
        }
        
        Transaction existing = existingOpt.get();
        existing.setDescription(transaction.getDescription());
        existing.setStatus(transaction.getStatus());
        existing.setMetadata(transaction.getMetadata());
        
        transactionRepository.save(existing);
        return "redirect:/admin/transactions/" + id;
    }
    
    // Delete transaction
    @PostMapping("/transactions/{id}/delete")
    public String deleteTransaction(@PathVariable Long id) {
        transactionRepository.deleteById(id);
        return "redirect:/admin/transactions";
    }
    
    // Transaction reversal
    @PostMapping("/transactions/{id}/reverse")
    public String reverseTransaction(@PathVariable Long id, @RequestParam String reason) {
        try {
            Optional<Transaction> transactionOpt = transactionRepository.findById(id);
            if (transactionOpt.isPresent()) {
                Transaction original = transactionOpt.get();
                // Add reason to transaction metadata before reversal
                if (original.getMetadata() == null) {
                    original.setMetadata("Reversal reason: " + reason);
                } else {
                    original.setMetadata(original.getMetadata() + "; Reversal reason: " + reason);
                }
                transactionRepository.save(original);
                
                Transaction reversal = transactionService.reverseTransaction(original.getId());
                return "redirect:/admin/transactions/" + reversal.getId();
            }
        } catch (Exception e) {
            // Handle error
        }
        return "redirect:/admin/transactions/" + id + "?error=reversal_failed";
    }
    
    // XySave transactions
    @GetMapping("/transactions/xysave")
    public String xySaveTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<XySaveTransaction> transactions = xySaveTransactionRepository.findAll(pageable);
        
        model.addAttribute("transactions", transactions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transactions.getTotalPages());
        model.addAttribute("transactionType", "XySave");
        
        return "admin/transaction-list";
    }
    
    // Fixed Savings transactions
    @GetMapping("/transactions/fixed-savings")
    public String fixedSavingsTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<FixedSavingsTransaction> transactions = fixedSavingsTransactionRepository.findAll(pageable);
        
        model.addAttribute("transactions", transactions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transactions.getTotalPages());
        model.addAttribute("transactionType", "FixedSavings");
        
        return "admin/transaction-list";
    }
    
    // Spend and Save transactions
    @GetMapping("/transactions/spend-and-save")
    public String spendAndSaveTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<SpendAndSaveTransaction> transactions = spendAndSaveTransactionRepository.findAll(pageable);
        
        model.addAttribute("transactions", transactions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transactions.getTotalPages());
        model.addAttribute("transactionType", "SpendAndSave");
        
        return "admin/transaction-list";
    }
    
    // Security transactions
    @GetMapping("/transactions/securities")
    public String securityTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "tradeDate"));
        Page<SecurityTransaction> transactions = securityTransactionRepository.findAll(pageable);
        
        model.addAttribute("transactions", transactions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transactions.getTotalPages());
        model.addAttribute("transactionType", "Security");
        
        return "admin/transaction-list";
    }
    
    // Redirect legacy create path to the new transaction form
    @GetMapping("/transactions/create")
    public String redirectCreateTransaction() {
        return "redirect:/admin/transactions/new";
    }
    
    // API endpoints for AJAX calls
    @GetMapping("/api/transactions/stats")
    @ResponseBody
    public Map<String, Object> getTransactionStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Basic stats
        stats.put("totalTransactions", transactionRepository.count());
        stats.put("todayTransactions", transactionRepository.countByCreatedAtBetween(
            LocalDate.now().atStartOfDay(), 
            LocalDate.now().atTime(23, 59, 59)
        ));
        stats.put("pendingTransactions", transactionRepository.countByStatus("PENDING"));
        stats.put("failedTransactions", transactionRepository.countByStatus("FAILED"));
        
        // Amount stats
        BigDecimal totalAmount = transactionRepository.sumAmountByStatus("SUCCESS");
        stats.put("totalAmount", totalAmount != null ? totalAmount : BigDecimal.ZERO);
        
        // Type distribution
        Map<String, Long> typeDistribution = new HashMap<>();
        typeDistribution.put("CREDIT", transactionRepository.countByType("CREDIT"));
        typeDistribution.put("DEBIT", transactionRepository.countByType("DEBIT"));
        stats.put("typeDistribution", typeDistribution);
        
        // Channel distribution
        Map<String, Long> channelDistribution = new HashMap<>();
        channelDistribution.put("TRANSFER", transactionRepository.countByChannel("TRANSFER"));
        channelDistribution.put("DEPOSIT", transactionRepository.countByChannel("DEPOSIT"));
        channelDistribution.put("BILL", transactionRepository.countByChannel("BILL"));
        stats.put("channelDistribution", channelDistribution);
        
        return stats;
    }
    
    @GetMapping("/api/transactions/recent")
    @ResponseBody
    public List<Transaction> getRecentTransactions(@RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return transactionRepository.findAll(pageable).getContent();
    }
    
    @PostMapping("/api/transactions/{id}/status")
    @ResponseBody
    public ResponseEntity<Map<String, String>> updateTransactionStatus(
            @PathVariable Long id, 
            @RequestParam String status) {
        
        Optional<Transaction> transactionOpt = transactionRepository.findById(id);
        if (transactionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Transaction transaction = transactionOpt.get();
        transaction.setStatus(status);
        transactionRepository.save(transaction);
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Transaction status updated successfully");
        
        return ResponseEntity.ok(response);
    }
}
