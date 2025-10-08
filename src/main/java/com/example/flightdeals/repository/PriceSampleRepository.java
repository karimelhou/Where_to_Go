package com.example.flightdeals.repository;

import com.example.flightdeals.domain.PriceSample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PriceSampleRepository extends JpaRepository<PriceSample, UUID> {

    @Query("select ps from PriceSample ps where ps.origin = :origin and ps.destination = :destination and ps.collectedAt >= :since order by ps.collectedAt desc")
    List<PriceSample> findRecentSamples(@Param("origin") String origin,
                                        @Param("destination") String destination,
                                        @Param("since") Instant since);
}
