package com.campingtripplanner.backend.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class WeatherClientConfig {

    // Without an explicit timeout a slow/hanging Open-Meteo call could make
    // trip-detail loading hang indefinitely (NFR3).
    private static final Duration EXTERNAL_API_TIMEOUT = Duration.ofSeconds(5);

    @Bean
    public RestTemplate weatherRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(EXTERNAL_API_TIMEOUT)
                .setReadTimeout(EXTERNAL_API_TIMEOUT)
                .build();
    }
}
