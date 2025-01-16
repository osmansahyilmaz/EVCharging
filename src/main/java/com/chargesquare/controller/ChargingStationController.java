package com.chargesquare.controller;

import com.chargesquare.model.ChargingStation;
import com.chargesquare.repository.ChargingStationRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/stations")
public class ChargingStationController {

    private final ChargingStationRepository repository;

    @Autowired
    public ChargingStationController(ChargingStationRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Cacheable(value = "stations", key = "'all'")
    public Flux<ChargingStation> getAllStations() {
        return Flux.defer(() -> Flux.fromIterable(repository.findAll()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ChargingStation>> getStationById(@PathVariable Long id) {
        return Mono.defer(() -> 
            Mono.justOrEmpty(repository.findById(id))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build()))
            .subscribeOn(Schedulers.boundedElastic())
            .cache();
    }

    @PostMapping
    @CacheEvict(value = "stations", allEntries = true)
    public Mono<ResponseEntity<ChargingStation>> createStation(@Valid @RequestBody ChargingStation station) {
        return Mono.defer(() -> Mono.just(repository.save(station)))
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved))
                .subscribeOn(Schedulers.boundedElastic());
    }
} 