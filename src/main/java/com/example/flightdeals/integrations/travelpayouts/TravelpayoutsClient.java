package com.example.flightdeals.integrations.travelpayouts;

import com.example.flightdeals.domain.Deal;
import com.example.flightdeals.dto.DealCandidate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class TravelpayoutsClient {

    private static final Logger log = LoggerFactory.getLogger(TravelpayoutsClient.class);

    private final WebClient webClient;
    private final String token;

    public TravelpayoutsClient(WebClient travelpayoutsWebClient,
                               @Value("${integrations.travelpayouts.token}") String token) {
        this.webClient = travelpayoutsWebClient;
        this.token = token;
    }

    public List<DealCandidate> fetchRecentDeals(String origin) {
        List<DealCandidate> latest = fetchFromEndpoint("/v2/prices/latest", origin);
        List<DealCandidate> specials = fetchFromEndpoint("/v2/prices/special-offers", origin);
        List<DealCandidate> combined = new ArrayList<>(latest.size() + specials.size());
        combined.addAll(latest);
        combined.addAll(specials);
        return combined;
    }

    private List<DealCandidate> fetchFromEndpoint(String path, String origin) {
        try {
            TravelpayoutsResponse response = webClient.get()
                    .uri(builder -> builder.path(path)
                            .queryParam("origin", origin)
                            .queryParam("currency", "EUR")
                            .queryParam("token", token)
                            .build())
                    .retrieve()
                    .onStatus(status -> status.isError(), this::handleError)
                    .bodyToMono(TravelpayoutsResponse.class)
                    .retryWhen(reactor.util.retry.Retry.backoff(2, java.time.Duration.ofSeconds(2)))
                    .block();
            if (response == null || response.data() == null) {
                return Collections.emptyList();
            }
            List<DealCandidate> candidates = new ArrayList<>();
            for (TravelpayoutsResponse.Item item : response.data()) {
                if (item.destination() == null) {
                    continue;
                }
                DealCandidate candidate = new DealCandidate();
                candidate.setOrigin(item.origin() != null ? item.origin() : origin);
                candidate.setDestination(item.destination());
                candidate.setPrice(BigDecimal.valueOf(item.price()));
                candidate.setCurrency(item.currency() != null ? item.currency() : "EUR");
                candidate.setDepartAt(parseDateTime(item.departureAt(), item.departDate()));
                candidate.setReturnAt(parseDateTime(item.returnAt(), item.returnDate()));
                candidate.setDeepLink(item.link());
                candidate.setSource(Deal.Source.TRAVELPAYOUTS);
                candidate.setNonstop(item.transfers() != null && item.transfers() == 0);
                candidate.setRawPayload(item.toString());
                candidates.add(candidate);
            }
            return candidates;
        } catch (Exception ex) {
            log.warn("Travelpayouts fetch failed for {} via {}: {}", origin, path, ex.getMessage());
            return Collections.emptyList();
        }
    }

    private Instant parseDateTime(String isoDateTime, String isoDate) {
        if (isoDateTime != null && !isoDateTime.isBlank()) {
            try {
                return OffsetDateTime.parse(isoDateTime).toInstant();
            } catch (Exception ignored) {
                // fall through to date parsing
            }
        }
        if (isoDate != null && !isoDate.isBlank()) {
            try {
                return LocalDate.parse(isoDate).atStartOfDay().toInstant(ZoneOffset.UTC);
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private Mono<? extends Throwable> handleError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .defaultIfEmpty("")
                .flatMap(body -> {
                    log.error("Travelpayouts API error: status={} body={}", response.statusCode(), body);
                    return Mono.error(new IllegalStateException("Travelpayouts API error %s".formatted(response.statusCode())));
                });
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record TravelpayoutsResponse(List<Item> data) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        private record Item(
                String origin,
                String destination,
                double price,
                String currency,
                @JsonProperty("link") String link,
                @JsonProperty("departure_at") String departureAt,
                @JsonProperty("return_at") String returnAt,
                @JsonProperty("depart_date") String departDate,
                @JsonProperty("return_date") String returnDate,
                @JsonProperty("transfers") Integer transfers
        ) {
        }
    }
}
