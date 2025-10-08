package com.example.flightdeals.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HaversineTest {

    @Test
    void distanceBetweenParisAndLyonIsReasonable() {
        double distance = Haversine.distance(48.8566, 2.3522, 45.7640, 4.8357);
        assertThat(distance).isBetween(380.0, 410.0);
    }
}
