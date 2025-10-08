package com.example.flightdeals.controller;

import com.example.flightdeals.domain.AlertPreference;
import com.example.flightdeals.domain.Deal;
import com.example.flightdeals.domain.User;
import com.example.flightdeals.repository.DealRepository;
import com.example.flightdeals.service.UserService;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/deals")
public class DealController {

    private final DealRepository dealRepository;
    private final UserService userService;

    public DealController(DealRepository dealRepository, UserService userService) {
        this.dealRepository = dealRepository;
        this.userService = userService;
    }

    @GetMapping
    public List<Deal> list(@RequestParam(required = false) String origin,
                           @RequestParam(required = false) String destination,
                           @RequestParam(required = false) BigDecimal maxPrice,
                           @RequestParam(required = false) Double minScore,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant since,
                           @RequestParam(required = false, defaultValue = "50") @Positive Integer limit,
                           @RequestParam(required = false) String email) {
        if (email != null && !email.isBlank()) {
            Optional<User> user = userService.findByEmail(email);
            if (user.isPresent()) {
                AlertPreference preference = user.get().getPreference();
                if (preference != null) {
                    if (origin == null) {
                        origin = preference.getHomeAirport();
                    }
                    if (maxPrice == null && preference.getMaxBudget() != null) {
                        maxPrice = BigDecimal.valueOf(preference.getMaxBudget());
                    }
                    if (minScore == null) {
                        minScore = 60d;
                    }
                }
            }
        }
        if (minScore == null) {
            minScore = 60d;
        }
        return dealRepository.search(origin, destination, maxPrice, minScore, since, PageRequest.of(0, Math.min(limit, 200)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Deal> get(@PathVariable UUID id) {
        return dealRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
