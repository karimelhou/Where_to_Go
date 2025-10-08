package com.example.flightdeals.service;

import com.example.flightdeals.config.AppProperties;
import com.example.flightdeals.domain.PriceSample;
import com.example.flightdeals.dto.DealCandidate;
import com.example.flightdeals.dto.DealEvaluation;
import com.example.flightdeals.repository.PriceSampleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class DealDetectorTest {

    private PriceSampleRepository priceSampleRepository;
    private AirportService airportService;
    private DealDetector dealDetector;

    @BeforeEach
    void setUp() {
        priceSampleRepository = Mockito.mock(PriceSampleRepository.class);
        airportService = Mockito.mock(AirportService.class);
        AppProperties properties = new AppProperties();
        properties.getThresholds().setMinScore(60);
        properties.getThresholds().setErrorDiscount(0.3);
        properties.getThresholds().setErrorPricePerKm(0.015);
        properties.getThresholds().setUltraLowLongHaul(120);
        dealDetector = new DealDetector(priceSampleRepository, airportService, properties);
    }

    @Test
    void detectsHighScoreForLargeDiscount() {
        PriceSample sample = new PriceSample();
        sample.setPrice(BigDecimal.valueOf(200));
        when(priceSampleRepository.findRecentSamples(any(), any(), any())).thenReturn(List.of(sample));
        when(airportService.distanceKm("LYS", "LIS")).thenReturn(1500.0);

        DealCandidate candidate = new DealCandidate();
        candidate.setOrigin("LYS");
        candidate.setDestination("LIS");
        candidate.setPrice(BigDecimal.valueOf(80));
        candidate.setCurrency("EUR");
        candidate.setDepartAt(Instant.now().plusSeconds(86400));
        candidate.setNonstop(true);

        DealEvaluation evaluation = dealDetector.evaluate(candidate);
        assertThat(evaluation.getScore()).isGreaterThan(40);
        assertThat(evaluation.isErrorFare()).isTrue();
    }
}
