package com.example.flightdeals.integrations.travelpayouts;

import com.example.flightdeals.domain.Deal;
import com.example.flightdeals.dto.DealCandidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Component
public class TravelpayoutsClient {

    private static final Logger log = LoggerFactory.getLogger(TravelpayoutsClient.class);

    private final WebClient webClient;

    public TravelpayoutsClient(WebClient travelpayoutsWebClient) {
        this.webClient = travelpayoutsWebClient;
    }

    public List<DealCandidate> fetchRecentDeals(String origin) {
        try {
            TravelpayoutsResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/v1/prices/cheap")
                            .queryParam("origin", origin)
                            .queryParam("limit", 200)
                            .queryParam("page", 1)
                            .build())
                    .retrieve()
                    .onStatus(status -> status.isError(), this::handleError)
                    .bodyToMono(TravelpayoutsResponse.class)
                    .retryWhen(reactor.util.retry.Retry.backoff(2, java.time.Duration.ofSeconds(2)))
                    .block();
            if (response == null || response.data() == null) {
                return List.of();
            }
            List<DealCandidate> candidates = new ArrayList<>();
            response.data().forEach((destination, data) -> data.forEach(item -> {
                DealCandidate candidate = new DealCandidate();
                candidate.setOrigin(origin);
                candidate.setDestination(destination);
                candidate.setPrice(BigDecimal.valueOf(item.price()));
                candidate.setCurrency(item.currency());
                candidate.setDepartAt(parseDate(item.departDate()));
                candidate.setReturnAt(parseDate(item.returnDate()));
                candidate.setDeepLink(item.link());
                candidate.setSource(Deal.Source.TRAVELPAYOUTS);
                candidate.setNonstop(item.transfers() == 0);
                candidate.setRawPayload(item.toString());
                candidates.add(candidate);
            }));
            return candidates;
        } catch (Exception ex) {
            log.warn("Travelpayouts fetch failed for {}: {}", origin, ex.getMessage());
            return List.of();
        }
    }

    private Instant parseDate(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(date).toInstant(ZoneOffset.UTC);
    }

    private Mono<? extends Throwable> handleError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .defaultIfEmpty("")
                .flatMap(body -> {
                    log.error("Travelpayouts API error: status={} body={}", response.statusCode(), body);
                    return Mono.error(new IllegalStateException("Travelpayouts API error %s".formatted(response.statusCode())));
                });
    }
}
