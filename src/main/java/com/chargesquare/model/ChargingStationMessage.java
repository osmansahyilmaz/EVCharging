package com.chargesquare.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargingStationMessage {
    private String messageId;
    private Instant timestamp;
    private String source;
    private String operation;
    private ChargingStation payload;
} 