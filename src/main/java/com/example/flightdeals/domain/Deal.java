package com.example.flightdeals.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "deals")
public class Deal {

    public enum Source {
        TRAVELPAYOUTS,
        AMADEUS
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 3)
    private String origin;

    @Column(nullable = false, length = 3)
    private String destination;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private String currency;

    @Column(name = "depart_at")
    private Instant departAt;

    @Column(name = "return_at")
    private Instant returnAt;

    @Column(name = "deep_link", length = 2048)
    private String deepLink;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Source source;

    @Column(name = "is_error_fare", nullable = false)
    private boolean errorFare;

    @Column(nullable = false)
    private double score;

    @Column(name = "found_at", nullable = false)
    private Instant foundAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "raw_payload")
    private String rawPayload;

    @Column(name = "price_per_km")
    private Double pricePerKm;

    @Column(name = "validated_by_amadeus", nullable = false)
    private boolean validatedByAmadeus;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Instant getDepartAt() {
        return departAt;
    }

    public void setDepartAt(Instant departAt) {
        this.departAt = departAt;
    }

    public Instant getReturnAt() {
        return returnAt;
    }

    public void setReturnAt(Instant returnAt) {
        this.returnAt = returnAt;
    }

    public String getDeepLink() {
        return deepLink;
    }

    public void setDeepLink(String deepLink) {
        this.deepLink = deepLink;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public boolean isErrorFare() {
        return errorFare;
    }

    public void setErrorFare(boolean errorFare) {
        this.errorFare = errorFare;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public Instant getFoundAt() {
        return foundAt;
    }

    public void setFoundAt(Instant foundAt) {
        this.foundAt = foundAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public void setRawPayload(String rawPayload) {
        this.rawPayload = rawPayload;
    }

    public Double getPricePerKm() {
        return pricePerKm;
    }

    public void setPricePerKm(Double pricePerKm) {
        this.pricePerKm = pricePerKm;
    }

    public boolean isValidatedByAmadeus() {
        return validatedByAmadeus;
    }

    public void setValidatedByAmadeus(boolean validatedByAmadeus) {
        this.validatedByAmadeus = validatedByAmadeus;
    }
}
