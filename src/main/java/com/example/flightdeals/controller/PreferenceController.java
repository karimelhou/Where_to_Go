package com.example.flightdeals.controller;

import com.example.flightdeals.domain.AlertPreference;
import com.example.flightdeals.domain.User;
import com.example.flightdeals.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/prefs")
public class PreferenceController {

    private final UserService userService;

    public PreferenceController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<AlertPreference> upsert(@Valid @RequestBody PreferenceRequest request) {
        User user = userService.findByEmail(request.email())
                .orElseGet(() -> userService.createUser(request.email()));
        AlertPreference preference = userService.upsertPreference(user, request.homeAirport(), request.maxBudget(),
                request.nonstopOnly() != null && request.nonstopOnly(), request.regions());
        return ResponseEntity.ok(preference);
    }

    public record PreferenceRequest(@Email @NotBlank String email,
                                    @NotBlank @Size(min = 3, max = 3) String homeAirport,
                                    Integer maxBudget,
                                    Boolean nonstopOnly,
                                    List<String> regions) {
    }
}
