package com.example.flightdeals.service;

import com.example.flightdeals.dto.DealCandidate;
import com.example.flightdeals.integrations.amadeus.AmadeusClient;
import com.example.flightdeals.integrations.travelpayouts.TravelpayoutsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FetchService {

    private static final Logger log = LoggerFactory.getLogger(FetchService.class);

    private final TravelpayoutsClient travelpayoutsClient;
    private final AmadeusClient amadeusClient;
    private final DealIngestionService ingestionService;

    public FetchService(TravelpayoutsClient travelpayoutsClient,
                        AmadeusClient amadeusClient,
                        DealIngestionService ingestionService) {
        this.travelpayoutsClient = travelpayoutsClient;
        this.amadeusClient = amadeusClient;
        this.ingestionService = ingestionService;
    }

    public FetchResult fetchOrigin(String origin, boolean nonstopOnly, Integer maxBudget) {
        List<DealCandidate> candidates = new ArrayList<>();
        List<DealCandidate> travelpayoutsDeals = travelpayoutsClient.fetchRecentDeals(origin);
        int validated = 0;
        for (DealCandidate candidate : travelpayoutsDeals) {
            boolean validatedByAmadeus = amadeusClient.validateOffer(candidate);
            candidate.setValidatedByAmadeus(validatedByAmadeus);
            if (validatedByAmadeus) {
                validated++;
            }
            candidates.add(candidate);
        }

        List<DealCandidate> amadeusDeals = amadeusClient.searchPopularDestinations(origin);
        for (DealCandidate amadeusDeal : amadeusDeals) {
            if (amadeusDeal.getPrice() == null) {
                continue;
            }
            candidates.add(amadeusDeal);
            if (amadeusDeal.isValidatedByAmadeus()) {
                validated++;
            }
        }

        int stored = 0;
        for (DealCandidate candidate : candidates) {
            if (maxBudget != null && candidate.getPrice().intValue() > maxBudget) {
                continue;
            }
            if (nonstopOnly && !candidate.isNonstop()) {
                continue;
            }
            if (candidate.getCurrency() == null) {
                candidate.setCurrency("EUR");
            }
            stored += ingestionService.ingest(candidate).map(d -> 1).orElse(0);
        }
        log.info("Fetched {} candidates for {} (validated: {}, stored: {})", candidates.size(), origin, validated, stored);
        return new FetchResult(candidates.size(), validated, stored);
    }

    public record FetchResult(int fetched, int validated, int stored) {
    }
}
