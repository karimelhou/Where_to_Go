package com.example.flightdeals.integrations.amadeus;

import com.example.flightdeals.domain.Deal;
import com.example.flightdeals.dto.DealCandidate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class AmadeusClient {

    private static final Logger log = LoggerFactory.getLogger(AmadeusClient.class);

    private final WebClient webClient;
    private final String clientId;
    private final String clientSecret;
    private final AtomicReference<AccessToken> tokenCache = new AtomicReference<>();

    public AmadeusClient(WebClient amadeusWebClient,
                         @Value("${integrations.amadeus.client-id}") String clientId,
                         @Value("${integrations.amadeus.client-secret}") String clientSecret) {
        this.webClient = amadeusWebClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public boolean validateOffer(DealCandidate candidate) {
        if (!StringUtils.hasText(candidate.getOrigin())
                || !StringUtils.hasText(candidate.getDestination())
                || candidate.getDepartAt() == null) {
            return false;
        }
        try {
            AccessToken token = ensureToken();
            FlightOffersResponse response = webClient.get()
                    .uri(builder -> builder.path("/v2/shopping/flight-offers")
                            .queryParam("originLocationCode", candidate.getOrigin())
                            .queryParam("destinationLocationCode", candidate.getDestination())
                            .queryParam("departureDate", candidate.getDepartAt().atZone(ZoneOffset.UTC).toLocalDate())
                            .queryParam("adults", 1)
                            .queryParamIfPresent("returnDate", Optional.ofNullable(candidate.getReturnAt()).map(date -> date.atZone(ZoneOffset.UTC).toLocalDate()))
                            .queryParam("currencyCode", candidate.getCurrency() != null ? candidate.getCurrency() : "EUR")
                            .queryParamIfPresent("maxPrice", Optional.ofNullable(candidate.getPrice()).map(price -> price.intValue()))
                            .build())
                    .headers(headers -> headers.setBearerAuth(token.value()))
                    .retrieve()
                    .bodyToMono(FlightOffersResponse.class)
                    .onErrorResume(ex -> {
                        log.debug("Amadeus validation error for {}-{}: {}", candidate.getOrigin(), candidate.getDestination(), ex.getMessage());
                        return Mono.empty();
                    })
                    .block();
            return response != null && response.hasData();
        } catch (Exception ex) {
            log.debug("Amadeus validation failed for {}-{}: {}", candidate.getOrigin(), candidate.getDestination(), ex.getMessage());
            return false;
        }
    }

    public List<DealCandidate> searchPopularDestinations(String origin) {
        if (!StringUtils.hasText(origin)) {
            return Collections.emptyList();
        }
        try {
            AccessToken token = ensureToken();
            FlightDestinationsResponse response = webClient.get()
                    .uri(builder -> builder.path("/v1/shopping/flight-destinations")
                            .queryParam("origin", origin)
                            .queryParam("currency", "EUR")
                            .build())
                    .headers(headers -> headers.setBearerAuth(token.value()))
                    .retrieve()
                    .bodyToMono(FlightDestinationsResponse.class)
                    .onErrorResume(ex -> {
                        log.debug("Amadeus destination search error for {}: {}", origin, ex.getMessage());
                        return Mono.empty();
                    })
                    .block();
            if (response == null || response.data() == null) {
                return Collections.emptyList();
            }
            List<DealCandidate> candidates = new ArrayList<>();
            for (FlightDestinationsResponse.Destination item : response.data()) {
                if (!StringUtils.hasText(item.destination()) || item.price() == null) {
                    continue;
                }
                DealCandidate candidate = new DealCandidate();
                candidate.setOrigin(item.origin());
                candidate.setDestination(item.destination());
                candidate.setPrice(parsePrice(item.price().total()));
                candidate.setCurrency(item.price().currency());
                candidate.setDepartAt(parseDate(item.departureDate()));
                candidate.setReturnAt(parseDate(item.returnDate()));
                candidate.setDeepLink(item.links() != null ? item.links().flightOffers() : null);
                candidate.setSource(Deal.Source.AMADEUS);
                candidate.setNonstop(true);
                candidate.setValidatedByAmadeus(true);
                candidate.setRawPayload(item.toString());
                candidates.add(candidate);
            }
            return candidates;
        } catch (Exception ex) {
            log.debug("Amadeus destination fetch failed for {}: {}", origin, ex.getMessage());
            return Collections.emptyList();
        }
    }

    private BigDecimal parsePrice(String total) {
        if (!StringUtils.hasText(total)) {
            return null;
        }
        try {
            return new BigDecimal(total);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Instant parseDate(String isoDate) {
        if (!StringUtils.hasText(isoDate)) {
            return null;
        }
        try {
            return LocalDate.parse(isoDate).atStartOfDay().toInstant(ZoneOffset.UTC);
        } catch (Exception ex) {
            return null;
        }
    }

    private AccessToken ensureToken() {
        AccessToken current = tokenCache.get();
        if (current != null && current.expiresAt().isAfter(Instant.now())) {
            return current;
        }
        synchronized (tokenCache) {
            current = tokenCache.get();
            if (current != null && current.expiresAt().isAfter(Instant.now())) {
                return current;
            }
            AccessToken refreshed = fetchToken();
            tokenCache.set(refreshed);
            return refreshed;
        }
    }

    private AccessToken fetchToken() {
        TokenResponse response = webClient.post()
                .uri("/v1/security/oauth2/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret))
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .block();
        if (response == null || !StringUtils.hasText(response.accessToken())) {
            throw new IllegalStateException("Failed to obtain Amadeus access token");
        }
        int expiresIn = Objects.requireNonNullElse(response.expiresIn(), 1800);
        return new AccessToken(response.accessToken(), Instant.now().plusSeconds(Math.max(expiresIn - 30L, 60L)));
    }

    private record AccessToken(String value, Instant expiresAt) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record TokenResponse(@JsonProperty("access_token") String accessToken,
                                 @JsonProperty("expires_in") Integer expiresIn) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record FlightOffersResponse(List<Object> data) {
        boolean hasData() {
            return data != null && !data.isEmpty();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record FlightDestinationsResponse(List<Destination> data) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        private record Destination(String origin,
                                   String destination,
                                   @JsonProperty("departureDate") String departureDate,
                                   @JsonProperty("returnDate") String returnDate,
                                   Price price,
                                   Links links) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private record Price(@JsonProperty("total") String total,
                             @JsonProperty("currency") String currency) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private record Links(@JsonProperty("flightOffers") String flightOffers) {
        }
    }
}
