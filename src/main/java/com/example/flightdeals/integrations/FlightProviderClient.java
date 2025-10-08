package com.example.flightdeals.integrations;

import com.example.flightdeals.dto.DealCandidate;

import java.time.Instant;
import java.util.List;

public interface FlightProviderClient {
    List<DealCandidate> search(String origin, Instant dateFrom, Instant dateTo, boolean nonstopOnly, Integer maxPrice);
}
