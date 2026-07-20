package com.campingtripplanner.backend.service;

import com.campingtripplanner.backend.domain.Trip;
import com.campingtripplanner.backend.web.dto.WeatherResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

/**
 * Looks up a forecast for a trip's destination/start date via Open-Meteo. Kept separate from
 * {@link TripService} since this is a live external lookup, not core trip CRUD, and every
 * external-call failure mode collapses into the same {@code {"available": false}} response
 * rather than surfacing as an error - see docs/step2-extension/02-design.md.
 */
@Service
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);

    private static final String GEOCODING_URL =
            "https://geocoding-api.open-meteo.com/v1/search?name={destination}&count=1";

    private static final String FORECAST_URL =
            "https://api.open-meteo.com/v1/forecast?latitude={latitude}&longitude={longitude}"
                    + "&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max"
                    + "&forecast_days=16&timezone=auto";

    private final RestTemplate restTemplate;

    public WeatherService(RestTemplate weatherRestTemplate) {
        this.restTemplate = weatherRestTemplate;
    }

    public WeatherResponse getForecastForTrip(Trip trip) {
        GeocodingResult location;
        try {
            location = geocode(trip.getDestination());
        } catch (RestClientException ex) {
            log.warn("Geocoding lookup failed for destination '{}'", trip.getDestination(), ex);
            return WeatherResponse.unavailable();
        }
        if (location == null) {
            return WeatherResponse.unavailable();
        }

        DailyBlock daily;
        try {
            daily = fetchForecast(location);
        } catch (RestClientException ex) {
            log.warn("Forecast lookup failed for destination '{}'", trip.getDestination(), ex);
            return WeatherResponse.unavailable();
        }
        if (daily == null || daily.time() == null) {
            return WeatherResponse.unavailable();
        }

        int index = daily.time().indexOf(trip.getStartDate().toString());
        if (index < 0) {
            return WeatherResponse.unavailable();
        }

        return WeatherResponse.available(
                trip.getStartDate(),
                valueAt(daily.weatherCode(), index),
                valueAt(daily.temperatureMax(), index),
                valueAt(daily.temperatureMin(), index),
                valueAt(daily.precipitationProbabilityMax(), index)
        );
    }

    private GeocodingResult geocode(String destination) {
        GeocodingResponse response = restTemplate.getForObject(GEOCODING_URL, GeocodingResponse.class, destination);
        if (response == null || response.results() == null || response.results().isEmpty()) {
            return null;
        }
        return response.results().get(0);
    }

    private DailyBlock fetchForecast(GeocodingResult location) {
        ForecastApiResponse response = restTemplate.getForObject(
                FORECAST_URL, ForecastApiResponse.class, location.latitude(), location.longitude());
        return response == null ? null : response.daily();
    }

    private static <T> T valueAt(List<T> values, int index) {
        if (values == null || index >= values.size()) {
            return null;
        }
        return values.get(index);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GeocodingResponse(List<GeocodingResult> results) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GeocodingResult(double latitude, double longitude) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ForecastApiResponse(DailyBlock daily) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record DailyBlock(
            List<String> time,
            @JsonProperty("weather_code") List<Integer> weatherCode,
            @JsonProperty("temperature_2m_max") List<Double> temperatureMax,
            @JsonProperty("temperature_2m_min") List<Double> temperatureMin,
            @JsonProperty("precipitation_probability_max") List<Integer> precipitationProbabilityMax
    ) {
    }
}
