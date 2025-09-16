package com.xypay.xypay.service;

import com.xypay.xypay.domain.*;
import com.xypay.xypay.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.UUID;

@Service
@Transactional
public class BulkTransferService {
    
    private static final Logger logger = LoggerFactory.getLogger(BulkTransferService.class);
    
    @Autowired
    private BulkTransferRepository bulkTransferRepository;
    
    @Autowired
    private BulkTransferItemRepository bulkTransferItemRepository;
    
    @Autowired
    private BankTransferProcessingService bankTransferProcessingService;
    
    @Autowired
    private TransactionCreationService transactionCreationService;
    
    @Autowired
    private WalletService walletService;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Create a new bulk transfer from CSV file
     */
    public BulkTransfer createBulkTransfer(User user, MultipartFile file, String description) {
        try {
            String batchId = "BULK-" + System.currentTimeMillis();
            BulkTransfer bulkTransfer = new BulkTransfer(user, batchId, description);
            bulkTransfer = bulkTransferRepository.save(bulkTransfer);
            
            // Parse CSV file and create transfer items
            List<BulkTransferItem> items = parseCSVFile(file, bulkTransfer);
            bulkTransferItemRepository.saveAll(items);
            
            // Calculate totals
            calculateBulkTransferTotals(bulkTransfer);
            
            logger.info("Created bulk transfer {} with {} items", batchId, items.size());
            return bulkTransfer;
            
        } catch (Exception e) {
            logger.error("Error creating bulk transfer: {}", e.getMessage());
            throw new RuntimeException("Failed to create bulk transfer", e);
        }
    }
    
    /**
     * Process bulk transfer asynchronously
     */
    @Async
    public CompletableFuture<BulkTransfer> processBulkTransfer(UUID bulkTransferId) {
        try {
            BulkTransfer bulkTransfer = bulkTransferRepository.findById(bulkTransferId)
                    .orElseThrow(() -> new RuntimeException("Bulk transfer not found"));
            
            bulkTransfer.startProcessing();
            bulkTransferRepository.save(bulkTransfer);
            
            List<BulkTransferItem> items = bulkTransferItemRepository.findByBulkTransfer(bulkTransfer);
            
            int successful = 0;
            int failed = 0;
            BigDecimal processedAmount = BigDecimal.ZERO;
            
            for (BulkTransferItem item : items) {
                try {
                    // Process individual transfer
                    processBulkTransferItem(item);
                    successful++;
                    processedAmount = processedAmount.add(item.getAmount());
                    
                } catch (Exception e) {
                    item.markAsFailed(e.getMessage());
                    failed++;
                    logger.error("Failed to process bulk transfer item {}: {}", item.getId(), e.getMessage());
                }
                
                bulkTransferItemRepository.save(item);
            }
            
            // Update bulk transfer status
            bulkTransfer.setSuccessfulTransfers(successful);
            bulkTransfer.setFailedTransfers(failed);
            bulkTransfer.setProcessedAmount(processedAmount);
            
            if (failed == 0) {
                bulkTransfer.completeProcessing();
            } else if (successful > 0) {
                bulkTransfer.markAsPartiallyCompleted();
            } else {
                bulkTransfer.markAsFailed("All transfers failed");
            }
            
            bulkTransferRepository.save(bulkTransfer);
            
            // Send notification
            notificationService.sendNotification(
                bulkTransfer.getUser().getId(),
                "BULK_TRANSFER_COMPLETED",
                String.format("Bulk transfer %s completed. %d successful, %d failed", 
                    bulkTransfer.getBatchId(), successful, failed)
            );
            
            logger.info("Completed bulk transfer {}: {} successful, {} failed", 
                bulkTransfer.getBatchId(), successful, failed);
            
            return CompletableFuture.completedFuture(bulkTransfer);
            
        } catch (Exception e) {
            logger.error("Error processing bulk transfer {}: {}", bulkTransferId, e.getMessage());
            throw new RuntimeException("Failed to process bulk transfer", e);
        }
    }
    
    /**
     * Process individual bulk transfer item
     */
    private void processBulkTransferItem(BulkTransferItem item) {
        item.markAsProcessing();
        
        // Check if recipient is internal (XYPay account)
        Wallet recipientWallet = walletService.getWalletByAnyAccountNumber(item.getRecipientAccountNumber()).orElse(null);
        
        if (recipientWallet != null) {
            // Internal transfer
            processInternalBulkTransfer(item, recipientWallet);
        } else {
            // External transfer
            processExternalBulkTransfer(item);
        }
    }
    
