package com.campingtripplanner.backend.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WeatherResponse(
        boolean available,
        LocalDate date,
        Integer weatherCode,
        Double temperatureMaxC,
        Double temperatureMinC,
        Integer precipitationProbabilityMax
) {

    public static WeatherResponse unavailable() {
        return new WeatherResponse(false, null, null, null, null, null);
    }

    public static WeatherResponse available(LocalDate date, Integer weatherCode, Double temperatureMaxC,
                                             Double temperatureMinC, Integer precipitationProbabilityMax) {
        return new WeatherResponse(true, date, weatherCode, temperatureMaxC, temperatureMinC,
                precipitationProbabilityMax);
    }
}
