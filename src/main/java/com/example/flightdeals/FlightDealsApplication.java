package com.example.flightdeals;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FlightDealsApplication {
    public static void main(String[] args) {
        SpringApplication.run(FlightDealsApplication.class, args);
    }
}
