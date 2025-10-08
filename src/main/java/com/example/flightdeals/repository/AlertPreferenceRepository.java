package com.example.flightdeals.repository;

import com.example.flightdeals.domain.AlertPreference;
import com.example.flightdeals.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AlertPreferenceRepository extends JpaRepository<AlertPreference, UUID> {
    Optional<AlertPreference> findByUser(User user);
}
