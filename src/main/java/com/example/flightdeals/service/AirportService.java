package com.example.flightdeals.service;

import com.example.flightdeals.domain.Airport;
import com.example.flightdeals.util.Haversine;
import jakarta.annotation.PostConstruct;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class AirportService {

    private final Map<String, Airport> airportsByCode = new ConcurrentHashMap<>();

    @PostConstruct
    void init() throws IOException {
        ClassPathResource resource = new ClassPathResource("airports_eu.csv");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("iata")) {
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length < 5) {
                    continue;
                }
                Airport airport = new Airport(parts[0].trim(), parts[1].trim(), parts[2].trim(),
                        Double.parseDouble(parts[3]), Double.parseDouble(parts[4]));
                airportsByCode.put(airport.getIata().toUpperCase(Locale.ROOT), airport);
            }
        }
    }

    public Optional<Airport> findByCode(String code) {
        if (code == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(airportsByCode.get(code.toUpperCase(Locale.ROOT)));
    }

    @Cacheable("airport-search")
    public List<Airport> search(String query) {
        if (query == null || query.isBlank()) {
            return new ArrayList<>(airportsByCode.values()).stream()
                    .sorted((a, b) -> a.getIata().compareToIgnoreCase(b.getIata()))
                    .limit(50)
                    .collect(Collectors.toList());
        }
        String q = query.toLowerCase(Locale.ROOT);
        return airportsByCode.values().stream()
                .filter(a -> a.getIata().toLowerCase(Locale.ROOT).contains(q)
                        || a.getCity().toLowerCase(Locale.ROOT).contains(q)
                        || a.getCountry().toLowerCase(Locale.ROOT).contains(q))
                .sorted((a, b) -> a.getIata().compareToIgnoreCase(b.getIata()))
                .limit(50)
                .collect(Collectors.toList());
    }

    public double distanceKm(String origin, String destination) {
        Airport a = airportsByCode.get(origin.toUpperCase(Locale.ROOT));
        Airport b = airportsByCode.get(destination.toUpperCase(Locale.ROOT));
        if (a == null || b == null) {
            return Double.NaN;
        }
        return Haversine.distance(a.getLatitude(), a.getLongitude(), b.getLatitude(), b.getLongitude());
    }
}
