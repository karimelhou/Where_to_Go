package com.example.flightdeals.repository;

import com.example.flightdeals.domain.Deal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DealRepository extends JpaRepository<Deal, UUID> {

    @Query("select d from Deal d where (:origin is null or d.origin = :origin) " +
            "and (:destination is null or d.destination = :destination) " +
            "and (:maxPrice is null or d.price <= :maxPrice) " +
            "and (:minScore is null or d.score >= :minScore) " +
            "and (:since is null or d.foundAt >= :since) order by d.foundAt desc")
    List<Deal> search(@Param("origin") String origin,
                      @Param("destination") String destination,
                      @Param("maxPrice") java.math.BigDecimal maxPrice,
                      @Param("minScore") Double minScore,
                      @Param("since") Instant since,
                      org.springframework.data.domain.Pageable pageable);

    @Query("select d from Deal d where d.origin = :origin and d.destination = :destination and d.source = :source " +
            "and d.price = :price and ((d.departAt is null and :departAt is null) or d.departAt = :departAt) " +
            "and ((d.returnAt is null and :returnAt is null) or d.returnAt = :returnAt) " +
            "and d.foundAt >= :since")
    Optional<Deal> findRecentDuplicate(@Param("origin") String origin,
                                       @Param("destination") String destination,
                                       @Param("source") Deal.Source source,
                                       @Param("price") java.math.BigDecimal price,
                                       @Param("departAt") Instant departAt,
                                       @Param("returnAt") Instant returnAt,
                                       @Param("since") Instant since);
}
