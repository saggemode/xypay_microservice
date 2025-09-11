package com.xypay.xypay.repository;

import com.xypay.xypay.domain.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {
    List<Channel> findByBranchId(String branchId);
    List<Channel> findByChannelTypeAndIsActiveTrue(String channelType);
    List<Channel> findByIsActiveTrue();
}