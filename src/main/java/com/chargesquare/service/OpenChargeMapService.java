package com.chargesquare.service;

import com.chargesquare.model.ChargingStation;
import com.chargesquare.model.ChargingStationMessage;
import com.chargesquare.config.KafkaConfig;
import com.fasterxml.uuid.Generators;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.core.ParameterizedTypeReference;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
public class OpenChargeMapService {

    private WebClient webClient;
    private final KafkaTemplate<String, ChargingStationMessage> kafkaTemplate;
    
    @Value("${opencharge.api.key}")
    private String apiKey;
    
    @Value("${opencharge.api.base-url}")
    private String baseUrl;
    
    public OpenChargeMapService(KafkaTemplate<String, ChargingStationMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    @PostConstruct
    public void init() {
        log.info("Initializing OpenChargeMapService with baseUrl: {} and apiKey length: {}", 
                baseUrl, apiKey != null ? apiKey.length() : 0);
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-API-Key", apiKey)
                .build();
    }
    
    @Scheduled(fixedRateString = "${opencharge.api.fetch-interval}")
    public void fetchAndPublishStations() {
        log.info("Starting scheduled fetch of charging stations");
        fetchStations()
            .doOnNext(data -> log.info("Received station data: {}", data.get("ID")))
            .flatMap(this::convertToChargingStation)
            .doOnNext(station -> log.info("Converted to station: {}", station.getId()))
            .map(this::createMessage)
            .doOnNext(message -> log.info("Created message with ID: {}", message.getMessageId()))
            .flatMap(this::publishToKafka)
            .doOnComplete(() -> log.info("Completed processing batch"))
            .doOnError(error -> log.error("Error processing stations", error))
            .subscribe();
    }
    
    private Flux<Map<String, Object>> fetchStations() {
        return webClient.get()
                .uri("/poi?maxresults=100&compact=true&verbose=false&countrycode=TR")
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {});
    }
    
    private Mono<ChargingStation> convertToChargingStation(Map<String, Object> data) {
        return Mono.just(ChargingStation.builder()
                .id(Long.valueOf(data.get("ID").toString()))
                .name(extractNestedValue(data, "AddressInfo.Title"))
                .location(extractNestedValue(data, "AddressInfo.AddressLine1"))
                .status("true".equalsIgnoreCase(extractNestedValue(data, "StatusType.IsOperational")) ? 
                       ChargingStation.StationStatus.AVAILABLE : ChargingStation.StationStatus.MAINTENANCE)
                .powerOutput(extractPowerOutput(data))
                .connectorType(extractConnectorType(data))
                .build());
    }
    
    private ChargingStationMessage createMessage(ChargingStation station) {
        return ChargingStationMessage.builder()
                .messageId(Generators.timeBasedGenerator().generate().toString())
                .timestamp(Instant.now())
                .source("OPEN_CHARGE_MAP")
                .operation("CREATE")
                .payload(station)
                .build();
    }
    
    private Mono<Void> publishToKafka(ChargingStationMessage message) {
        log.info("Attempting to publish message to Kafka topic: {}, messageId: {}", KafkaConfig.TOPIC_NAME, message.getMessageId());
        return Mono.fromFuture(kafkaTemplate.send(KafkaConfig.TOPIC_NAME, message.getMessageId(), message)
                .toCompletableFuture())
                .doOnSuccess(result -> log.info("Successfully published message to Kafka: {}, offset: {}", 
                    message.getMessageId(), result.getRecordMetadata().offset()))
                .doOnError(error -> log.error("Failed to publish message to Kafka: {}, error: {}", 
                    message.getMessageId(), error.getMessage(), error))
                .then();
    }
    
    @SuppressWarnings("unchecked")
    private String extractNestedValue(Map<String, Object> data, String path) {
        String[] parts = path.split("\\.");
        Object current = data;
        
        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(part);
            } else {
                return null;
            }
        }
        
        return current != null ? current.toString() : null;
    }
    
    @SuppressWarnings("unchecked")
    private Double extractPowerOutput(Map<String, Object> data) {
        try {
            var connections = (java.util.List<Map<String, Object>>) data.get("Connections");
            if (connections != null && !connections.isEmpty()) {
                var powerKW = connections.get(0).get("PowerKW");
                return powerKW != null ? Double.valueOf(powerKW.toString()) : 0.0;
            }
        } catch (Exception e) {
            // Log error and return default
        }
        return 0.0;
    }
    
    @SuppressWarnings("unchecked")
    private String extractConnectorType(Map<String, Object> data) {
        try {
            var connections = (java.util.List<Map<String, Object>>) data.get("Connections");
            if (connections != null && !connections.isEmpty()) {
                var connectionType = (Map<String, Object>) connections.get(0).get("ConnectionType");
                return connectionType != null ? connectionType.get("Title").toString() : "Unknown";
            }
        } catch (Exception e) {
            // Log error and return default
        }
        return "Unknown";
    }
} 