package com.xypay.analytics.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "transaction-service")
public interface TransactionServiceClient {
    
    @GetMapping("/api/transactions/wallet/{walletId}")
    List<TransactionResponse> getTransactionsByWalletId(@PathVariable Long walletId);
    
    @GetMapping("/api/transactions/wallet/{walletId}/recent")
    List<TransactionResponse> getRecentTransactions(@PathVariable Long walletId, @RequestParam int limit);
    
    @GetMapping("/api/transactions/type/{type}")
    List<TransactionResponse> getTransactionsByType(@PathVariable String type);
    
    class TransactionResponse {
        public Long id;
        public Long walletId;
        public Long receiverId;
        public String reference;
        public java.math.BigDecimal amount;
        public String type;
        public String channel;
        public String description;
        public String status;
        public String currency;
        public LocalDateTime timestamp;
        public LocalDateTime createdAt;
    }
}
