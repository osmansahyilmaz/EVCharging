package com.chargesquare.service;

import com.chargesquare.model.ChargingStationMessage;
import com.chargesquare.repository.ChargingStationRepository;
import com.chargesquare.config.KafkaConfig;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class KafkaMessageService {

    private final ChargingStationRepository repository;

    public KafkaMessageService(ChargingStationRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = KafkaConfig.TOPIC_NAME, groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void consumeMessage(ChargingStationMessage message) {
        try {
            log.info("Received message: {}", message.getMessageId());
            repository.save(message.getPayload());
            log.info("Successfully processed message: {}", message.getMessageId());
        } catch (Exception e) {
            log.error("Error processing message: {}", message.getMessageId(), e);
            throw e; // Let the container handle the retry
        }
    }
} 