    /**
     * Process internal bulk transfer
     */
    private void processInternalBulkTransfer(BulkTransferItem item, Wallet recipientWallet) {
        try {
            // Create internal transfer
            TransactionCreationService.TransactionPair transactionPair = transactionCreationService
                    .createTransactionRecords(
                        item.getBulkTransfer().getUser().getWallet(),
                        recipientWallet,
                        item.getAmount(),
                        null, // No BankTransfer for internal transfers
                        "Bulk transfer: " + item.getDescription()
                    );
            
            item.markAsCompleted(transactionPair.getSenderTransaction().getId().getMostSignificantBits(), null);
            
        } catch (Exception e) {
            item.markAsFailed("Internal transfer failed: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Process external bulk transfer
     */
    private void processExternalBulkTransfer(BulkTransferItem item) {
        try {
            // Create external bank transfer
            BankTransfer bankTransfer = new BankTransfer();
            bankTransfer.setUser(item.getBulkTransfer().getUser());
            bankTransfer.setAccountNumber(item.getRecipientAccountNumber());
            bankTransfer.setBankName("Unknown Bank"); // Default bank name
            bankTransfer.setBankCode(item.getRecipientBankCode());
            bankTransfer.setAmount(item.getAmount());
            bankTransfer.setDescription(item.getDescription());
            bankTransfer.setReference(item.getReference());
            bankTransfer.setStatus("PENDING");
            
            BankTransfer createdTransfer = bankTransferProcessingService.createAndProcessTransfer(
                item.getBulkTransfer().getUser(),
                "Unknown Bank", // Default bank name
                item.getRecipientBankCode(),
                item.getRecipientAccountNumber(),
                item.getAmount(),
                item.getDescription()
            );
            
            item.markAsCompleted(null, createdTransfer.getId().getMostSignificantBits());
            
        } catch (Exception e) {
            item.markAsFailed("External transfer failed: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Parse CSV file and create transfer items
     */
    private List<BulkTransferItem> parseCSVFile(MultipartFile file, BulkTransfer bulkTransfer) {
        List<BulkTransferItem> items = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int rowNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                rowNumber++;
                if (rowNumber == 1) continue; // Skip header
                
                String[] columns = line.split(",");
                if (columns.length >= 4) {
                    BulkTransferItem item = new BulkTransferItem();
                    item.setBulkTransfer(bulkTransfer);
                    item.setRecipientAccountNumber(columns[0].trim());
                    item.setRecipientName(columns[1].trim());
                    item.setRecipientBankCode(columns[2].trim());
                    item.setAmount(new BigDecimal(columns[3].trim()));
                    item.setDescription(columns.length > 4 ? columns[4].trim() : "");
                    item.setRowNumber(rowNumber);
                    
                    items.add(item);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error parsing CSV file: {}", e.getMessage());
            throw new RuntimeException("Failed to parse CSV file", e);
        }
        
        return items;
    }
    
    /**
     * Calculate bulk transfer totals
     */
    private void calculateBulkTransferTotals(BulkTransfer bulkTransfer) {
        List<BulkTransferItem> items = bulkTransferItemRepository.findByBulkTransfer(bulkTransfer);
        
        BigDecimal totalAmount = items.stream()
                .map(BulkTransferItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        bulkTransfer.setTotalAmount(totalAmount);
        bulkTransfer.setTotalRecipients(items.size());
        
        bulkTransferRepository.save(bulkTransfer);
    }
    
    /**
     * Get bulk transfer by ID
     */
    public BulkTransfer getBulkTransfer(UUID id) {
        return bulkTransferRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bulk transfer not found"));
    }
    
    /**
     * Get bulk transfers by user
     */
    public List<BulkTransfer> getBulkTransfersByUser(User user) {
        return bulkTransferRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    /**
     * Get bulk transfer items
     */
    public List<BulkTransferItem> getBulkTransferItems(UUID bulkTransferId) {
        BulkTransfer bulkTransfer = getBulkTransfer(bulkTransferId);
        return bulkTransferItemRepository.findByBulkTransfer(bulkTransfer);
    }
    
    /**
     * Cancel bulk transfer
     */
    public BulkTransfer cancelBulkTransfer(UUID id) {
        BulkTransfer bulkTransfer = getBulkTransfer(id);
        
        if (bulkTransfer.isProcessing()) {
            throw new RuntimeException("Cannot cancel bulk transfer that is being processed");
        }
        
        bulkTransfer.cancel();
        return bulkTransferRepository.save(bulkTransfer);
    }
}
