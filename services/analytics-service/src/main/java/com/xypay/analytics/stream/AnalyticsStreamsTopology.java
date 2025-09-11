package com.xypay.analytics.stream;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class AnalyticsStreamsTopology {

    @Bean
    public KStream<String, String> fraudAnomalyStream(StreamsBuilder builder) {
        KStream<String, String> txEvents = builder.stream("transaction-events", Consumed.with(org.apache.kafka.common.serialization.Serdes.String(), org.apache.kafka.common.serialization.Serdes.String()));

        KStream<String, String> alerts = txEvents
                .filter((key, value) -> value != null)
                .mapValues(this::scoreAndDetect);

        alerts.filter((k, v) -> v != null && !v.isBlank())
                .to("analytics-alerts", Produced.with(org.apache.kafka.common.serialization.Serdes.String(), org.apache.kafka.common.serialization.Serdes.String()));

        return txEvents;
    }

    private String scoreAndDetect(String json) {
        try {
            // Simple heuristic: flag transactions > 10000 as high-risk
            if (json.contains("\"amount\":")) {
                String amtStr = json.replaceAll(".*\"amount\":\\s*([0-9.]+).*", "$1");
                double amount = Double.parseDouble(amtStr);
                if (amount > 10000) {
                    return "{\"type\":\"HIGH_VALUE_TX\",\"risk\":\"HIGH\",\"amount\":" + amount + "}";
                }
            }
        } catch (Exception e) {
            log.debug("Stream scoring parse error: {}", e.getMessage());
        }
        return null;
    }
}


