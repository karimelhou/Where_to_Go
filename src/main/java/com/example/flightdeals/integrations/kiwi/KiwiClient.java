package com.example.flightdeals.integrations.kiwi;

import com.example.flightdeals.domain.Deal;
import com.example.flightdeals.dto.DealCandidate;
import com.example.flightdeals.integrations.FlightProviderClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class KiwiClient implements FlightProviderClient {

    private static final Logger log = LoggerFactory.getLogger(KiwiClient.class);

    private final WebClient webClient;

    public KiwiClient(WebClient kiwiWebClient) {
        this.webClient = kiwiWebClient;
    }

    @Override
    public List<DealCandidate> search(String origin, Instant dateFrom, Instant dateTo, boolean nonstopOnly, Integer maxPrice) {
        LocalDate from = dateFrom.atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate to = dateTo.atZone(ZoneOffset.UTC).toLocalDate();
        try {
            KiwiResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/v2/search")
                            .queryParam("fly_from", origin)
                            .queryParam("date_from", from)
                            .queryParam("date_to", to)
                            .queryParam("curr", "EUR")
                            .queryParam("limit", 200)
                            .queryParam("max_stopovers", nonstopOnly ? 0 : 2)
                            .queryParamIfPresent("price_to", java.util.Optional.ofNullable(maxPrice))
                            .build())
                    .retrieve()
                    .onStatus(status -> status.isError(), this::handleError)
                    .bodyToMono(KiwiResponse.class)
                    .retryWhen(reactor.util.retry.Retry.backoff(2, java.time.Duration.ofSeconds(2)))
                    .block();
            if (response == null || response.data() == null) {
                return List.of();
            }
            List<DealCandidate> results = new ArrayList<>();
            for (KiwiResponse.FlightData flight : response.data()) {
                DealCandidate candidate = new DealCandidate();
                candidate.setOrigin(flight.cityFrom());
                candidate.setDestination(flight.cityTo());
                candidate.setPrice(BigDecimal.valueOf(flight.price()));
                candidate.setCurrency(response.currency());
                candidate.setDepartAt(Instant.ofEpochSecond(flight.dTimeUTC()));
                candidate.setReturnAt(flight.returnRoute() != null && !flight.returnRoute().isEmpty()
                        ? Instant.ofEpochSecond(flight.returnRoute().get(0).dTimeUTC()) : null);
                candidate.setDeepLink(flight.deepLink());
                candidate.setSource(Deal.Source.KIWI);
                candidate.setNonstop(flight.route() != null && flight.route().size() <= 1);
                candidate.setRawPayload(flight.raw());
                results.add(candidate);
            }
            return results;
        } catch (Exception ex) {
            log.warn("Kiwi search failed for origin {}: {}", origin, ex.getMessage());
            return List.of();
        }
    }

    private Mono<? extends Throwable> handleError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .defaultIfEmpty("")
                .flatMap(body -> {
                    log.error("Kiwi API error: status={} body={}", response.statusCode(), body);
                    return Mono.error(new IllegalStateException("Kiwi API error %s".formatted(response.statusCode())));
                });
    }
}
