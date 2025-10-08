package com.example.flightdeals.controller;

import com.example.flightdeals.domain.Deal;
import com.example.flightdeals.dto.DealCandidate;
import com.example.flightdeals.integrations.amadeus.AmadeusClient;
import com.example.flightdeals.integrations.travelpayouts.TravelpayoutsClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {"app.admin.token=test-token","spring.jpa.hibernate.ddl-auto=create-drop","spring.flyway.enabled=false"})
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class AdminControllerIntegrationTest {

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:admin;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.datasource.driverClassName", () -> "org.h2.Driver");
        registry.add("app.scheduler.cron", () -> "0 0 */12 * * *");
    }

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    AmadeusClient amadeusClient;

    @MockBean
    TravelpayoutsClient travelpayoutsClient;

    @Test
    void fetchEndpointIngestsDeals() {
        DealCandidate candidate = new DealCandidate();
        candidate.setOrigin("LYS");
        candidate.setDestination("LIS");
        candidate.setPrice(BigDecimal.valueOf(80));
        candidate.setCurrency("EUR");
        candidate.setDepartAt(Instant.now().plusSeconds(86400));
        candidate.setSource(Deal.Source.TRAVELPAYOUTS);
        candidate.setNonstop(true);
        when(travelpayoutsClient.fetchRecentDeals(anyString())).thenReturn(List.of(candidate));
        when(amadeusClient.validateOffer(any())).thenReturn(true);
        when(amadeusClient.searchPopularDestinations(anyString())).thenReturn(List.of());

        webTestClient.post()
                .uri("/api/v1/admin/fetch")
                .header("X-ADMIN-TOKEN", "test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"origins\":[\"LYS\"]}")
                .exchange()
                .expectStatus().isOk();
    }
}
