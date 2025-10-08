package com.example.flightdeals.integrations;

import com.example.flightdeals.domain.Deal;
import com.example.flightdeals.integrations.travelpayouts.TravelpayoutsClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TravelpayoutsClientTest {

    @Test
    void parsesResponse() throws IOException {
        MockWebServer server = new MockWebServer();
        String latestBody = """
                {
                  "data": [
                    {
                      "origin": "LYS",
                      "destination": "LIS",
                      "price": 70,
                      "currency": "EUR",
                      "departure_at": "2024-05-20T00:00:00Z",
                      "return_at": "2024-05-27T00:00:00Z",
                      "transfers": 0,
                      "link": "http://deal"
                    }
                  ]
                }
                """;
        String specialsBody = """
                {
                  "data": []
                }
                """;
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(latestBody));
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(specialsBody));
        server.start();
        try {
            WebClient client = WebClient.builder()
                    .baseUrl(server.url("/").toString())
                    .build();
            TravelpayoutsClient tpClient = new TravelpayoutsClient(client, "token");
            List<com.example.flightdeals.dto.DealCandidate> deals = tpClient.fetchRecentDeals("LYS");
            assertThat(deals).hasSize(1);
            assertThat(deals.get(0).getSource()).isEqualTo(Deal.Source.TRAVELPAYOUTS);
            assertThat(deals.get(0).isNonstop()).isTrue();
            assertThat(deals.get(0).getDepartAt()).isNotNull();
        } finally {
            server.shutdown();
        }
    }
}
