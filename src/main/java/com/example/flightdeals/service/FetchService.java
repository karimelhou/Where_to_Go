package com.example.flightdeals.service;

import com.example.flightdeals.config.AppProperties;
import com.example.flightdeals.dto.DealCandidate;
import com.example.flightdeals.integrations.kiwi.KiwiClient;
import com.example.flightdeals.integrations.travelpayouts.TravelpayoutsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class FetchService {

    private static final Logger log = LoggerFactory.getLogger(FetchService.class);

    private final KiwiClient kiwiClient;
    private final TravelpayoutsClient travelpayoutsClient;
    private final DealIngestionService ingestionService;
    private final AppProperties properties;

    public FetchService(KiwiClient kiwiClient,
                        TravelpayoutsClient travelpayoutsClient,
                        DealIngestionService ingestionService,
                        AppProperties properties) {
        this.kiwiClient = kiwiClient;
        this.travelpayoutsClient = travelpayoutsClient;
        this.ingestionService = ingestionService;
        this.properties = properties;
    }

    public FetchResult fetchOrigin(String origin, boolean nonstopOnly, Integer maxBudget) {
        Instant now = Instant.now();
        Instant to = now.plus(properties.getFetch().getSearchLookaheadDays(), ChronoUnit.DAYS);
        List<DealCandidate> candidates = new ArrayList<>();
        candidates.addAll(kiwiClient.search(origin, now, to, nonstopOnly, maxBudget));
        candidates.addAll(travelpayoutsClient.fetchRecentDeals(origin));

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
        return new FetchResult(candidates.size(), stored);
    }

    public record FetchResult(int fetched, int stored) {
    }
}
