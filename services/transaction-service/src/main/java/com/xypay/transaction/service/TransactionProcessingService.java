package com.xypay.transaction.service;

import com.xypay.transaction.client.AccountServiceClient;
import com.xypay.transaction.domain.Transaction;
import com.xypay.transaction.dto.TransactionRequest;
import com.xypay.transaction.dto.TransactionResponse;
import com.xypay.transaction.enums.TransactionStatus;
import com.xypay.transaction.enums.TransactionType;
import com.xypay.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionProcessingService {
    
    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;
    private final TransactionFeeService transactionFeeService;
    private final TransactionEventPublisher transactionEventPublisher;
    
    @Transactional
    public TransactionResponse processTransaction(TransactionRequest request) {
        log.info("Processing transaction: {} for account: {}", request.getReference(), request.getAccountNumber());
        
        try {
            // 1. Validate transaction
            validateTransaction(request);
            
            // 2. Check for duplicate transaction
            Transaction existingTransaction = checkForDuplicate(request);
            if (existingTransaction != null) {
                log.warn("Duplicate transaction found: {}", request.getIdempotencyKey());
                return TransactionResponse.fromTransaction(existingTransaction);
            }
            
            // 3. Create transaction record
            Transaction transaction = createTransactionRecord(request);
            
            // 4. Validate account and limits
            validateAccountAndLimits(transaction);
            
            // 5. Calculate fees
            BigDecimal feeAmount = transactionFeeService.calculateFee(transaction);
            
            // 6. Process the transaction
            TransactionResponse response = processTransactionLogic(transaction, feeAmount);
            
            // 7. Publish transaction event
            transactionEventPublisher.publishTransactionEvent(transaction);
            
            log.info("Transaction processed successfully: {}", transaction.getId());
            return response;
            
        } catch (Exception e) {
            log.error("Transaction processing failed: {}", e.getMessage(), e);
            throw new RuntimeException("Transaction processing failed: " + e.getMessage(), e);
        }
    }
    
    private void validateTransaction(TransactionRequest request) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }
        
        if (request.getType() == null) {
            throw new IllegalArgumentException("Transaction type is required");
        }
        
        if (request.getChannel() == null) {
            throw new IllegalArgumentException("Transaction channel is required");
        }
        
        // Additional validations based on transaction type
        if (request.getType() == TransactionType.TRANSFER && 
            (request.getReceiverAccountNumber() == null || request.getReceiverAccountNumber().isEmpty())) {
            throw new IllegalArgumentException("Receiver account number is required for transfers");
        }
    }
    
    private Transaction checkForDuplicate(TransactionRequest request) {
        if (request.getIdempotencyKey() != null) {
            return transactionRepository.findByIdempotencyKey(request.getIdempotencyKey()).orElse(null);
        }
        return null;
    }
    
    private Transaction createTransactionRecord(TransactionRequest request) {
        Transaction transaction = new Transaction();
        transaction.setAccountNumber(request.getAccountNumber());
        transaction.setReceiverAccountNumber(request.getReceiverAccountNumber());
        transaction.setReference(request.getReference());
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setChannel(request.getChannel());
        transaction.setDescription(request.getDescription());
        transaction.setCurrency(request.getCurrency());
        transaction.setMetadata(request.getMetadata());
        transaction.setIdempotencyKey(request.getIdempotencyKey() != null ? 
            request.getIdempotencyKey() : UUID.randomUUID().toString());
        transaction.setParentId(request.getParentId());
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setDirection(request.getType().isDebit() ? "DEBIT" : "CREDIT");
        
        return transactionRepository.save(transaction);
    }
    
    private void validateAccountAndLimits(Transaction transaction) {
        try {
            // Validate transaction with Account Service
            AccountServiceClient.TransactionValidationRequest validationRequest = 
                new AccountServiceClient.TransactionValidationRequest(
                    transaction.getAmount(),
                    transaction.getType().getCode(),
                    transaction.getChannel().getCode(),
                    transaction.getCurrency()
                );
            
            ResponseEntity<Map<String, Object>> validationResponse = 
                accountServiceClient.validateTransaction(transaction.getAccountNumber(), validationRequest);
            
            if (!validationResponse.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Transaction validation failed");
            }
            
            Map<String, Object> validationResult = validationResponse.getBody();
            if (validationResult != null && !(Boolean) validationResult.get("valid")) {
                throw new RuntimeException("Transaction validation failed: " + validationResult.get("message"));
            }
            
        } catch (Exception e) {
            log.error("Account validation failed: {}", e.getMessage());
            throw new RuntimeException("Account validation failed: " + e.getMessage(), e);
        }
    }
    
    private TransactionResponse processTransactionLogic(Transaction transaction, BigDecimal feeAmount) {
        try {
            // Mark as processing
            transaction.markAsProcessing();
            transactionRepository.save(transaction);
            
            BigDecimal totalAmount = transaction.getAmount().add(feeAmount);
            
            if (transaction.getType().isDebit()) {
                // Debit transaction
                processDebitTransaction(transaction, totalAmount);
            } else {
                // Credit transaction
                processCreditTransaction(transaction, totalAmount);
            }
            
            // Get updated balance
            ResponseEntity<Map<String, Object>> balanceResponse = 
                accountServiceClient.getAccountBalance(transaction.getAccountNumber());
            
            BigDecimal newBalance = BigDecimal.ZERO;
            if (balanceResponse.getStatusCode().is2xxSuccessful() && balanceResponse.getBody() != null) {
                newBalance = new BigDecimal(balanceResponse.getBody().get("balance").toString());
            }
            
            // Mark as successful
            transaction.markAsSuccess(newBalance);
            transactionRepository.save(transaction);
            
            TransactionResponse response = TransactionResponse.fromTransaction(transaction);
            response.setFeeAmount(feeAmount);
            response.setProcessingTime(calculateProcessingTime(transaction));
            
            return response;
            
        } catch (Exception e) {
            // Mark as failed
            transaction.markAsFailed();
            transactionRepository.save(transaction);
            
            log.error("Transaction processing failed: {}", e.getMessage(), e);
            throw new RuntimeException("Transaction processing failed: " + e.getMessage(), e);
        }
    }
    
    private void processDebitTransaction(Transaction transaction, BigDecimal totalAmount) {
        AccountServiceClient.DebitRequest debitRequest = new AccountServiceClient.DebitRequest(
            totalAmount,
            transaction.getReference(),
            transaction.getDescription(),
            transaction.getType().getCode()
        );
        
        ResponseEntity<Map<String, Object>> response = 
            accountServiceClient.debitAccount(transaction.getAccountNumber(), debitRequest);
        
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Account debit failed");
        }
    }
    
    private void processCreditTransaction(Transaction transaction, BigDecimal amount) {
        AccountServiceClient.CreditRequest creditRequest = new AccountServiceClient.CreditRequest(
            amount,
            transaction.getReference(),
            transaction.getDescription(),
            transaction.getType().getCode()
        );
        
        ResponseEntity<Map<String, Object>> response = 
            accountServiceClient.creditAccount(transaction.getAccountNumber(), creditRequest);
        
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Account credit failed");
        }
    }
    
    private String calculateProcessingTime(Transaction transaction) {
        if (transaction.getProcessedAt() != null && transaction.getTimestamp() != null) {
            long processingTimeMs = java.time.Duration.between(
                transaction.getTimestamp(), 
                transaction.getProcessedAt()
            ).toMillis();
            return processingTimeMs + "ms";
        }
        return "N/A";
    }
    
    @Transactional
    public TransactionResponse reverseTransaction(Long transactionId, String reason) {
        log.info("Reversing transaction: {} with reason: {}", transactionId, reason);
        
        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        
        if (!transaction.canBeReversed()) {
            throw new RuntimeException("Transaction cannot be reversed: " + transaction.getStatus());
        }
        
        try {
            // Create reversal transaction
            Transaction reversalTransaction = new Transaction();
            reversalTransaction.setAccountNumber(transaction.getAccountNumber());
            reversalTransaction.setReference("REV_" + transaction.getReference());
            reversalTransaction.setAmount(transaction.getAmount());
            reversalTransaction.setType(transaction.getType() == TransactionType.DEPOSIT ? 
                TransactionType.WITHDRAWAL : TransactionType.DEPOSIT);
            reversalTransaction.setChannel(transaction.getChannel());
            reversalTransaction.setDescription("Reversal: " + transaction.getDescription() + " - " + reason);
            reversalTransaction.setCurrency(transaction.getCurrency());
            reversalTransaction.setParentId(transaction.getId());
            reversalTransaction.setStatus(TransactionStatus.PENDING);
            reversalTransaction.setDirection(transaction.getType().isDebit() ? "CREDIT" : "DEBIT");
            reversalTransaction.setIdempotencyKey(UUID.randomUUID().toString());
            
            Transaction savedReversal = transactionRepository.save(reversalTransaction);
            
            // Process the reversal
            TransactionResponse response = processTransactionLogic(savedReversal, BigDecimal.ZERO);
            
            // Mark original transaction as reversed
            transaction.markAsReversed();
            transactionRepository.save(transaction);
            
            log.info("Transaction reversed successfully: {}", transactionId);
            return response;
            
        } catch (Exception e) {
            log.error("Transaction reversal failed: {}", e.getMessage(), e);
            throw new RuntimeException("Transaction reversal failed: " + e.getMessage(), e);
        }
    }
}
