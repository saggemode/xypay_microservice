package com.xypay.xypay.service;

import com.xypay.xypay.domain.Channel;
import com.xypay.xypay.repository.ChannelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChannelService {
    
    @Autowired
    private ChannelRepository channelRepository;
    
    public Channel createChannel(String name, String channelType, String branchId) {
        Channel channel = new Channel();
        channel.setName(name);
        channel.setChannelType(channelType);
        channel.setBranchId(branchId);
        channel.setStatus("ACTIVE");
        channel.setCreatedAt(LocalDateTime.now());
        
        return channelRepository.save(channel);
    }
    
    public Channel getChannel(Long channelId) {
        return channelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found"));
    }
    
    public List<Channel> getChannelsByBranch(String branchId) {
        return channelRepository.findByBranchId(branchId);
    }
    
    public Channel updateChannelStatus(Long channelId, String status) {
        Channel channel = getChannel(channelId);
        channel.setStatus(status);
        return channelRepository.save(channel);
    }
}