package com.xypay.xypay.controller;

import com.xypay.xypay.domain.Channel;
import com.xypay.xypay.service.ChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/channels")
public class ChannelController {
    
    @Autowired
    private ChannelService channelService;
    
    @PostMapping
    public ResponseEntity<Channel> createChannel(
            @RequestParam String name,
            @RequestParam String channelType,
            @RequestParam String branchId) {
        
        Channel channel = channelService.createChannel(name, channelType, branchId);
        return ResponseEntity.ok(channel);
    }
    
    @GetMapping("/{channelId}")
    public ResponseEntity<Channel> getChannel(@PathVariable UUID channelId) {
        Channel channel = channelService.getChannel(channelId);
        return ResponseEntity.ok(channel);
    }
    
    @PostMapping("/{channelId}/status")
    public ResponseEntity<Channel> updateChannelStatus(
            @PathVariable UUID channelId,
            @RequestParam String status) {
        
        Channel channel = channelService.updateChannelStatus(channelId, status);
        return ResponseEntity.ok(channel);
    }
}