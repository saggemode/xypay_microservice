package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "channel_transactions")
public class ChannelTransaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private Channel channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @Column(name = "channel_reference")
    private String channelReference;

    @Column(name = "external_reference")
    private String externalReference;

    @Column(name = "request_payload")
    @Lob
    private String requestPayload;

    @Column(name = "response_payload")
    @Lob
    private String responsePayload;

    @Column(name = "status")
    private String status; // PENDING, COMPLETED, FAILED, TIMEOUT

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    
}
