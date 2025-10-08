package com.example.flightdeals.domain;

public class Airport {
    private final String iata;
    private final String city;
    private final String country;
    private final double latitude;
    private final double longitude;

    public Airport(String iata, String city, String country, double latitude, double longitude) {
        this.iata = iata;
        this.city = city;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getIata() {
        return iata;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
