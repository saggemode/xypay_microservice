package com.xypay.xypay.service;

import com.xypay.xypay.domain.*;
import com.xypay.xypay.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Transactional
public class RealTimeProcessingService {

    @Autowired
    private RealTimeProcessorRepository processorRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    // In-memory metrics for real-time tracking
    private final Map<UUID, AtomicLong> processorLoadMap = new ConcurrentHashMap<>();
    private final Map<Long, AtomicLong> processorQueueMap = new ConcurrentHashMap<>();
    
    @Async
    public CompletableFuture<String> processTransactionAsync(Transaction transaction) {
        RealTimeProcessor processor = selectOptimalProcessor(RealTimeProcessor.ProcessorType.PAYMENT_PROCESSOR);
        
        if (processor == null) {
            return CompletableFuture.completedFuture("NO_PROCESSOR_AVAILABLE");
        }
        
        // Check circuit breaker
        if (processor.getCircuitState() == RealTimeProcessor.CircuitState.OPEN) {
            return processWithBackupProcessor(transaction, processor);
        }
        
        return processWithProcessor(transaction, processor);
    }

    private CompletableFuture<String> processWithProcessor(Transaction transaction, RealTimeProcessor processor) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Increment load
            incrementProcessorLoad(processor.getId().toString());
            
            // Simulate processing time based on processor configuration
            Thread.sleep(processor.getProcessingTimeMs());
            
            // Update transaction status
            transaction.setStatus("PROCESSED");
            transaction.setProcessedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
            
            // Update processor metrics
            updateProcessorMetrics(processor, true, System.currentTimeMillis() - startTime);
            
            // Check if circuit breaker should be closed
            if (processor.getCircuitState() == RealTimeProcessor.CircuitState.HALF_OPEN) {
                processor.setCircuitState(RealTimeProcessor.CircuitState.CLOSED);
                processorRepository.save(processor);
            }
            
