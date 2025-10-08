package com.example.flightdeals.integrations.kiwi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KiwiResponse(String currency, List<FlightData> data) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FlightData(String cityFrom, String cityTo, double price, long dTimeUTC,
                              List<Route> route, List<Route> returnRoute, String deepLink, String raw) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Route(long dTimeUTC) {
    }
}
