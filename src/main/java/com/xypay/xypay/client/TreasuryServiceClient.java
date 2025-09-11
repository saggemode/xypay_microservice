package com.xypay.xypay.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@FeignClient(name = "treasury-service")
public interface TreasuryServiceClient {
    
    @GetMapping("/api/treasury/positions")
    List<Map<String, Object>> getTreasuryPositions();
    
    @GetMapping("/api/treasury/liquidity")
    Map<String, Object> getLiquidityStatus();
    
    @PostMapping("/api/treasury/liquidity/transfer")
    Map<String, Object> transferLiquidity(
            @RequestParam("fromAccount") String fromAccount,
            @RequestParam("toAccount") String toAccount,
            @RequestParam("amount") BigDecimal amount);
    
    @GetMapping("/api/treasury/risk-metrics")
    Map<String, Object> getRiskMetrics();
}
