package com.example.flightdeals.integrations.travelpayouts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TravelpayoutsResponse(Map<String, List<Item>> data) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(double price, String currency, String departDate, String returnDate, int transfers, String link) {
    }
}
