package com.chargesquare.config;

import com.chargesquare.model.ChargingStationMessage;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {
    
    public static final String TOPIC_NAME = "charging-stations-data";
    
    @Bean
    public NewTopic chargingStationsTopic() {
        return TopicBuilder.name(TOPIC_NAME)
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public KafkaTemplate<String, ChargingStationMessage> kafkaTemplate(
            ProducerFactory<String, ChargingStationMessage> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
} 