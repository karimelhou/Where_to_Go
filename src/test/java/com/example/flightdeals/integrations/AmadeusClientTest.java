package com.example.flightdeals.integrations;

import com.example.flightdeals.dto.DealCandidate;
import com.example.flightdeals.integrations.amadeus.AmadeusClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AmadeusClientTest {

    @Test
    void validatesOffer() throws IOException {
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setHeader("Content-Type", "application/json")
                .setBody("{\"access_token\":\"token\",\"expires_in\":3600}"));
        server.enqueue(new MockResponse().setHeader("Content-Type", "application/json")
                .setBody("{\"data\":[{\"type\":\"flight-offer\"}]}"));
        server.start();
        try {
            WebClient client = WebClient.builder().baseUrl(server.url("/").toString()).build();
            AmadeusClient amadeusClient = new AmadeusClient(client, "id", "secret");
            DealCandidate candidate = new DealCandidate();
            candidate.setOrigin("LYS");
            candidate.setDestination("LIS");
            candidate.setCurrency("EUR");
            candidate.setPrice(BigDecimal.valueOf(120));
            candidate.setDepartAt(Instant.parse("2024-05-01T00:00:00Z"));
            boolean validated = amadeusClient.validateOffer(candidate);
            assertThat(validated).isTrue();
        } finally {
            server.shutdown();
        }
    }

    @Test
    void mapsDestinationsToCandidates() throws IOException {
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setHeader("Content-Type", "application/json")
                .setBody("{\"access_token\":\"token\",\"expires_in\":3600}"));
        server.enqueue(new MockResponse().setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "data": [
                            {
                              "origin": "LYS",
                              "destination": "AMS",
                              "departureDate": "2024-06-01",
                              "returnDate": "2024-06-07",
                              "price": { "total": "199.50", "currency": "EUR" },
                              "links": { "flightOffers": "https://amadeus.example/offer" }
                            }
                          ]
                        }
                        """));
        server.start();
        try {
            WebClient client = WebClient.builder().baseUrl(server.url("/").toString()).build();
            AmadeusClient amadeusClient = new AmadeusClient(client, "id", "secret");
            assertThat(amadeusClient.searchPopularDestinations("LYS"))
                    .singleElement()
                    .satisfies(candidate -> {
                        assertThat(candidate.getDestination()).isEqualTo("AMS");
                        assertThat(candidate.getPrice()).isEqualByComparingTo("199.50");
                        assertThat(candidate.isValidatedByAmadeus()).isTrue();
                    });
        } finally {
            server.shutdown();
        }
    }
}
