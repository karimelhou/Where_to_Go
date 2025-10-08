package com.example.flightdeals.scheduler;

import com.example.flightdeals.config.AppProperties;
import com.example.flightdeals.service.FetchService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class FetchScheduler {

    private static final Logger log = LoggerFactory.getLogger(FetchScheduler.class);

    private final AppProperties properties;
    private final FetchService fetchService;

    public FetchScheduler(AppProperties properties, FetchService fetchService) {
        this.properties = properties;
        this.fetchService = fetchService;
    }

    @PostConstruct
    void logConfig() {
        log.info("Fetch scheduler initialized with cron {} and origins {}", properties.getScheduler().getCron(), properties.getFetch().getMonitoredOrigins());
    }

    @Scheduled(cron = "${app.scheduler.cron}")
    public void scheduledFetch() {
        String originsConfig = properties.getFetch().getMonitoredOrigins();
        if (originsConfig == null || originsConfig.isBlank()) {
            return;
        }
        List<String> origins = Arrays.stream(originsConfig.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        for (String origin : origins) {
            FetchService.FetchResult result = fetchService.fetchOrigin(origin, true, null);
            log.info("Scheduled fetch for {} fetched {} deals (validated: {}, stored: {})", origin, result.fetched(), result.validated(), result.stored());
        }
    }
}
