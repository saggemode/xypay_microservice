package com.xypay.xypay.openbanking;

import com.xypay.xypay.service.AccountService;
import com.xypay.xypay.service.TransactionService;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/openbanking")
public class OpenBankingController {
    private final AccountService accountService;
    private final TransactionService transactionService;
    public OpenBankingController(AccountService accountService, TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @GetMapping("/accounts")
    public List<Map<String, Object>> getAccounts() {
        return accountService.getAllAccountsForOpenBanking();
    }

    @GetMapping("/accounts/{accountId}/balances")
    public Map<String, Object> getBalances(@PathVariable Long accountId) {
        return accountService.getBalanceForOpenBanking(accountId);
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public List<Map<String, Object>> getTransactions(@PathVariable Long accountId) {
        return transactionService.getTransactionsForOpenBanking(accountId);
    }

    @PostMapping("/payments")
    public Map<String, Object> initiatePayment(@RequestBody Map<String, Object> payment) {
        return accountService.initiateOpenBankingPayment(payment);
    }
}
