package com.xypay.transaction.controller;

import com.xypay.transaction.dto.ExternalTransferRequest;
import com.xypay.transaction.dto.TransactionFilterRequest;
import com.xypay.transaction.dto.TransactionRequest;
import com.xypay.transaction.dto.TransactionResponse;
import com.xypay.transaction.service.ExternalTransferService;
import com.xypay.transaction.service.ReportingService;
import com.xypay.transaction.service.RetryService;
import com.xypay.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {
    
    private final TransactionService transactionService;
    private final ExternalTransferService externalTransferService;
    private final ReportingService reportingService;
    private final RetryService retryService;
    
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody TransactionRequest request) {
        log.info("Creating transaction for wallet: {}", request.getWalletId());
        TransactionResponse response = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable Long id) {
        log.info("Retrieving transaction with ID: {}", id);
        TransactionResponse response = transactionService.getTransactionById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/reference/{reference}")
    public ResponseEntity<TransactionResponse> getTransactionByReference(@PathVariable String reference) {
        log.info("Retrieving transaction with reference: {}", reference);
        TransactionResponse response = transactionService.getTransactionByReference(reference);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByAccountNumber(@PathVariable String accountNumber) {
        log.info("Retrieving transactions for account: {}", accountNumber);
        List<TransactionResponse> responses = transactionService.getTransactionsByAccountNumber(accountNumber);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/account/{accountNumber}/paginated")
    public ResponseEntity<Page<TransactionResponse>> getTransactionsByAccountNumberPaginated(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Retrieving paginated transactions for account: {}, page: {}, size: {}", accountNumber, page, size);
        Page<TransactionResponse> responses = transactionService.getTransactionsByAccountNumber(accountNumber, page, size);
        return ResponseEntity.ok(responses);
    }
    
    @PostMapping("/search")
    public ResponseEntity<Page<TransactionResponse>> searchTransactions(@RequestBody TransactionFilterRequest filterRequest) {
        log.info("Searching transactions with filters: {}", filterRequest);
        Page<TransactionResponse> responses = transactionService.getTransactionsWithFilters(filterRequest);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/wallet/{walletId}/recent")
    public ResponseEntity<List<TransactionResponse>> getRecentTransactions(
            @PathVariable Long walletId,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Retrieving recent {} transactions for wallet: {}", limit, walletId);
        List<TransactionResponse> responses = transactionService.getRecentTransactionsByWalletId(walletId, limit);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByStatus(@PathVariable String status) {
        log.info("Retrieving transactions with status: {}", status);
        List<TransactionResponse> responses = transactionService.getTransactionsByStatus(status);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/type/{type}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByType(@PathVariable String type) {
        log.info("Retrieving transactions with type: {}", type);
        List<TransactionResponse> responses = transactionService.getTransactionsByType(type);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/channel/{channel}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByChannel(@PathVariable String channel) {
        log.info("Retrieving transactions with channel: {}", channel);
        List<TransactionResponse> responses = transactionService.getTransactionsByChannel(channel);
        return ResponseEntity.ok(responses);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<TransactionResponse> updateTransactionStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        log.info("Updating transaction {} status to {}", id, status);
        TransactionResponse response = transactionService.updateTransactionStatus(id, status);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/balance")
    public ResponseEntity<TransactionResponse> updateTransactionBalance(
            @PathVariable Long id,
            @RequestParam BigDecimal balanceAfter) {
        log.info("Updating transaction {} balance to {}", id, balanceAfter);
        TransactionResponse response = transactionService.updateTransactionBalance(id, balanceAfter);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/wallet/{walletId}/total/type/{type}")
    public ResponseEntity<BigDecimal> getTotalAmountByWalletAndType(
            @PathVariable Long walletId,
            @PathVariable String type) {
        log.info("Calculating total amount for wallet {} and type {}", walletId, type);
        BigDecimal total = transactionService.getTotalAmountByWalletAndType(walletId, type);
        return ResponseEntity.ok(total);
    }
    
    @GetMapping("/wallet/{walletId}/total/channel/{channel}")
    public ResponseEntity<BigDecimal> getTotalAmountByWalletAndChannel(
            @PathVariable Long walletId,
            @PathVariable String channel) {
        log.info("Calculating total amount for wallet {} and channel {}", walletId, channel);
        BigDecimal total = transactionService.getTotalAmountByWalletAndChannel(walletId, channel);
        return ResponseEntity.ok(total);
    }
    
    @GetMapping("/wallet/{walletId}/count/status/{status}")
    public ResponseEntity<Long> getTransactionCountByWalletAndStatus(
            @PathVariable Long walletId,
            @PathVariable String status) {
        log.info("Counting transactions for wallet {} with status {}", walletId, status);
        Long count = transactionService.getTransactionCountByWalletAndStatus(walletId, status);
        return ResponseEntity.ok(count);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        log.info("Deleting transaction with ID: {}", id);
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
    
    // External Transfer Endpoints
    @PostMapping("/external/transfer")
    public ResponseEntity<TransactionResponse> processExternalTransfer(@Valid @RequestBody ExternalTransferRequest request) {
        log.info("Processing external transfer: {}", request.getReference());
        TransactionResponse response = externalTransferService.processExternalTransfer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/external/inter-bank")
    public ResponseEntity<TransactionResponse> processInterBankTransfer(@Valid @RequestBody ExternalTransferRequest request) {
        log.info("Processing inter-bank transfer: {}", request.getReference());
        TransactionResponse response = externalTransferService.processInterBankTransfer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/external/rtgs")
    public ResponseEntity<TransactionResponse> processRTGSTransfer(@Valid @RequestBody ExternalTransferRequest request) {
        log.info("Processing RTGS transfer: {}", request.getReference());
        TransactionResponse response = externalTransferService.processRTGSTransfer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    // Reporting Endpoints
    @GetMapping("/reports/daily/{date}")
    public ResponseEntity<ReportingService.DailyTransactionReport> getDailyReport(@PathVariable LocalDate date) {
        log.info("Generating daily report for date: {}", date);
        ReportingService.DailyTransactionReport report = reportingService.generateDailyReport(date);
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/reports/monthly/{year}/{month}")
    public ResponseEntity<ReportingService.MonthlyTransactionReport> getMonthlyReport(
            @PathVariable int year, @PathVariable int month) {
        log.info("Generating monthly report for {}-{}", year, month);
        ReportingService.MonthlyTransactionReport report = reportingService.generateMonthlyReport(year, month);
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/reports/reconciliation/{date}")
    public ResponseEntity<ReportingService.ReconciliationReport> getReconciliationReport(@PathVariable LocalDate date) {
        log.info("Generating reconciliation report for date: {}", date);
        ReportingService.ReconciliationReport report = reportingService.generateReconciliationReport(date);
        return ResponseEntity.ok(report);
    }
    
    // Retry Endpoints
    @PostMapping("/{id}/retry")
    public ResponseEntity<Void> retryTransaction(@PathVariable Long id) {
        log.info("Manual retry requested for transaction: {}", id);
        retryService.retryTransaction(id);
        return ResponseEntity.accepted().build();
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Transaction Service is running");
    }
}
