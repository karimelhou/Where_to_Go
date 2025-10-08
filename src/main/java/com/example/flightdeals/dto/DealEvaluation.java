package com.example.flightdeals.dto;

public class DealEvaluation {
    private final double score;
    private final boolean errorFare;
    private final double discountRatio;
    private final double pricePerKm;

    public DealEvaluation(double score, boolean errorFare, double discountRatio, double pricePerKm) {
        this.score = score;
        this.errorFare = errorFare;
        this.discountRatio = discountRatio;
        this.pricePerKm = pricePerKm;
    }

    public double getScore() {
        return score;
    }

    public boolean isErrorFare() {
        return errorFare;
    }

    public double getDiscountRatio() {
        return discountRatio;
    }

    public double getPricePerKm() {
        return pricePerKm;
    }
}
