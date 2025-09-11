package com.xypay.xypay.service;

// Note: KafkaTemplate is only required if Kafka is configured. If not using Kafka, this bean is optional.
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class KafkaEventService {
    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;

    public void publishEvent(String topic, String key, String value) {
        if (kafkaTemplate != null) {
            kafkaTemplate.send(topic, key, value);
        }
    }
}
