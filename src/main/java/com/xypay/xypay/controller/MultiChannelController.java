package com.xypay.xypay.controller;

import com.xypay.xypay.domain.Channel;
import com.xypay.xypay.domain.ChannelTransaction;
import com.xypay.xypay.domain.Transaction;
import com.xypay.xypay.service.MultiChannelIntegrationService;
import com.xypay.xypay.repository.ChannelRepository;
import com.xypay.xypay.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/channels")
public class MultiChannelController {

    @Autowired
    private MultiChannelIntegrationService multiChannelService;
    
    @Autowired
    private ChannelRepository channelRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;

    @GetMapping
    public ResponseEntity<List<Channel>> getAllChannels() {
        List<Channel> channels = channelRepository.findByIsActiveTrue();
        return ResponseEntity.ok(channels);
    }

    @GetMapping("/type/{channelType}")
    public ResponseEntity<List<Channel>> getChannelsByType(@PathVariable String channelType) {
        List<Channel> channels = channelRepository.findByChannelTypeAndIsActiveTrue(channelType);
        return ResponseEntity.ok(channels);
    }

    @PostMapping("/process")
    public ResponseEntity<ChannelTransaction> processChannelTransaction(
            @RequestParam String channelType,
            @RequestParam Long transactionId,
            @RequestBody Map<String, Object> channelData) {
        
        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transaction not found"));
        
        ChannelTransaction channelTxn = multiChannelService.processChannelTransaction(
            channelType, transaction, channelData);
        
        return ResponseEntity.ok(channelTxn);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getChannelStatistics() {
        Map<String, Object> stats = multiChannelService.getChannelStatistics();
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/setup-default")
    public ResponseEntity<List<Channel>> setupDefaultChannels() {
        List<Channel> channels = createDefaultChannels();
        return ResponseEntity.ok(channels);
    }

    private List<Channel> createDefaultChannels() {
        // ATM Channel
        Channel atmChannel = new Channel();
        atmChannel.setName("ATM Network");
        atmChannel.setChannelType("ATM");
        atmChannel.setStatus("ACTIVE");
        atmChannel.setMaxTransactionLimit(new java.math.BigDecimal("500000"));
        atmChannel.setDailyLimit(new java.math.BigDecimal("2000000"));
        atmChannel.setConfiguration("{\"timeout_seconds\": 30, \"retry_attempts\": 3}");
        
        // Mobile Channel
        Channel mobileChannel = new Channel();
        mobileChannel.setName("Mobile App");
        mobileChannel.setChannelType("MOBILE");
        mobileChannel.setStatus("ACTIVE");
        mobileChannel.setMaxTransactionLimit(new java.math.BigDecimal("1000000"));
        mobileChannel.setDailyLimit(new java.math.BigDecimal("5000000"));
        mobileChannel.setConfiguration("{\"require_biometric\": true, \"push_notifications\": true}");
        
        // Internet Banking Channel
        Channel webChannel = new Channel();
        webChannel.setName("Internet Banking");
        webChannel.setChannelType("INTERNET_BANKING");
        webChannel.setStatus("ACTIVE");
        webChannel.setMaxTransactionLimit(new java.math.BigDecimal("2000000"));
        webChannel.setDailyLimit(new java.math.BigDecimal("10000000"));
        webChannel.setConfiguration("{\"require_2fa\": true, \"session_timeout\": 1800}");
        
        // API Channel
        Channel apiChannel = new Channel();
        apiChannel.setName("API Integration");
        apiChannel.setChannelType("API");
        apiChannel.setStatus("ACTIVE");
        apiChannel.setMaxTransactionLimit(new java.math.BigDecimal("5000000"));
        apiChannel.setDailyLimit(new java.math.BigDecimal("50000000"));
        apiChannel.setConfiguration("{\"rate_limit_per_minute\": 1000, \"require_webhook\": false}");
        
        // Branch Channel
        Channel branchChannel = new Channel();
        branchChannel.setName("Branch Banking");
        branchChannel.setChannelType("BRANCH");
        branchChannel.setStatus("ACTIVE");
        branchChannel.setMaxTransactionLimit(new java.math.BigDecimal("10000000"));
        branchChannel.setDailyLimit(new java.math.BigDecimal("100000000"));
        branchChannel.setConfiguration("{\"require_id_verification\": true, \"teller_approval_required\": true}");
        
        return channelRepository.saveAll(List.of(atmChannel, mobileChannel, webChannel, apiChannel, branchChannel));
    }
}
