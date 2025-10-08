package com.example.flightdeals.service;

import com.example.flightdeals.domain.AlertPreference;
import com.example.flightdeals.domain.User;
import com.example.flightdeals.repository.AlertPreferenceRepository;
import com.example.flightdeals.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AlertPreferenceRepository preferenceRepository;

    public UserService(UserRepository userRepository, AlertPreferenceRepository preferenceRepository) {
        this.userRepository = userRepository;
        this.preferenceRepository = preferenceRepository;
    }

    public User createUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> {
                    User user = new User();
                    user.setEmail(email.toLowerCase());
                    return userRepository.save(user);
                });
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public AlertPreference upsertPreference(User user, String homeAirport, Integer maxBudget, boolean nonstopOnly, List<String> regions) {
        AlertPreference preference = preferenceRepository.findByUser(user)
                .orElseGet(() -> {
                    AlertPreference created = new AlertPreference();
                    created.setUser(user);
                    return created;
                });
        preference.setHomeAirport(homeAirport);
        preference.setMaxBudget(maxBudget);
        preference.setNonstopOnly(nonstopOnly);
        preference.setRegions(regions == null || regions.isEmpty() ? null : String.join(",", regions));
        return preferenceRepository.save(preference);
    }
}
