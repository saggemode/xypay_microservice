package com.xypay.transaction.service;

import com.xypay.transaction.client.PaymentGatewayClient;
import com.xypay.transaction.domain.Transaction;
import com.xypay.transaction.dto.ExternalTransferRequest;
import com.xypay.transaction.dto.TransactionResponse;
import com.xypay.transaction.enums.TransactionStatus;
import com.xypay.transaction.enums.TransactionType;
import com.xypay.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalTransferService {
    
    private final TransactionRepository transactionRepository;
    private final PaymentGatewayClient paymentGatewayClient;
    private final TransactionProcessingService transactionProcessingService;
    private final TransactionEventPublisher transactionEventPublisher;
    
    @Transactional
    public TransactionResponse processExternalTransfer(ExternalTransferRequest request) {
        log.info("Processing external transfer: {} to bank: {}", request.getReference(), request.getDestinationBank());
        
        try {
            // 1. Validate external transfer request
            validateExternalTransfer(request);
            
            // 2. Create external transfer transaction
            Transaction transaction = createExternalTransferTransaction(request);
            
            // 3. Process through payment gateway
            TransactionResponse response = processThroughPaymentGateway(transaction, request);
            
            // 4. Publish external transfer event
            transactionEventPublisher.publishExternalTransferEvent(transaction);
            
            log.info("External transfer processed successfully: {}", transaction.getId());
            return response;
            
        } catch (Exception e) {
            log.error("External transfer processing failed: {}", e.getMessage(), e);
            throw new RuntimeException("External transfer processing failed: " + e.getMessage(), e);
        }
    }
    
    private void validateExternalTransfer(ExternalTransferRequest request) {
        if (request.getAmount().compareTo(new BigDecimal("1000")) < 0) {
            throw new IllegalArgumentException("External transfer minimum amount is ₦1,000");
        }
        
        if (request.getAmount().compareTo(new BigDecimal("1000000")) > 0) {
            throw new IllegalArgumentException("External transfer maximum amount is ₦1,000,000");
        }
        
        if (request.getDestinationBank() == null || request.getDestinationBank().isEmpty()) {
            throw new IllegalArgumentException("Destination bank is required");
        }
        
        if (request.getDestinationAccountNumber() == null || request.getDestinationAccountNumber().isEmpty()) {
            throw new IllegalArgumentException("Destination account number is required");
        }
        
        if (request.getDestinationAccountName() == null || request.getDestinationAccountName().isEmpty()) {
            throw new IllegalArgumentException("Destination account name is required");
        }
    }
    
    private Transaction createExternalTransferTransaction(ExternalTransferRequest request) {
        Transaction transaction = new Transaction();
        transaction.setAccountNumber(request.getSourceAccountNumber());
        transaction.setReference(request.getReference());
        transaction.setAmount(request.getAmount());
        transaction.setType(TransactionType.TRANSFER);
        transaction.setChannel(request.getChannel());
        transaction.setDescription("External transfer to " + request.getDestinationBank() + " - " + request.getDestinationAccountName());
        transaction.setCurrency(request.getCurrency());
        transaction.setMetadata(createExternalTransferMetadata(request));
        transaction.setIdempotencyKey(request.getIdempotencyKey() != null ? 
            request.getIdempotencyKey() : UUID.randomUUID().toString());
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setDirection("DEBIT");
        
        return transactionRepository.save(transaction);
    }
    
    private String createExternalTransferMetadata(ExternalTransferRequest request) {
        return String.format(
            "{\"destinationBank\":\"%s\",\"destinationAccountNumber\":\"%s\",\"destinationAccountName\":\"%s\",\"transferType\":\"EXTERNAL\",\"routingNumber\":\"%s\"}",
            request.getDestinationBank(),
            request.getDestinationAccountNumber(),
            request.getDestinationAccountName(),
            request.getRoutingNumber()
        );
    }
    
    private TransactionResponse processThroughPaymentGateway(Transaction transaction, ExternalTransferRequest request) {
        try {
            // Mark as processing
            transaction.markAsProcessing();
            transactionRepository.save(transaction);
            
            // Process through payment gateway
            PaymentGatewayClient.TransferRequest gatewayRequest = new PaymentGatewayClient.TransferRequest(
                request.getSourceAccountNumber(),
                request.getDestinationBank(),
                request.getDestinationAccountNumber(),
                request.getDestinationAccountName(),
                request.getAmount(),
                request.getCurrency(),
                transaction.getReference(),
                request.getRoutingNumber()
            );
            
            PaymentGatewayClient.TransferResponse gatewayResponse = 
                paymentGatewayClient.processTransfer(gatewayRequest);
            
            if (gatewayResponse.isSuccess()) {
                // Mark as successful
                transaction.markAsSuccess(transaction.getBalanceAfter());
                transaction.setMetadata(transaction.getMetadata() + 
                    ",\"gatewayTransactionId\":\"" + gatewayResponse.getGatewayTransactionId() + "\"");
                transactionRepository.save(transaction);
                
                TransactionResponse response = TransactionResponse.fromTransaction(transaction);
                response.setGatewayTransactionId(gatewayResponse.getGatewayTransactionId());
                response.setProcessingTime(calculateProcessingTime(transaction));
                
                return response;
            } else {
                // Mark as failed
                transaction.markAsFailed();
                transaction.setMetadata(transaction.getMetadata() + 
                    ",\"gatewayError\":\"" + gatewayResponse.getErrorMessage() + "\"");
                transactionRepository.save(transaction);
                
                throw new RuntimeException("Payment gateway transfer failed: " + gatewayResponse.getErrorMessage());
            }
            
        } catch (Exception e) {
            // Mark as failed
            transaction.markAsFailed();
            transactionRepository.save(transaction);
            
            log.error("Payment gateway processing failed: {}", e.getMessage(), e);
            throw new RuntimeException("Payment gateway processing failed: " + e.getMessage(), e);
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
    public TransactionResponse processInterBankTransfer(ExternalTransferRequest request) {
        log.info("Processing inter-bank transfer: {} via NIBSS", request.getReference());
        
        // For inter-bank transfers, we use NIBSS (Nigeria Inter-Bank Settlement System)
        request.setTransferType("INTER_BANK");
        return processExternalTransfer(request);
    }
    
    @Transactional
    public TransactionResponse processRTGSTransfer(ExternalTransferRequest request) {
        log.info("Processing RTGS transfer: {} for high-value transfer", request.getReference());
        
        // For high-value transfers, we use RTGS (Real Time Gross Settlement)
        if (request.getAmount().compareTo(new BigDecimal("100000")) < 0) {
            throw new IllegalArgumentException("RTGS transfers require minimum amount of ₦100,000");
        }
        
        request.setTransferType("RTGS");
        return processExternalTransfer(request);
    }
}
