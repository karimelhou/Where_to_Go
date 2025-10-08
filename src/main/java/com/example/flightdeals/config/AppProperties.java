package com.example.flightdeals.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Scheduler scheduler = new Scheduler();
    private Thresholds thresholds = new Thresholds();
    private Admin admin = new Admin();
    private Fetch fetch = new Fetch();

    public Scheduler getScheduler() {
        return scheduler;
    }

    public Thresholds getThresholds() {
        return thresholds;
    }

    public Admin getAdmin() {
        return admin;
    }

    public Fetch getFetch() {
        return fetch;
    }

    public static class Scheduler {
        private String cron;

        public String getCron() {
            return cron;
        }

        public void setCron(String cron) {
            this.cron = cron;
        }
    }

    public static class Thresholds {
        private double dealDiscount;
        private double errorDiscount;
        private double errorPricePerKm;
        private double minScore;
        private double ultraLowLongHaul;

        public double getDealDiscount() {
            return dealDiscount;
        }

        public void setDealDiscount(double dealDiscount) {
            this.dealDiscount = dealDiscount;
        }

        public double getErrorDiscount() {
            return errorDiscount;
        }

        public void setErrorDiscount(double errorDiscount) {
            this.errorDiscount = errorDiscount;
        }

        public double getErrorPricePerKm() {
            return errorPricePerKm;
        }

        public void setErrorPricePerKm(double errorPricePerKm) {
            this.errorPricePerKm = errorPricePerKm;
        }

        public double getMinScore() {
            return minScore;
        }

        public void setMinScore(double minScore) {
            this.minScore = minScore;
        }

        public double getUltraLowLongHaul() {
            return ultraLowLongHaul;
        }

        public void setUltraLowLongHaul(double ultraLowLongHaul) {
            this.ultraLowLongHaul = ultraLowLongHaul;
        }
    }

    public static class Admin {
        private String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

    public static class Fetch {
        private int searchLookaheadDays;
        private String monitoredOrigins;

        public int getSearchLookaheadDays() {
            return searchLookaheadDays;
        }

        public void setSearchLookaheadDays(int searchLookaheadDays) {
            this.searchLookaheadDays = searchLookaheadDays;
        }

        public String getMonitoredOrigins() {
            return monitoredOrigins;
        }

        public void setMonitoredOrigins(String monitoredOrigins) {
            this.monitoredOrigins = monitoredOrigins;
        }
    }
}
