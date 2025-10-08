package com.example.flightdeals.integrations;

import com.example.flightdeals.domain.Deal;
import com.example.flightdeals.integrations.kiwi.KiwiClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KiwiClientTest {

    @Test
    void parsesResponse() throws IOException {
        MockWebServer server = new MockWebServer();
        String body = """
                {
                  "currency": "EUR",
                  "data": [
                    {
                      "cityFrom": "LYS",
                      "cityTo": "LIS",
                      "price": 79,
                      "dTimeUTC": 1700000000,
                      "route": [
                        { "dTimeUTC": 1700000000 }
                      ],
                      "returnRoute": [
                        { "dTimeUTC": 1700500000 }
                      ],
                      "deepLink": "http://deal",
                      "raw": "{}"
                    }
                  ]
                }
                """;
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(body)
        );
        server.start();
        try {
            WebClient client = WebClient.builder()
                    .baseUrl(server.url("/").toString())
                    .defaultHeader("apikey", "test")
                    .build();
            KiwiClient kiwiClient = new KiwiClient(client);
            List<com.example.flightdeals.dto.DealCandidate> candidates = kiwiClient.search(
                    "LYS",
                    Instant.now(),
                    Instant.now().plusSeconds(86400),
                    true,
                    null
            );
            assertThat(candidates).hasSize(1);
            assertThat(candidates.get(0).getSource()).isEqualTo(Deal.Source.KIWI);
            assertThat(candidates.get(0).getDestination()).isEqualTo("LIS");
        } finally {
            server.shutdown();
        }
    }
}
