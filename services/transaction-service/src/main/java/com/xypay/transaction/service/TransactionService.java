package com.xypay.transaction.service;

import com.xypay.transaction.domain.Transaction;
import com.xypay.transaction.dto.TransactionFilterRequest;
import com.xypay.transaction.dto.TransactionRequest;
import com.xypay.transaction.dto.TransactionResponse;
import com.xypay.transaction.enums.TransactionStatus;
import com.xypay.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {
    
    private final TransactionRepository transactionRepository;
    private final TransactionProcessingService transactionProcessingService;
    
    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        log.info("Creating transaction for account: {}, amount: {}, type: {}", 
                request.getAccountNumber(), request.getAmount(), request.getType());
        
        // Use the new processing service for complete transaction handling
        return transactionProcessingService.processTransaction(request);
    }
    
    @Transactional
    public TransactionResponse updateTransactionStatus(Long transactionId, String status) {
        log.info("Updating transaction {} status to {}", transactionId, status);
        
        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transaction not found with ID: " + transactionId));
        
        TransactionStatus newStatus = TransactionStatus.fromCode(status.toUpperCase());
        
        switch (newStatus) {
            case SUCCESS:
                transaction.markAsSuccess(transaction.getBalanceAfter());
                break;
            case FAILED:
                transaction.markAsFailed();
                break;
            case PENDING:
                transaction.markAsPending();
                break;
            case PROCESSING:
                transaction.markAsProcessing();
                break;
            case CANCELLED:
                transaction.markAsCancelled();
                break;
            case REVERSED:
                transaction.markAsReversed();
                break;
            default:
                throw new IllegalArgumentException("Invalid status: " + status);
        }
        
        Transaction updatedTransaction = transactionRepository.save(transaction);
        log.info("Transaction {} status updated to {}", transactionId, status);
        
        return TransactionResponse.fromTransaction(updatedTransaction);
    }
    
    @Transactional
    public TransactionResponse updateTransactionBalance(Long transactionId, BigDecimal balanceAfter) {
        log.info("Updating transaction {} balance after to {}", transactionId, balanceAfter);
        
        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transaction not found with ID: " + transactionId));
        
        transaction.setBalanceAfter(balanceAfter);
        if (transaction.isPending()) {
            transaction.markAsSuccess(balanceAfter);
        }
        
        Transaction updatedTransaction = transactionRepository.save(transaction);
        log.info("Transaction {} balance updated to {}", transactionId, balanceAfter);
        
        return TransactionResponse.fromTransaction(updatedTransaction);
    }
    
    public TransactionResponse getTransactionById(Long transactionId) {
        log.info("Retrieving transaction with ID: {}", transactionId);
        
        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transaction not found with ID: " + transactionId));
        
        return TransactionResponse.fromTransaction(transaction);
    }
    
    public TransactionResponse getTransactionByReference(String reference) {
        log.info("Retrieving transaction with reference: {}", reference);
        
        Transaction transaction = transactionRepository.findByReference(reference)
            .orElseThrow(() -> new RuntimeException("Transaction not found with reference: " + reference));
        
        return TransactionResponse.fromTransaction(transaction);
    }
    
    public List<TransactionResponse> getTransactionsByAccountNumber(String accountNumber) {
        log.info("Retrieving transactions for account: {}", accountNumber);
        
        List<Transaction> transactions = transactionRepository.findByAccountNumberOrderByTimestampDesc(accountNumber);
        
        return transactions.stream()
            .map(TransactionResponse::fromTransaction)
            .collect(Collectors.toList());
    }
    
    public Page<TransactionResponse> getTransactionsByAccountNumber(String accountNumber, int page, int size) {
        log.info("Retrieving paginated transactions for account: {}, page: {}, size: {}", accountNumber, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<Transaction> transactions = transactionRepository.findByAccountNumberOrderByTimestampDesc(accountNumber, pageable);
        
        return transactions.map(TransactionResponse::fromTransaction);
    }
    
    public Page<TransactionResponse> getTransactionsWithFilters(TransactionFilterRequest filterRequest) {
        log.info("Retrieving transactions with filters: {}", filterRequest);
        
        Sort sort = Sort.by(Sort.Direction.fromString(filterRequest.getSortDirection()), filterRequest.getSortBy());
        Pageable pageable = PageRequest.of(filterRequest.getPage(), filterRequest.getSize(), sort);
        
        Page<Transaction> transactions = transactionRepository.findTransactionsWithFilters(
            filterRequest.getWalletId(),
            filterRequest.getStatus(),
            filterRequest.getType(),
            filterRequest.getChannel(),
            filterRequest.getStartDate(),
            filterRequest.getEndDate(),
            pageable
        );
        
        return transactions.map(TransactionResponse::fromTransaction);
    }
    
    public List<TransactionResponse> getRecentTransactionsByWalletId(Long walletId, int limit) {
        log.info("Retrieving recent {} transactions for wallet: {}", limit, walletId);
        
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));
        List<Transaction> transactions = transactionRepository.findRecentTransactionsByWalletId(walletId, pageable);
        
        return transactions.stream()
            .map(TransactionResponse::fromTransaction)
            .collect(Collectors.toList());
    }
    
    public List<TransactionResponse> getTransactionsByStatus(String status) {
        log.info("Retrieving transactions with status: {}", status);
        
        List<Transaction> transactions = transactionRepository.findByStatusOrderByTimestampDesc(status);
        
        return transactions.stream()
            .map(TransactionResponse::fromTransaction)
            .collect(Collectors.toList());
    }
    
    public List<TransactionResponse> getTransactionsByType(String type) {
        log.info("Retrieving transactions with type: {}", type);
        
        List<Transaction> transactions = transactionRepository.findByTypeOrderByTimestampDesc(type);
        
        return transactions.stream()
            .map(TransactionResponse::fromTransaction)
            .collect(Collectors.toList());
    }
    
    public List<TransactionResponse> getTransactionsByChannel(String channel) {
        log.info("Retrieving transactions with channel: {}", channel);
        
        List<Transaction> transactions = transactionRepository.findByChannelOrderByTimestampDesc(channel);
        
        return transactions.stream()
            .map(TransactionResponse::fromTransaction)
            .collect(Collectors.toList());
    }
    
    public List<TransactionResponse> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Retrieving transactions between {} and {}", startDate, endDate);
        
        List<Transaction> transactions = transactionRepository.findByTimestampBetweenOrderByTimestampDesc(startDate, endDate);
        
        return transactions.stream()
            .map(TransactionResponse::fromTransaction)
            .collect(Collectors.toList());
    }
    
    public BigDecimal getTotalAmountByWalletAndType(Long walletId, String type) {
        log.info("Calculating total amount for wallet {} and type {}", walletId, type);
        
        return transactionRepository.sumAmountByWalletIdAndType(walletId, type);
    }
    
    public BigDecimal getTotalAmountByWalletAndChannel(Long walletId, String channel) {
        log.info("Calculating total amount for wallet {} and channel {}", walletId, channel);
        
        return transactionRepository.sumAmountByWalletIdAndChannel(walletId, channel);
    }
    
    public long getTransactionCountByWalletAndStatus(Long walletId, String status) {
        log.info("Counting transactions for wallet {} with status {}", walletId, status);
        
        return transactionRepository.countByWalletIdAndStatus(walletId, status);
    }
    
    @Transactional
    public void deleteTransaction(Long transactionId) {
        log.info("Deleting transaction with ID: {}", transactionId);
        
        if (!transactionRepository.existsById(transactionId)) {
            throw new RuntimeException("Transaction not found with ID: " + transactionId);
        }
        
        transactionRepository.deleteById(transactionId);
        log.info("Transaction {} deleted successfully", transactionId);
    }
}
