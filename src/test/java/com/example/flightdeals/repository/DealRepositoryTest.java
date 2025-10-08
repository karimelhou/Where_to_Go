package com.example.flightdeals.repository;

import com.example.flightdeals.domain.Deal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class DealRepositoryTest {

    @Autowired
    private DealRepository dealRepository;

    @Test
    void savesDeal() {
        Deal deal = new Deal();
        deal.setOrigin("LYS");
        deal.setDestination("LIS");
        deal.setPrice(BigDecimal.valueOf(90));
        deal.setCurrency("EUR");
        deal.setDepartAt(Instant.now());
        deal.setSource(Deal.Source.KIWI);
        deal.setErrorFare(false);
        deal.setScore(65);
        deal.setFoundAt(Instant.now());
        Deal saved = dealRepository.save(deal);
        assertThat(saved.getId()).isNotNull();
    }
}
