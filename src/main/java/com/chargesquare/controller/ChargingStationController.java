package com.chargesquare.controller;

import com.chargesquare.model.ChargingStation;
import com.chargesquare.repository.ChargingStationRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@RestController
@RequestMapping("/stations")
public class ChargingStationController {

    private final ChargingStationRepository repository;

    @Autowired
    public ChargingStationController(ChargingStationRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<ChargingStation> getAllStations() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChargingStation> getStationById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ChargingStation> createStation(@Valid @RequestBody ChargingStation station) {
        ChargingStation savedStation = repository.save(station);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedStation);
    }
} 