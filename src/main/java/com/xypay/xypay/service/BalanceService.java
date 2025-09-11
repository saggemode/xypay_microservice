package com.xypay.xypay.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class BalanceService {
    private final Map<Long, BigDecimal> balanceCache = new HashMap<>();

    public BigDecimal getBalance(Long accountId) {
        return balanceCache.getOrDefault(accountId, BigDecimal.ZERO);
    }

    public void updateBalance(Long accountId, BigDecimal newBalance) {
        balanceCache.put(accountId, newBalance);
    }
}