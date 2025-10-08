package com.example.flightdeals.service;

import com.example.flightdeals.config.AppProperties;
import com.example.flightdeals.domain.Deal;
import com.example.flightdeals.dto.DealCandidate;
import com.example.flightdeals.dto.DealEvaluation;
import com.example.flightdeals.repository.DealRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class DealIngestionService {

    private final DealDetector dealDetector;
    private final DealRepository dealRepository;
    private final AppProperties properties;

    public DealIngestionService(DealDetector dealDetector,
                                DealRepository dealRepository,
                                AppProperties properties) {
        this.dealDetector = dealDetector;
        this.dealRepository = dealRepository;
        this.properties = properties;
    }

    @Transactional
    public Optional<Deal> ingest(DealCandidate candidate) {
        DealEvaluation evaluation = dealDetector.evaluate(candidate);
        if (evaluation.getScore() < properties.getThresholds().getMinScore()) {
            dealDetector.recordSample(candidate);
            return Optional.empty();
        }
        Instant duplicateSince = Instant.now().minus(12, ChronoUnit.HOURS);
        Optional<Deal> duplicate = dealRepository.findRecentDuplicate(candidate.getOrigin(), candidate.getDestination(),
                candidate.getSource(), candidate.getPrice(), candidate.getDepartAt(), candidate.getReturnAt(), duplicateSince);
        if (duplicate.isPresent()) {
            return duplicate;
        }
        Deal deal = new Deal();
        deal.setOrigin(candidate.getOrigin());
        deal.setDestination(candidate.getDestination());
        deal.setPrice(candidate.getPrice());
        deal.setCurrency(candidate.getCurrency());
        deal.setDepartAt(candidate.getDepartAt());
        deal.setReturnAt(candidate.getReturnAt());
        deal.setDeepLink(candidate.getDeepLink());
        deal.setSource(candidate.getSource());
        deal.setErrorFare(evaluation.isErrorFare());
        deal.setScore(evaluation.getScore());
        deal.setFoundAt(Instant.now());
        deal.setRawPayload(candidate.getRawPayload());
        deal.setPricePerKm(Double.isNaN(evaluation.getPricePerKm()) ? null : evaluation.getPricePerKm());
        Deal saved = dealRepository.save(deal);
        dealDetector.recordSample(candidate);
        return Optional.of(saved);
    }
}
