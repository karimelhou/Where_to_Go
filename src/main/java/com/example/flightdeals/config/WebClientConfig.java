package com.example.flightdeals.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient kiwiWebClient(
            @Value("${integrations.kiwi.base-url}") String baseUrl,
            @Value("${integrations.kiwi.api-key}") String apiKey) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("apikey", apiKey)
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                        .build())
                .build();
    }

    @Bean
    public WebClient travelpayoutsWebClient(
            @Value("${integrations.travelpayouts.base-url}") String baseUrl,
            @Value("${integrations.travelpayouts.token}") String token) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-Access-Token", token)
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                        .build())
                .build();
    }
}