            return CompletableFuture.completedFuture("SUCCESS");
            
        } catch (Exception e) {
            // Handle failure
            updateProcessorMetrics(processor, false, System.currentTimeMillis() - startTime);
            
            // Check circuit breaker threshold
            checkCircuitBreaker(processor);
            
            return CompletableFuture.completedFuture("FAILED: " + e.getMessage());
            
        } finally {
            decrementProcessorLoad(processor.getId().toString());
        }
    }

    private CompletableFuture<String> processWithBackupProcessor(Transaction transaction, RealTimeProcessor primaryProcessor) {
        if (primaryProcessor.getBackupProcessorId() == null) {
            return CompletableFuture.completedFuture("NO_BACKUP_PROCESSOR");
        }
        
        RealTimeProcessor backupProcessor = processorRepository.findById(primaryProcessor.getBackupProcessorId())
            .orElse(null);
            
        if (backupProcessor == null || !backupProcessor.getIsActive()) {
            return CompletableFuture.completedFuture("BACKUP_PROCESSOR_UNAVAILABLE");
        }
        
        return processWithProcessor(transaction, backupProcessor);
    }

    public RealTimeProcessor selectOptimalProcessor(RealTimeProcessor.ProcessorType type) {
        List<RealTimeProcessor> processors = processorRepository.findByProcessorTypeAndIsActiveTrueOrderByCurrentLoadAsc(type);
        
        for (RealTimeProcessor processor : processors) {
            // Check if processor is healthy and has capacity
            if (processor.getStatus() == RealTimeProcessor.ProcessorStatus.HEALTHY &&
                processor.getCurrentLoad() < processor.getMaxThroughputPerSecond() &&
                processor.getQueueSize() < processor.getMaxQueueSize()) {
                return processor;
            }
        }
        
        return null; // No available processor
    }

    public void updateProcessorMetrics(RealTimeProcessor processor, boolean success, long responseTime) {
        processor.setAverageResponseTimeMs(
            (processor.getAverageResponseTimeMs() + responseTime) / 2
        );
        
        processor.setTotalTransactionsProcessed(processor.getTotalTransactionsProcessed() + 1);
        processor.setTransactionsToday(processor.getTransactionsToday() + 1);
        
        // Update success/error rates
        if (success) {
            BigDecimal currentSuccessRate = processor.getSuccessRate();
            BigDecimal newSuccessRate = currentSuccessRate.multiply(new BigDecimal("0.99"))
                .add(new BigDecimal("0.01"));
            processor.setSuccessRate(newSuccessRate);
            
            processor.setErrorRate(new BigDecimal("100").subtract(newSuccessRate));
        } else {
            BigDecimal currentErrorRate = processor.getErrorRate();
            BigDecimal newErrorRate = currentErrorRate.multiply(new BigDecimal("0.99"))
                .add(new BigDecimal("0.01"));
            processor.setErrorRate(newErrorRate);
            
            processor.setSuccessRate(new BigDecimal("100").subtract(newErrorRate));
        }
        
        processor.setLastHealthCheck(LocalDateTime.now());
        processorRepository.save(processor);
    }

    private void checkCircuitBreaker(RealTimeProcessor processor) {
        if (!processor.getCircuitBreakerEnabled()) {
            return;
        }
        
        // Simple circuit breaker logic based on error rate
        if (processor.getErrorRate().compareTo(new BigDecimal(processor.getFailureThreshold())) > 0) {
            processor.setCircuitState(RealTimeProcessor.CircuitState.OPEN);
            
            // Schedule recovery attempt
            scheduleCircuitBreakerRecovery(processor);
        }
        
        processorRepository.save(processor);
    }

    @Async
    public void scheduleCircuitBreakerRecovery(RealTimeProcessor processor) {
        try {
            Thread.sleep(processor.getRecoveryTimeoutSeconds() * 1000L);
            
            // Attempt to move to half-open state
            processor.setCircuitState(RealTimeProcessor.CircuitState.HALF_OPEN);
            processorRepository.save(processor);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void performHealthCheck() {
        List<RealTimeProcessor> processors = processorRepository.findAll();
        
        for (RealTimeProcessor processor : processors) {
            // Check if processor is responding
            boolean isHealthy = checkProcessorHealth(processor);
            
            RealTimeProcessor.ProcessorStatus newStatus;
            if (isHealthy) {
                if (processor.getCurrentLoad() > processor.getMaxThroughputPerSecond() * 0.8) {
                    newStatus = RealTimeProcessor.ProcessorStatus.DEGRADED;
                } else {
                    newStatus = RealTimeProcessor.ProcessorStatus.HEALTHY;
                }
            } else {
                newStatus = RealTimeProcessor.ProcessorStatus.CRITICAL;
            }
            
            processor.setStatus(newStatus);
            processor.setLastHealthCheck(LocalDateTime.now());
            processorRepository.save(processor);
            
            // Send alerts for critical processors
            if (newStatus == RealTimeProcessor.ProcessorStatus.CRITICAL) {
                sendProcessorAlert(processor);
            }
        }
    }

    private boolean checkProcessorHealth(RealTimeProcessor processor) {
        // Simulate health check - in real implementation, this would ping the processor
        return processor.getErrorRate().compareTo(new BigDecimal("10")) < 0 && 
               processor.getCurrentLoad() < processor.getMaxThroughputPerSecond();
    }

    private void sendProcessorAlert(RealTimeProcessor processor) {
        try {
            notificationService.sendNotification(UUID.randomUUID(), "SYSTEM_ALERT", 
                "Processor " + processor.getProcessorName() + " is in critical state");
        } catch (Exception e) {
            // Log error but don't fail
        }
    }

    public void incrementProcessorLoad(String processorId) {
        UUID id = UUID.fromString(processorId);
        processorLoadMap.computeIfAbsent(id, k -> new AtomicLong(0)).incrementAndGet();
        
        // Update database
        processorRepository.findById(id).ifPresent(processor -> {
            processor.setCurrentLoad(processorLoadMap.get(id).get());
            processorRepository.save(processor);
        });
    }

    public void decrementProcessorLoad(String processorId) {
        UUID id = UUID.fromString(processorId);
        AtomicLong load = processorLoadMap.get(id);
        if (load != null && load.get() > 0) {
            load.decrementAndGet();
            
            // Update database
            processorRepository.findById(id).ifPresent(processor -> {
                processor.setCurrentLoad(load.get());
                processorRepository.save(processor);
            });
        }
    }

    public void resetDailyMetrics() {
        List<RealTimeProcessor> processors = processorRepository.findAll();
        
        for (RealTimeProcessor processor : processors) {
            processor.setTransactionsToday(0L);
            processor.setPeakTpsToday(0L);
            processorRepository.save(processor);
        }
    }

    public Map<String, Object> getSystemMetrics() {
        List<RealTimeProcessor> processors = processorRepository.findAll();
        
        long totalThroughput = processors.stream()
            .mapToLong(RealTimeProcessor::getMaxThroughputPerSecond)
            .sum();
            
        long currentLoad = processors.stream()
            .mapToLong(RealTimeProcessor::getCurrentLoad)
            .sum();
            
        long totalTransactionsToday = processors.stream()
            .mapToLong(RealTimeProcessor::getTransactionsToday)
            .sum();
            
        BigDecimal averageSuccessRate = processors.stream()
            .map(RealTimeProcessor::getSuccessRate)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(new BigDecimal(processors.size()), 2, java.math.RoundingMode.HALF_UP);
        
        Map<String, Object> metrics = new ConcurrentHashMap<>();
        metrics.put("totalProcessors", processors.size());
        metrics.put("healthyProcessors", processors.stream().mapToLong(p -> 
            p.getStatus() == RealTimeProcessor.ProcessorStatus.HEALTHY ? 1 : 0).sum());
        metrics.put("totalThroughputCapacity", totalThroughput);
        metrics.put("currentSystemLoad", currentLoad);
        metrics.put("systemUtilization", currentLoad * 100.0 / totalThroughput);
        metrics.put("transactionsProcessedToday", totalTransactionsToday);
        metrics.put("averageSuccessRate", averageSuccessRate);
        metrics.put("supports24x7", processors.stream().allMatch(RealTimeProcessor::getSupports24x7));
        
        return metrics;
    }

    public List<RealTimeProcessor> createDefaultProcessors() {
        // Create default real-time processors for 24/7 operations
        List<RealTimeProcessor> processors = List.of(
            createProcessor("Payment Processor 1", RealTimeProcessor.ProcessorType.PAYMENT_PROCESSOR, 5000L),
            createProcessor("Payment Processor 2", RealTimeProcessor.ProcessorType.PAYMENT_PROCESSOR, 5000L),
            createProcessor("Transaction Validator", RealTimeProcessor.ProcessorType.TRANSACTION_VALIDATOR, 10000L),
            createProcessor("Fraud Detector", RealTimeProcessor.ProcessorType.FRAUD_DETECTOR, 3000L),
            createProcessor("Compliance Checker", RealTimeProcessor.ProcessorType.COMPLIANCE_CHECKER, 2000L),
            createProcessor("Notification Sender", RealTimeProcessor.ProcessorType.NOTIFICATION_SENDER, 8000L),
            createProcessor("STP Processor", RealTimeProcessor.ProcessorType.STP_PROCESSOR, 4000L)
        );
        
        // Set up backup relationships
        processors.get(0).setBackupProcessorId(processors.get(1).getId()); // Payment processors backup each other
        processors.get(1).setBackupProcessorId(processors.get(0).getId());
        
        return processorRepository.saveAll(processors);
    }

    private RealTimeProcessor createProcessor(String name, RealTimeProcessor.ProcessorType type, Long throughput) {
        RealTimeProcessor processor = new RealTimeProcessor();
        processor.setProcessorName(name);
        processor.setProcessorType(type);
        processor.setIsActive(true);
        processor.setMaxThroughputPerSecond(throughput);
        processor.setMaxQueueSize(throughput * 2); // 2x throughput for queue
        processor.setProcessingTimeMs(50L); // 50ms average processing time
        processor.setSuccessRate(new BigDecimal("99.99"));
        processor.setErrorRate(new BigDecimal("0.01"));
        processor.setStatus(RealTimeProcessor.ProcessorStatus.HEALTHY);
        processor.setSupports24x7(true);
        processor.setAutoFailoverEnabled(true);
        processor.setCircuitBreakerEnabled(true);
        processor.setFailureThreshold(5);
        processor.setRecoveryTimeoutSeconds(60);
        processor.setCircuitState(RealTimeProcessor.CircuitState.CLOSED);
        processor.setLastHealthCheck(LocalDateTime.now());
        
        return processor;
    }
}
