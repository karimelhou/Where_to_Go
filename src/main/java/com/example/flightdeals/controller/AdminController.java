package com.example.flightdeals.controller;

import com.example.flightdeals.config.AppProperties;
import com.example.flightdeals.service.FetchService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AppProperties properties;
    private final FetchService fetchService;

    public AdminController(AppProperties properties, FetchService fetchService) {
        this.properties = properties;
        this.fetchService = fetchService;
    }

    @PostMapping("/fetch")
    public ResponseEntity<?> fetch(@RequestHeader("X-ADMIN-TOKEN") String token,
                                   @Valid @RequestBody FetchRequest request) {
        if (token == null || !token.equals(properties.getAdmin().getToken())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        int fetched = 0;
        int stored = 0;
        for (String origin : request.origins()) {
            FetchService.FetchResult result = fetchService.fetchOrigin(origin, request.nonstopOnlyFlag(), request.maxBudget());
            fetched += result.fetched();
            stored += result.stored();
        }
        return ResponseEntity.ok(new FetchResponse(fetched, stored));
    }

    public record FetchRequest(@NotEmpty List<@Size(min = 3, max = 3) String> origins,
                               Boolean nonstopOnly,
                               Integer maxBudget) {
        public boolean nonstopOnlyFlag() {
            return nonstopOnly != null && nonstopOnly;
        }
    }

    public record FetchResponse(int fetched, int stored) {
    }
}
