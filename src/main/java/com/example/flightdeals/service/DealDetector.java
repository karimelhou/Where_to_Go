package com.example.flightdeals.service;

import com.example.flightdeals.config.AppProperties;
import com.example.flightdeals.domain.PriceSample;
import com.example.flightdeals.dto.DealCandidate;
import com.example.flightdeals.dto.DealEvaluation;
import com.example.flightdeals.repository.PriceSampleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class DealDetector {

    private final PriceSampleRepository priceSampleRepository;
    private final AirportService airportService;
    private final AppProperties properties;

    public DealDetector(PriceSampleRepository priceSampleRepository,
                        AirportService airportService,
                        AppProperties properties) {
        this.priceSampleRepository = priceSampleRepository;
        this.airportService = airportService;
        this.properties = properties;
    }

    public DealEvaluation evaluate(DealCandidate candidate) {
        double medianPrice = medianPrice(candidate.getOrigin(), candidate.getDestination());
        double price = candidate.getPrice().doubleValue();
        double discountRatio = medianPrice > 0 ? price / medianPrice : 0.6;
        double distance = airportService.distanceKm(candidate.getOrigin(), candidate.getDestination());
        double pricePerKm = Double.isNaN(distance) || distance == 0 ? Double.NaN : price / distance;

        double baseScore = 100 * Math.max(0, 1 - discountRatio);
        if (Double.isNaN(pricePerKm)) {
            pricePerKm = price;
        }
        double perKmScore = Math.max(0, 1 - (pricePerKm / Math.max(properties.getThresholds().getErrorPricePerKm() * 4, 1e-6))) * 20;
        double nonstopBonus = candidate.isNonstop() ? 5 : 0;
        double recencyBoost = computeRecencyBoost(candidate.getDepartAt());

        double score = baseScore * 0.5 + perKmScore + nonstopBonus + recencyBoost;
        boolean errorFare = discountRatio <= properties.getThresholds().getErrorDiscount()
                || pricePerKm < properties.getThresholds().getErrorPricePerKm()
                || (properties.getThresholds().getUltraLowLongHaul() > 0
                && price < properties.getThresholds().getUltraLowLongHaul());

        return new DealEvaluation(round(score), errorFare, discountRatio, pricePerKm);
    }

    public void recordSample(DealCandidate candidate) {
        PriceSample sample = new PriceSample();
        sample.setOrigin(candidate.getOrigin());
        sample.setDestination(candidate.getDestination());
        sample.setPrice(candidate.getPrice());
        sample.setCurrency(candidate.getCurrency());
        sample.setCollectedAt(Instant.now());
        priceSampleRepository.save(sample);
    }

    private double medianPrice(String origin, String destination) {
        Instant since = Instant.now().minus(30, ChronoUnit.DAYS);
        List<PriceSample> samples = priceSampleRepository.findRecentSamples(origin, destination, since);
        if (samples.isEmpty()) {
            return 0;
        }
        List<Double> sorted = samples.stream()
                .map(PriceSample::getPrice)
                .map(BigDecimal::doubleValue)
                .sorted()
                .toList();
        int size = sorted.size();
        if (size % 2 == 1) {
            return sorted.get(size / 2);
        }
        return (sorted.get(size / 2 - 1) + sorted.get(size / 2)) / 2.0;
    }

    private double computeRecencyBoost(Instant departAt) {
        if (departAt == null) {
            return 0;
        }
        long days = Instant.now().until(departAt, ChronoUnit.DAYS);
        if (days < 0) {
            return 0;
        }
        return Math.max(0, 10 - days * 0.5);
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
