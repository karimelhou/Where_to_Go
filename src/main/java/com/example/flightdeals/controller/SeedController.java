package com.example.flightdeals.controller;

import com.example.flightdeals.domain.Deal;
import com.example.flightdeals.repository.DealRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/test")
@Profile("dev")
public class SeedController {

    private final DealRepository dealRepository;

    public SeedController(DealRepository dealRepository) {
        this.dealRepository = dealRepository;
    }

    @PostMapping("/seed")
    public ResponseEntity<?> seed() {
        Deal deal = new Deal();
        deal.setOrigin("LYS");
        deal.setDestination("LIS");
        deal.setPrice(BigDecimal.valueOf(89));
        deal.setCurrency("EUR");
        deal.setDepartAt(Instant.now().plusSeconds(86400));
        deal.setReturnAt(Instant.now().plusSeconds(86400 * 5));
        deal.setDeepLink("https://example.com/deal");
        deal.setSource(Deal.Source.TRAVELPAYOUTS);
        deal.setErrorFare(false);
        deal.setScore(75);
        deal.setFoundAt(Instant.now());
        deal.setValidatedByAmadeus(false);
        dealRepository.save(deal);
        return ResponseEntity.ok().build();
    }
}
