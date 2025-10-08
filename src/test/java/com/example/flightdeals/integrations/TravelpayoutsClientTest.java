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
        String body = """
                {
                  "data": {
                    "LIS": [
                      {
                        "price": 70,
                        "currency": "EUR",
                        "departDate": "2024-05-20T00:00:00",
                        "returnDate": "2024-05-27T00:00:00",
                        "transfers": 0,
                        "link": "http://deal"
                      }
                    ]
                  }
                }
                """;
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(body));
        server.start();
        try {
            WebClient client = WebClient.builder()
                    .baseUrl(server.url("/").toString())
                    .defaultHeader("X-Access-Token", "test")
                    .build();
            TravelpayoutsClient tpClient = new TravelpayoutsClient(client);
            List<com.example.flightdeals.dto.DealCandidate> deals = tpClient.fetchRecentDeals("LYS");
            assertThat(deals).hasSize(1);
            assertThat(deals.get(0).getSource()).isEqualTo(Deal.Source.TRAVELPAYOUTS);
        } finally {
            server.shutdown();
        }
    }
}
