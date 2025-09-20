package com.xypay.account.service;

import com.xypay.account.domain.BalanceHistory;
import com.xypay.account.repository.BalanceHistoryRepository;
import com.xypay.account.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.UUID;

@Service
@Transactional
public class BalanceService {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private BalanceHistoryRepository balanceHistoryRepository;

    @Cacheable(value = "balances", key = "#accountId")
    public BigDecimal getBalance(UUID accountId) {
        var account = accountRepository.findById(accountId).orElse(null);
        return account != null ? account.getLedgerBalance() : BigDecimal.ZERO;
    }

    @CacheEvict(value = "balances", key = "#accountId")
    public void updateBalance(UUID accountId, BigDecimal newBalance) {
        var account = accountRepository.findById(accountId).orElse(null);
        if (account != null) {
            BigDecimal oldBalance = account.getLedgerBalance();
            account.setLedgerBalance(newBalance);
            account.setUpdatedAt(LocalDateTime.now());
            accountRepository.save(account);
            
            // Record balance history
            recordBalanceHistory(accountId, oldBalance, newBalance, "MANUAL_UPDATE");
        }
    }

    public List<Map<String, Object>> getBalanceHistory(UUID accountId) {
        List<BalanceHistory> history = balanceHistoryRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
        return history.stream().map(h -> {
            Map<String, Object> historyItem = new HashMap<>();
            historyItem.put("accountId", h.getAccountId());
            historyItem.put("oldBalance", h.getOldBalance());
            historyItem.put("newBalance", h.getNewBalance());
            historyItem.put("changeAmount", h.getChangeAmount());
            historyItem.put("reason", h.getReason());
            historyItem.put("createdAt", h.getCreatedAt());
            return historyItem;
        }).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public void recordBalanceHistory(UUID accountId, BigDecimal oldBalance, BigDecimal newBalance, String reason) {
        BalanceHistory history = new BalanceHistory();
        history.setAccountId(accountId);
        history.setOldBalance(oldBalance);
        history.setNewBalance(newBalance);
        history.setChangeAmount(newBalance.subtract(oldBalance));
        history.setReason(reason);
        history.setCreatedAt(LocalDateTime.now());
        balanceHistoryRepository.save(history);
    }

    public BigDecimal getAvailableBalance(UUID accountId) {
        // In a real implementation, this would consider:
        // - Pending transactions
        // - Holds
        // - Credit limits
        // For now, just return the ledger balance
        return getBalance(accountId);
    }

    public boolean hasSufficientBalance(UUID accountId, BigDecimal amount) {
        return getAvailableBalance(accountId).compareTo(amount) >= 0;
    }
}
