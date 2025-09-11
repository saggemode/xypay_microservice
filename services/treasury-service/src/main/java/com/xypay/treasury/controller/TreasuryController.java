package com.xypay.treasury.controller;

import com.xypay.treasury.dto.LiquidityForecastRequest;
import com.xypay.treasury.dto.LiquidityForecastResponse;
import com.xypay.treasury.dto.TreasuryPositionRequest;
import com.xypay.treasury.dto.TreasuryPositionResponse;
import com.xypay.treasury.service.LiquidityManagementService;
import com.xypay.treasury.service.TreasuryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/treasury")
@RequiredArgsConstructor
@Slf4j
public class TreasuryController {
    
    private final TreasuryService treasuryService;
    private final LiquidityManagementService liquidityManagementService;
    
    // Treasury Position endpoints
    @PostMapping("/positions")
    public ResponseEntity<TreasuryPositionResponse> createPosition(@Valid @RequestBody TreasuryPositionRequest request) {
        log.info("Creating treasury position for currency: {}", request.getCurrencyCode());
        TreasuryPositionResponse response = treasuryService.createPosition(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/positions/{id}")
    public ResponseEntity<TreasuryPositionResponse> getPositionById(@PathVariable Long id) {
        log.info("Retrieving treasury position with ID: {}", id);
        TreasuryPositionResponse response = treasuryService.getPositionById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/positions")
    public ResponseEntity<List<TreasuryPositionResponse>> getAllPositions() {
        log.info("Retrieving all treasury positions");
        List<TreasuryPositionResponse> responses = treasuryService.getAllPositions();
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/positions/currency/{currencyCode}")
    public ResponseEntity<List<TreasuryPositionResponse>> getPositionsByCurrency(@PathVariable String currencyCode) {
        log.info("Retrieving treasury positions for currency: {}", currencyCode);
        List<TreasuryPositionResponse> responses = treasuryService.getPositionsByCurrency(currencyCode);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/positions/active")
    public ResponseEntity<List<TreasuryPositionResponse>> getActivePositions() {
        log.info("Retrieving active treasury positions");
        List<TreasuryPositionResponse> responses = treasuryService.getActivePositions();
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/positions/type/{positionType}")
    public ResponseEntity<List<TreasuryPositionResponse>> getPositionsByType(@PathVariable String positionType) {
        log.info("Retrieving treasury positions by type: {}", positionType);
        List<TreasuryPositionResponse> responses = treasuryService.getPositionsByType(positionType);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/positions/liquidity-bucket/{liquidityBucket}")
    public ResponseEntity<List<TreasuryPositionResponse>> getPositionsByLiquidityBucket(@PathVariable String liquidityBucket) {
        log.info("Retrieving treasury positions by liquidity bucket: {}", liquidityBucket);
        List<TreasuryPositionResponse> responses = treasuryService.getPositionsByLiquidityBucket(liquidityBucket);
        return ResponseEntity.ok(responses);
    }
    
    @PutMapping("/positions/{id}")
    public ResponseEntity<TreasuryPositionResponse> updatePosition(
            @PathVariable Long id, 
            @Valid @RequestBody TreasuryPositionRequest request) {
        log.info("Updating treasury position with ID: {}", id);
        TreasuryPositionResponse response = treasuryService.updatePosition(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/positions/{id}")
    public ResponseEntity<Void> deletePosition(@PathVariable Long id) {
        log.info("Deleting treasury position with ID: {}", id);
        treasuryService.deletePosition(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/positions/{id}/deactivate")
    public ResponseEntity<Void> deactivatePosition(@PathVariable Long id) {
        log.info("Deactivating treasury position with ID: {}", id);
        treasuryService.deactivatePosition(id);
        return ResponseEntity.ok().build();
    }
    
    // Liquidity Management endpoints
    @PostMapping("/liquidity/forecast")
    public ResponseEntity<LiquidityForecastResponse> generateLiquidityForecast(@Valid @RequestBody LiquidityForecastRequest request) {
        log.info("Generating liquidity forecast for currency: {}, days: {}", 
                request.getCurrencyCode(), request.getForecastDays());
        LiquidityForecastResponse response = liquidityManagementService.generateForecast(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/liquidity/rebalance")
    public ResponseEntity<String> rebalancePositions() {
        log.info("Starting treasury position rebalancing");
        liquidityManagementService.rebalancePositions();
        return ResponseEntity.ok("Rebalancing completed successfully");
    }
    
    @PostMapping("/liquidity/monitor")
    public ResponseEntity<String> monitorLiquidity() {
        log.info("Starting liquidity monitoring");
        liquidityManagementService.monitorLiquidity();
        return ResponseEntity.ok("Liquidity monitoring completed");
    }
    
    // Summary and Analytics endpoints
    @GetMapping("/summary/currency/{currencyCode}/total-amount")
    public ResponseEntity<BigDecimal> getTotalPositionAmountByCurrency(@PathVariable String currencyCode) {
        log.info("Calculating total position amount for currency: {}", currencyCode);
        BigDecimal total = treasuryService.getTotalPositionAmountByCurrency(currencyCode);
        return ResponseEntity.ok(total);
    }
    
    @GetMapping("/summary/currency/{currencyCode}/available-amount")
    public ResponseEntity<BigDecimal> getTotalAvailableAmountByCurrency(@PathVariable String currencyCode) {
        log.info("Calculating total available amount for currency: {}", currencyCode);
        BigDecimal total = treasuryService.getTotalAvailableAmountByCurrency(currencyCode);
        return ResponseEntity.ok(total);
    }
    
    @GetMapping("/summary/currency/{currencyCode}/reserved-amount")
    public ResponseEntity<BigDecimal> getTotalReservedAmountByCurrency(@PathVariable String currencyCode) {
        log.info("Calculating total reserved amount for currency: {}", currencyCode);
        BigDecimal total = treasuryService.getTotalReservedAmountByCurrency(currencyCode);
        return ResponseEntity.ok(total);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Treasury Service is running");
    }
}