package com.xypay.xypay.service;

import com.xypay.xypay.domain.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class BalanceCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String BALANCE_PREFIX = "wallet:balance:";

    public void cacheBalance(Wallet wallet, BigDecimal balance) {
        String key = BALANCE_PREFIX + wallet.getAccountNumber();
        redisTemplate.opsForValue().set(key, balance.toString());
    }

    public BigDecimal getCachedBalance(Wallet wallet) {
        String key = BALANCE_PREFIX + wallet.getAccountNumber();
        String balanceStr = (String) redisTemplate.opsForValue().get(key);
        return balanceStr != null ? new BigDecimal(balanceStr) : null;
    }

    public void evictBalance(Wallet wallet) {
        String key = BALANCE_PREFIX + wallet.getAccountNumber();
        redisTemplate.delete(key);
    }
}